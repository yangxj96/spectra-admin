-- 通知模板直接切换为最终生命周期模型；不保留 enabled 双轨字段。

ALTER TABLE spectra_notification.ntf_template
    ADD COLUMN state VARCHAR(16) NOT NULL DEFAULT 'DRAFT';

UPDATE spectra_notification.ntf_template
SET state = CASE WHEN enabled THEN 'PUBLISHED' ELSE 'DISABLED' END
WHERE deleted IS NULL;

ALTER TABLE spectra_notification.ntf_template
    ADD CONSTRAINT "CK_NTF_TEMPLATE_STATE"
        CHECK (state IN ('DRAFT', 'PUBLISHED', 'DISABLED', 'ARCHIVED'));

DROP INDEX IF EXISTS spectra_notification."UK_NTF_TEMPLATE_ENABLED";

CREATE UNIQUE INDEX "UK_NTF_TEMPLATE_PUBLISHED"
    ON spectra_notification.ntf_template (template_group_code, channel)
    WHERE state = 'PUBLISHED' AND deleted IS NULL;

COMMENT ON COLUMN spectra_notification.ntf_template.state IS '模板生命周期状态：DRAFT、PUBLISHED、DISABLED或ARCHIVED';

ALTER TABLE spectra_notification.ntf_template
    DROP COLUMN enabled;
