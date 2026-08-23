-- 补充模板名称和敏感参数元数据，并清理 V31 生成的无效验证码渠道。

ALTER TABLE spectra_notification.ntf_template
    ADD COLUMN IF NOT EXISTS template_name VARCHAR(200);

UPDATE spectra_notification.ntf_template
SET template_name = CASE template_group_code
                        WHEN 'security.login-code' THEN '登录验证码'
                        WHEN 'security.bind-phone-code' THEN '绑定手机号验证码'
                        WHEN 'security.bind-email-code' THEN '绑定邮箱验证码'
                        WHEN 'security.reset-password-code' THEN '重置密码验证码'
                        WHEN 'workflow.task.todo' THEN '流程待办任务通知'
                        WHEN 'workflow.task.result' THEN '流程任务处理结果通知'
                        WHEN 'oa.meeting.invitation' THEN '会议邀请通知'
                        WHEN 'oa.notice.published' THEN '公告发布通知'
                        WHEN 'oa.document.published' THEN '文档发布通知'
                        WHEN 'oa.application.status' THEN 'OA申请状态通知'
                        WHEN 'oa.contract.milestone.reminder' THEN '合同履约节点提醒'
                        WHEN 'ai.rag.index' THEN '知识库索引通知'
                        WHEN 'system.service-monitor-alert' THEN '服务监控告警'
                        WHEN 'system.notice' THEN '系统通知'
                        ELSE template_group_code
                    END
WHERE template_name IS NULL;

ALTER TABLE spectra_notification.ntf_template
    ALTER COLUMN template_name SET NOT NULL;

COMMENT ON COLUMN spectra_notification.ntf_template.template_name IS '模板名称，供管理端识别和展示';

UPDATE spectra_notification.ntf_template
SET parameter_schema = jsonb_set(
        parameter_schema,
        '{properties,code,sensitive}',
        'true'::jsonb,
        true)
WHERE template_group_code IN (
          'security.login-code',
          'security.bind-phone-code',
          'security.bind-email-code',
          'security.reset-password-code')
  AND version_no = 1
  AND created_by IS NULL
  AND parameter_schema ? 'properties'
  AND parameter_schema -> 'properties' ? 'code';

UPDATE spectra_notification.ntf_template
SET version_digest = encode(
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
        'hex')
WHERE template_group_code IN (
          'security.login-code',
          'security.bind-phone-code',
          'security.bind-email-code',
          'security.reset-password-code')
  AND version_no = 1
  AND created_by IS NULL;

DELETE FROM spectra_notification.ntf_template
WHERE version_no = 1
  AND state = 'PUBLISHED'
  AND created_by IS NULL
  AND (
      (template_group_code IN ('security.login-code', 'security.reset-password-code')
          AND channel = 'IN_APP')
      OR (template_group_code = 'security.bind-phone-code' AND channel <> 'SMS')
      OR (template_group_code = 'security.bind-email-code' AND channel <> 'EMAIL')
  );
