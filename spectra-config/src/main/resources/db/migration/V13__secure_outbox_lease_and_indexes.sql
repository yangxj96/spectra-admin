-- 安全变更 outbox 和审计归档清单的租约、幂等和重试基础设施。

ALTER TABLE spectra_security.sec_security_change_outbox
    ADD COLUMN idempotency_key character varying(64),
    ADD COLUMN correlation_id character varying(100),
    ADD COLUMN state character varying(32) DEFAULT 'PENDING' NOT NULL,
    ADD COLUMN available_at timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ADD COLUMN lease_owner character varying(128),
    ADD COLUMN lease_until timestamp(6) with time zone;

UPDATE spectra_security.sec_security_change_outbox
SET idempotency_key = md5(concat_ws('|', event_type, aggregate_type, aggregate_id::text, payload::text)),
    state = CASE WHEN processed_at IS NULL THEN 'PENDING' ELSE 'PROCESSED' END
WHERE idempotency_key IS NULL;

ALTER TABLE spectra_security.sec_security_change_outbox
    ALTER COLUMN idempotency_key SET NOT NULL,
    ALTER COLUMN last_error TYPE character varying(2000);

ALTER TABLE spectra_security.sec_security_change_outbox
    ADD CONSTRAINT ck_sec_security_change_outbox_state
        CHECK (state IN ('PENDING', 'PROCESSING', 'PROCESSED', 'DEAD_LETTER')),
    ADD CONSTRAINT ck_sec_security_change_outbox_attempts
        CHECK (attempts >= 0);

COMMENT ON COLUMN spectra_security.sec_security_change_outbox.idempotency_key IS '事件内容幂等摘要，避免相同安全变更重复产生外部动作';
COMMENT ON COLUMN spectra_security.sec_security_change_outbox.correlation_id IS '关联请求或事务 ID';
COMMENT ON COLUMN spectra_security.sec_security_change_outbox.state IS 'PENDING、PROCESSING、PROCESSED 或 DEAD_LETTER';
COMMENT ON COLUMN spectra_security.sec_security_change_outbox.available_at IS '下一次可领取时间';
COMMENT ON COLUMN spectra_security.sec_security_change_outbox.lease_owner IS '当前租约持有者';
COMMENT ON COLUMN spectra_security.sec_security_change_outbox.lease_until IS '当前租约截止时间';

CREATE UNIQUE INDEX uk_sec_security_change_outbox_idempotency_key
    ON spectra_security.sec_security_change_outbox (idempotency_key);

CREATE INDEX idx_sec_security_change_outbox_pending
    ON spectra_security.sec_security_change_outbox (available_at, created_at)
    WHERE deleted IS NULL AND processed_at IS NULL AND state IN ('PENDING', 'PROCESSING');

CREATE INDEX idx_sec_security_change_outbox_lease
    ON spectra_security.sec_security_change_outbox (lease_until)
    WHERE deleted IS NULL AND state = 'PROCESSING';

ALTER TABLE spectra_security.sec_security_audit_archive_manifest
    ADD COLUMN content_length bigint,
    ADD COLUMN available_at timestamp(6) with time zone DEFAULT CURRENT_TIMESTAMP NOT NULL,
    ADD COLUMN attempts integer DEFAULT 0 NOT NULL,
    ADD COLUMN lease_owner character varying(128),
    ADD COLUMN lease_until timestamp(6) with time zone;

ALTER TABLE spectra_security.sec_security_audit_archive_manifest
    ADD CONSTRAINT ck_sec_security_audit_archive_manifest_attempts
        CHECK (attempts >= 0),
    ADD CONSTRAINT ck_sec_security_audit_archive_manifest_content_length
        CHECK (content_length IS NULL OR content_length >= 0);

COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.available_at IS '下一次可执行归档或恢复时间';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.content_length IS '归档对象 UTF-8 字节长度';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.attempts IS '归档或恢复尝试次数';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.lease_owner IS '当前归档 worker 租约持有者';
COMMENT ON COLUMN spectra_security.sec_security_audit_archive_manifest.lease_until IS '当前归档 worker 租约截止时间';

CREATE INDEX idx_sec_security_audit_archive_manifest_pending
    ON spectra_security.sec_security_audit_archive_manifest (available_at, created_at)
    WHERE deleted IS NULL AND state IN ('PLANNED', 'ARCHIVED', 'RESTORE_PENDING', 'FAILED');

CREATE INDEX idx_sec_security_audit_archive_manifest_lease
    ON spectra_security.sec_security_audit_archive_manifest (lease_until)
    WHERE deleted IS NULL AND lease_until IS NOT NULL;
