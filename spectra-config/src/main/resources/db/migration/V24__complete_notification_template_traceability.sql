-- 完成通知模板版本摘要和投递链路追溯，不保留旧字段或双写兼容分支。

ALTER TABLE spectra_notification.ntf_template
    ADD COLUMN version_digest VARCHAR(64);

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
WHERE version_digest IS NULL;

ALTER TABLE spectra_notification.ntf_template
    ALTER COLUMN version_digest SET NOT NULL,
    ADD CONSTRAINT "CK_NTF_TEMPLATE_VERSION_DIGEST"
        CHECK (version_digest ~ '^[0-9a-f]{64}$');

COMMENT ON COLUMN spectra_notification.ntf_template.version_digest IS '模板版本内容 SHA-256 摘要，发布后用于不可变追溯';

ALTER TABLE spectra_notification.ntf_request
    ADD COLUMN template_snapshot JSONB NOT NULL DEFAULT '{}'::jsonb;

COMMENT ON COLUMN spectra_notification.ntf_request.template_snapshot IS '各渠道实际模板版本元数据，不包含渲染正文和敏感参数';

ALTER TABLE spectra_notification.ntf_task
    ADD COLUMN template_version_no INTEGER,
    ADD COLUMN template_version_digest VARCHAR(64);

UPDATE spectra_notification.ntf_task task
SET template_version_no = template.version_no,
    template_version_digest = template.version_digest
FROM spectra_notification.ntf_template template
WHERE task.template_id = template.id;

ALTER TABLE spectra_notification.ntf_task
    ADD CONSTRAINT "CK_NTF_TASK_TEMPLATE_DIGEST"
        CHECK (template_version_digest IS NULL OR template_version_digest ~ '^[0-9a-f]{64}$');

COMMENT ON COLUMN spectra_notification.ntf_task.template_version_no IS '任务锁定的模板业务版本号';
COMMENT ON COLUMN spectra_notification.ntf_task.template_version_digest IS '任务锁定的模板内容 SHA-256 摘要';

ALTER TABLE spectra_notification.ntf_delivery
    ADD COLUMN template_id UUID,
    ADD COLUMN template_version_no INTEGER,
    ADD COLUMN template_version_digest VARCHAR(64),
    ADD COLUMN rendered_title TEXT,
    ADD COLUMN rendered_content TEXT;

UPDATE spectra_notification.ntf_delivery delivery
SET template_id = task.template_id,
    template_version_no = task.template_version_no,
    template_version_digest = task.template_version_digest,
    rendered_title = task.title,
    rendered_content = task.content
FROM spectra_notification.ntf_task task
WHERE delivery.notification_task_id = task.id;

ALTER TABLE spectra_notification.ntf_delivery
    ADD CONSTRAINT "FK_NTF_DELIVERY_TEMPLATE" FOREIGN KEY (template_id)
        REFERENCES spectra_notification.ntf_template (id),
    ADD CONSTRAINT "CK_NTF_DELIVERY_TEMPLATE_DIGEST"
        CHECK (template_version_digest IS NULL OR template_version_digest ~ '^[0-9a-f]{64}$');

COMMENT ON COLUMN spectra_notification.ntf_delivery.template_id IS '投递时锁定的模板版本 ID';
COMMENT ON COLUMN spectra_notification.ntf_delivery.template_version_no IS '投递时锁定的模板业务版本号';
COMMENT ON COLUMN spectra_notification.ntf_delivery.template_version_digest IS '投递时锁定的模板内容 SHA-256 摘要';
COMMENT ON COLUMN spectra_notification.ntf_delivery.rendered_title IS '投递时使用的渲染标题快照';
COMMENT ON COLUMN spectra_notification.ntf_delivery.rendered_content IS '投递时使用的渲染正文快照';
