-- 可复用授权方案只保存 Role/Permission/部门业务编码和版本快照，
-- 应用时仍必须通过 RoleAssignment Preview/Apply 和 Grant Boundary 校验。

CREATE TABLE spectra_security.sec_authorization_profile (
    id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    code        VARCHAR(80) NOT NULL,
    name        VARCHAR(120) NOT NULL,
    description VARCHAR(500),
    state       VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_sec_authorization_profile_code CHECK (code ~ '^PROFILE_[A-Z0-9_]+$'),
    CONSTRAINT ck_sec_authorization_profile_state CHECK (state IN ('ACTIVE', 'DISABLED'))
);

CREATE UNIQUE INDEX uk_sec_authorization_profile_code
    ON spectra_security.sec_authorization_profile (code)
    WHERE deleted IS NULL;

CREATE TABLE spectra_security.sec_authorization_profile_assignment (
    id          UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    profile_id  UUID NOT NULL REFERENCES spectra_security.sec_authorization_profile (id) ON DELETE CASCADE,
    role_code   VARCHAR(80) NOT NULL,
    role_version BIGINT NOT NULL,
    created_by  UUID,
    created_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by  UUID,
    updated_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted     TIMESTAMP(6) WITH TIME ZONE,
    version     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_sec_authorization_profile_assignment_role_version CHECK (role_version >= 0)
);

CREATE UNIQUE INDEX uk_sec_authorization_profile_assignment_role
    ON spectra_security.sec_authorization_profile_assignment (profile_id, role_code)
    WHERE deleted IS NULL;

CREATE TABLE spectra_security.sec_authorization_profile_boundary (
    id                    UUID DEFAULT gen_random_uuid() PRIMARY KEY,
    profile_assignment_id UUID NOT NULL REFERENCES spectra_security.sec_authorization_profile_assignment (id) ON DELETE CASCADE,
    permission_code       VARCHAR(120) NOT NULL,
    access_scope           JSONB NOT NULL,
    grant_scope            JSONB,
    created_by            UUID,
    created_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by            UUID,
    updated_at            TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted               TIMESTAMP(6) WITH TIME ZONE,
    version               BIGINT NOT NULL DEFAULT 0
);

CREATE UNIQUE INDEX uk_sec_authorization_profile_boundary_permission
    ON spectra_security.sec_authorization_profile_boundary (profile_assignment_id, permission_code)
    WHERE deleted IS NULL;

CREATE INDEX idx_sec_authorization_profile_assignment_profile
    ON spectra_security.sec_authorization_profile_assignment (profile_id)
    WHERE deleted IS NULL;

CREATE INDEX idx_sec_authorization_profile_boundary_assignment
    ON spectra_security.sec_authorization_profile_boundary (profile_assignment_id)
    WHERE deleted IS NULL;

COMMENT ON TABLE spectra_security.sec_authorization_profile IS '可复用授权方案元数据';
COMMENT ON TABLE spectra_security.sec_authorization_profile_assignment IS '授权方案中的 Role 版本快照';
COMMENT ON TABLE spectra_security.sec_authorization_profile_boundary IS '授权方案中的 Permission-specific Access/Grant Boundary';
COMMENT ON COLUMN spectra_security.sec_authorization_profile.code IS '稳定授权方案编码';
COMMENT ON COLUMN spectra_security.sec_authorization_profile.state IS '方案状态：ACTIVE/DISABLED';
COMMENT ON COLUMN spectra_security.sec_authorization_profile.version IS '方案版本，应用前用于并发校验';
COMMENT ON COLUMN spectra_security.sec_authorization_profile_assignment.role_code IS '稳定 Role 编码，不保存运行时 Role UUID';
COMMENT ON COLUMN spectra_security.sec_authorization_profile_assignment.role_version IS '保存方案时的 Role version 快照';
COMMENT ON COLUMN spectra_security.sec_authorization_profile_boundary.permission_code IS '稳定 Permission 编码';
COMMENT ON COLUMN spectra_security.sec_authorization_profile_boundary.access_scope IS 'Access Boundary，部门使用业务编码';
COMMENT ON COLUMN spectra_security.sec_authorization_profile_boundary.grant_scope IS '可选 Grant Boundary，部门使用业务编码';
