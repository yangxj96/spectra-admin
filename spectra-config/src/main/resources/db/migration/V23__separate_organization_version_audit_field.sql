-- Development refactor: the organization tree counter is a business field.
-- It must not share the public audit column name `version` inherited by every
-- BaseEntity.  Rebuild the singleton so its order remains canonical.

CREATE TABLE spectra_core.__canonical_sys_organization_version (
    id                  UUID NOT NULL DEFAULT uuidv7(),
    singleton_key       VARCHAR(16) NOT NULL DEFAULT 'SYSTEM',
    organization_version BIGINT NOT NULL DEFAULT 0,
    changed_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_by          UUID,
    created_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID,
    updated_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             TIMESTAMP(6) WITH TIME ZONE,
    version             BIGINT NOT NULL DEFAULT 0
);

INSERT INTO spectra_core.__canonical_sys_organization_version (
    id, singleton_key, organization_version, changed_at,
    created_by, created_at, updated_by, updated_at, deleted, version
)
SELECT id, singleton_key, version, changed_at,
       created_by, created_at, updated_by, updated_at, deleted, 0
FROM spectra_core.sys_organization_version;

DROP TABLE spectra_core.sys_organization_version CASCADE;

ALTER TABLE spectra_core.__canonical_sys_organization_version
    RENAME TO sys_organization_version;

ALTER TABLE spectra_core.sys_organization_version
    ADD CONSTRAINT pk_sys_organization_version PRIMARY KEY (id),
    ADD CONSTRAINT uk_sys_organization_version_key UNIQUE (singleton_key),
    ADD CONSTRAINT ck_sys_organization_version_key CHECK (singleton_key = 'SYSTEM'),
    ADD CONSTRAINT ck_sys_organization_version_value CHECK (organization_version >= 0);

COMMENT ON TABLE spectra_core.sys_organization_version IS '组织树安全版本';
COMMENT ON COLUMN spectra_core.sys_organization_version.id IS '技术主键';
COMMENT ON COLUMN spectra_core.sys_organization_version.singleton_key IS '单例键，固定为 SYSTEM';
COMMENT ON COLUMN spectra_core.sys_organization_version.organization_version IS '组织树安全版本';
COMMENT ON COLUMN spectra_core.sys_organization_version.changed_at IS '组织树版本变更时间';

GRANT SELECT, INSERT, UPDATE, DELETE ON spectra_core.sys_organization_version TO spectra_runtime;
