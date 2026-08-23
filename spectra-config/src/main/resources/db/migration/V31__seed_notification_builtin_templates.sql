-- 统一通知快捷发送使用的内置模板；模板组编码跨渠道复用，具体内容按渠道独立发布。
-- 参数可以为空：system.notice 作为无参数模板示例，其余模板只声明正文实际使用的参数。

WITH base_templates(template_group_code, purpose, title_template, content_template, parameter_schema) AS (
    VALUES
        ('security.login-code', 'LOGIN_CODE', '登录验证码', '您的验证码为 {{code}}，请在有效期内完成操作。', '{"properties":{"code":{"type":"string"}}}'::jsonb),
        ('security.bind-phone-code', 'BIND_PHONE_CODE', '绑定手机号验证码', '您的验证码为 {{code}}，请在有效期内完成操作。', '{"properties":{"code":{"type":"string"}}}'::jsonb),
        ('security.bind-email-code', 'BIND_EMAIL_CODE', '绑定邮箱验证码', '您的验证码为 {{code}}，请在有效期内完成操作。', '{"properties":{"code":{"type":"string"}}}'::jsonb),
        ('security.reset-password-code', 'RESET_PASSWORD_CODE', '重置密码验证码', '您的验证码为 {{code}}，请在有效期内完成操作。', '{"properties":{"code":{"type":"string"}}}'::jsonb),
        ('workflow.task.todo', 'WORKFLOW_TODO', '新的待办任务', '您有新的流程待办任务：{{task_name}}', '{"properties":{"task_name":{"type":"string"}}}'::jsonb),
        ('workflow.task.result', 'WORKFLOW_RESULT', '流程任务{{result}}', '您处理的流程任务已{{result}}。', '{"properties":{"result":{"type":"string"}}}'::jsonb),
        ('oa.meeting.invitation', 'OA_REMINDER', '会议邀请：{{meeting_title}}', '{{content}}', '{"properties":{"meeting_title":{"type":"string"},"content":{"type":"string"}}}'::jsonb),
        ('oa.notice.published', 'OA_NOTICE', '{{title}}', '{{content}}', '{"properties":{"title":{"type":"string"},"content":{"type":"string"}}}'::jsonb),
        ('oa.document.published', 'OA_NOTICE', '文档已发布：{{document_title}}', '{{summary}}', '{"properties":{"document_title":{"type":"string"},"summary":{"type":"string"}}}'::jsonb),
        ('oa.application.status', 'OA_NOTICE', '{{title}}', '{{content}}', '{"properties":{"title":{"type":"string"},"content":{"type":"string"}}}'::jsonb),
        ('oa.contract.milestone.reminder', 'OA_REMINDER', '合同履约节点即将到期', '合同「{{contract_title}}」的履约节点「{{milestone_name}}」将于 {{due_date}} 到期，请及时处理。', '{"properties":{"contract_title":{"type":"string"},"milestone_name":{"type":"string"},"due_date":{"type":"string"}}}'::jsonb),
        ('ai.rag.index', 'SYSTEM_NOTICE', '知识库索引{{status}}', '文件「{{file_name}}」{{message}}', '{"properties":{"status":{"type":"string"},"file_name":{"type":"string"},"message":{"type":"string"}}}'::jsonb),
        ('system.service-monitor-alert', 'SYSTEM_NOTICE', '服务监控告警：{{rule_name}}', '{{message}}，请在服务监控中查看。', '{"properties":{"rule_name":{"type":"string"},"message":{"type":"string"}}}'::jsonb),
        ('system.notice', 'SYSTEM_NOTICE', '系统通知', '系统通知', '{"properties":{}}'::jsonb)
),
template_values AS (
    SELECT base.template_group_code,
           channel.channel,
           base.purpose,
           base.title_template,
           base.content_template,
           base.parameter_schema
    FROM base_templates base
    CROSS JOIN (VALUES ('IN_APP'), ('SMS'), ('EMAIL')) channel(channel)
),
prepared AS (
    SELECT gen_random_uuid() AS id,
           template_group_code,
           channel,
           purpose,
           1 AS version_no,
           title_template,
           content_template,
           NULL::text AS html_template,
           parameter_schema,
           NULL::varchar(200) AS provider_template_code,
           'PUBLISHED' AS state,
           encode(
               digest(
                   concat_ws(chr(31),
                       coalesce(template_group_code, '<NULL>'),
                       coalesce(channel, '<NULL>'),
                       coalesce(purpose, '<NULL>'),
                       coalesce(version_no::text, '<NULL>'),
                       coalesce(title_template, '<NULL>'),
                       coalesce(content_template, '<NULL>'),
                       coalesce(html_template, '<NULL>'),
                       coalesce(parameter_schema::text, '<NULL>'),
                       coalesce(provider_template_code, '<NULL>')),
                   'sha256'),
               'hex') AS version_digest
    FROM template_values
)
INSERT INTO spectra_notification.ntf_template (
    id, template_group_code, channel, purpose, version_no, title_template, content_template, html_template,
    parameter_schema, provider_template_code, state, version_digest, created_at, updated_at, version)
SELECT prepared.id,
       prepared.template_group_code,
       prepared.channel,
       prepared.purpose,
       prepared.version_no,
       prepared.title_template,
       prepared.content_template,
       prepared.html_template,
       prepared.parameter_schema,
       prepared.provider_template_code,
       prepared.state,
       prepared.version_digest,
       CURRENT_TIMESTAMP,
       CURRENT_TIMESTAMP,
       0
FROM prepared
WHERE NOT EXISTS (
    SELECT 1
    FROM spectra_notification.ntf_template existing
    WHERE existing.template_group_code = prepared.template_group_code
      AND existing.channel = prepared.channel
      AND existing.version_no = prepared.version_no
      AND existing.deleted IS NULL
);
