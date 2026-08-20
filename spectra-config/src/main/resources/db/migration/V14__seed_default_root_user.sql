-- Immutable security seed for a newly initialized installation.
--
-- ROLE_DEV_OPS is a capability definition, not an account.  The first user,
-- password credential, MFA enrollment and role assignment are created by the
-- anonymous system-initialization flow after the operator supplies them.

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
