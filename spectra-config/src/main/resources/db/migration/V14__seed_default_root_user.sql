-- Default bootstrap identity for a newly initialized installation.
--
-- The password is stored only as a BCrypt hash.  The bootstrap credential is
-- intentionally not copied into documentation or application configuration;
-- operators must rotate it after completing MFA enrollment.

INSERT INTO spectra_security.role (
    id,
    code,
    name,
    authority_level,
    state,
    role_kind,
    system_managed,
    remark
)
VALUES (
    md5('seed:role:ROLE_DEV_OPS')::uuid,
    'ROLE_DEV_OPS',
    'Root 运维管理员',
    1000,
    'ACTIVE',
    'DEV_OPS',
    TRUE,
    '初始化默认 Root；高风险授权仍受审计与治理流程约束'
)
ON CONFLICT (code) DO NOTHING;

INSERT INTO spectra_core.sys_user (
    id,
    username,
    status,
    real_name,
    email,
    language,
    timezone,
    security_version,
    created_at,
    updated_at,
    version
)
VALUES (
    md5('seed:user:devops00.com')::uuid,
    'devops00.com',
    'ACTIVE',
    'Root 运维管理员',
    'devops00.com',
    'zh-CN',
    'Asia/Shanghai',
    0,
    CURRENT_TIMESTAMP,
    CURRENT_TIMESTAMP,
    0
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO spectra_security.authentication_identity (
    id,
    user_id,
    method_code,
    provider_code,
    identifier_hash,
    state,
    verified_at,
    version
)
VALUES (
    md5('seed:identity:PASSWORD:LOCAL:devops00.com')::uuid,
    md5('seed:user:devops00.com')::uuid,
    'PASSWORD',
    'LOCAL',
    '06b2237e978cae1d35d16b65f28c60d6fa0bb034fc13ade5fc70b7de1395f3d9',
    'ACTIVE',
    CURRENT_TIMESTAMP,
    0
)
ON CONFLICT (method_code, provider_code, identifier_hash) DO NOTHING;

INSERT INTO spectra_security.password_credential (
    user_id,
    password_hash,
    changed_at,
    must_change,
    failed_attempts,
    version
)
VALUES (
    md5('seed:user:devops00.com')::uuid,
    '$2a$10$8Ea.jILAOTGhzY/ofUGXCOyD2WTL32MiWkp06axficaFZkZ9QX42C',
    CURRENT_TIMESTAMP,
    FALSE,
    0,
    0
)
ON CONFLICT (user_id) DO NOTHING;

INSERT INTO spectra_security.role_assignment (
    id,
    user_id,
    role_id,
    state,
    assigned_at,
    version
)
VALUES (
    md5('seed:assignment:devops00.com:ROLE_DEV_OPS')::uuid,
    md5('seed:user:devops00.com')::uuid,
    md5('seed:role:ROLE_DEV_OPS')::uuid,
    'ACTIVE',
    CURRENT_TIMESTAMP,
    0
)
ON CONFLICT (id) DO NOTHING;
