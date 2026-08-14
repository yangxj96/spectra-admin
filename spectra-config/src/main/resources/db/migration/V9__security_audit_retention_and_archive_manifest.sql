-- Phase 8 Security Audit retention metadata and future archive manifest.
-- The application exposes read-only metadata only; partition detach/archive operations
-- must be executed by an audited operational workflow with independent database rights.
-- Navigation remains separate from Permission rows; role_menu is populated by role provisioning.
INSERT INTO spectra_core.sys_menu (id,name,pid,icon,menu_type,route_name,sort,created_by,created_at,updated_by,updated_at,deleted,version)
VALUES ('019fdba9-f00a-7716-918c-0ca1ae929b64','安全审计','019fdba9-f00a-7716-918c-0ca1ae929b62','icon-security','MENU','SystemSecurityAudit',1,NULL,CURRENT_TIMESTAMP,NULL,CURRENT_TIMESTAMP,NULL,0)
ON CONFLICT (id) DO NOTHING;

CREATE TABLE IF NOT EXISTS spectra_security.security_audit_retention_policy (
    policy_key             VARCHAR(64) PRIMARY KEY,
    hot_retention_months   INTEGER NOT NULL DEFAULT 12,
    total_retention_years  INTEGER NOT NULL DEFAULT 5,
    archive_backend        VARCHAR(64) NOT NULL DEFAULT 'PENDING',
    state                  VARCHAR(32) NOT NULL DEFAULT 'ACTIVE',
    version                BIGINT NOT NULL DEFAULT 0,
    created_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at             TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_security_audit_hot_retention CHECK (hot_retention_months >= 12),
    CONSTRAINT ck_security_audit_total_retention CHECK (total_retention_years >= 5),
    CONSTRAINT ck_security_audit_retention_state CHECK (state IN ('ACTIVE', 'PAUSED'))
);

INSERT INTO spectra_security.security_audit_retention_policy
    (policy_key, hot_retention_months, total_retention_years, archive_backend, state)
VALUES ('DEFAULT', 12, 5, 'PENDING', 'ACTIVE')
ON CONFLICT (policy_key) DO NOTHING;

CREATE TABLE IF NOT EXISTS spectra_security.security_audit_archive_manifest (
    manifest_id       UUID PRIMARY KEY,
    partition_name    VARCHAR(128) NOT NULL UNIQUE,
    range_start       TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    range_end         TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    object_uri        VARCHAR(1000),
    content_sha256    CHAR(64),
    row_count         BIGINT,
    state             VARCHAR(32) NOT NULL DEFAULT 'PLANNED',
    archived_at       TIMESTAMP(6) WITH TIME ZONE,
    verified_at       TIMESTAMP(6) WITH TIME ZONE,
    last_error        VARCHAR(2000),
    created_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT ck_security_audit_archive_range CHECK (range_end > range_start),
    CONSTRAINT ck_security_audit_archive_state CHECK (state IN ('PLANNED', 'ARCHIVED', 'VERIFIED', 'RESTORE_PENDING', 'RESTORED', 'FAILED')),
    CONSTRAINT ck_security_audit_archive_hash CHECK (content_sha256 IS NULL OR content_sha256 ~ '^[0-9a-fA-F]{64}$')
);

CREATE INDEX IF NOT EXISTS idx_security_audit_archive_manifest_range
    ON spectra_security.security_audit_archive_manifest (range_start, range_end);
CREATE INDEX IF NOT EXISTS idx_security_audit_archive_manifest_state
    ON spectra_security.security_audit_archive_manifest (state, updated_at DESC);

COMMENT ON TABLE spectra_security.security_audit_retention_policy IS '安全审计热存/总保留策略，只读展示，变更需审计运维流程';
COMMENT ON TABLE spectra_security.security_audit_archive_manifest IS '安全审计分区归档、校验、恢复清单，不代表可删除审计事实';
COMMENT ON COLUMN spectra_security.security_audit_archive_manifest.content_sha256 IS '归档对象完整性校验摘要';

GRANT SELECT ON spectra_security.security_audit_retention_policy TO spectra_runtime;
GRANT SELECT ON spectra_security.security_audit_archive_manifest TO spectra_runtime;
REVOKE INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER
    ON spectra_security.security_audit_retention_policy FROM spectra_runtime;
REVOKE INSERT, UPDATE, DELETE, TRUNCATE, REFERENCES, TRIGGER
    ON spectra_security.security_audit_archive_manifest FROM spectra_runtime;
