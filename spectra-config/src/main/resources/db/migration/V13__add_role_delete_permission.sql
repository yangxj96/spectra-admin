-- 将角色状态管理与角色逻辑删除拆分为独立权限。
INSERT INTO spectra_security.sec_permission
    (id, code, name, resource_code, action_code, allowed_scope_modes, state, system_managed,
     created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    ('c14d04e6-0d37-4c03-b5dd-80de3a2fcb31'::uuid, 'role:delete', '角色删除', 'role', 'delete', 'NONE', 'ACTIVE', false,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0)
ON CONFLICT (code) DO UPDATE
SET name = EXCLUDED.name,
    resource_code = EXCLUDED.resource_code,
    action_code = EXCLUDED.action_code,
    allowed_scope_modes = EXCLUDED.allowed_scope_modes,
    state = EXCLUDED.state,
    system_managed = EXCLUDED.system_managed,
    deleted = NULL,
    updated_at = CURRENT_TIMESTAMP;

-- 已具备角色停用权限的角色同步获得角色删除权限，避免升级后已有管理员无法使用新增操作。
INSERT INTO spectra_security.sec_role_permission
    (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, deleted, version)
SELECT gen_random_uuid(),
       role_id,
       (SELECT id FROM spectra_security.sec_permission WHERE code = 'role:delete'),
       '00000000-0000-0000-0000-000000000000'::uuid,
       CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000'::uuid,
       CURRENT_TIMESTAMP,
       NULL,
       0
FROM spectra_security.sec_role_permission
WHERE permission_id = (SELECT id FROM spectra_security.sec_permission WHERE code = 'role:disable')
  AND deleted IS NULL
ON CONFLICT (role_id, permission_id) DO UPDATE
SET deleted = NULL,
    updated_at = CURRENT_TIMESTAMP;
