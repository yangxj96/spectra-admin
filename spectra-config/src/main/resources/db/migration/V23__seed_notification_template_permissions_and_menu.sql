-- 通知模板管理的最终权限和运维菜单。

INSERT INTO spectra_security.sec_permission
    (id, code, name, resource_code, action_code, allowed_scope_modes, state, system_managed,
     created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    (gen_random_uuid(), 'notification:template:read', '通知模板查看', 'notification:template', 'read', 'NONE', 'ACTIVE', true,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0),
    (gen_random_uuid(), 'notification:template:write', '通知模板维护', 'notification:template', 'write', 'NONE', 'ACTIVE', true,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0),
    (gen_random_uuid(), 'notification:template:publish', '通知模板发布与回滚', 'notification:template', 'publish', 'NONE', 'ACTIVE', true,
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

INSERT INTO spectra_security.sec_role_permission
    (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, deleted, version)
SELECT gen_random_uuid(), role.id, permission.id,
       '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0
FROM spectra_security.sec_role role
JOIN spectra_security.sec_permission permission
  ON permission.code IN ('notification:template:read', 'notification:template:write', 'notification:template:publish')
WHERE role.code = 'ROLE_DEV_OPS'
  AND role.deleted IS NULL
  AND permission.deleted IS NULL
ON CONFLICT (role_id, permission_id) DO UPDATE
SET deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = spectra_security.sec_role_permission.version + 1;

INSERT INTO spectra_security.sec_role_permission
    (id, role_id, permission_id, created_by, created_at, updated_by, updated_at, deleted, version)
SELECT gen_random_uuid(), role.id, permission.id,
       '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0
FROM spectra_security.sec_role role
JOIN spectra_security.sec_permission permission
  ON permission.code = 'notification:template:read'
WHERE role.code = 'ROLE_AUDIT'
  AND role.deleted IS NULL
  AND permission.deleted IS NULL
ON CONFLICT (role_id, permission_id) DO UPDATE
SET deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = spectra_security.sec_role_permission.version + 1;

INSERT INTO spectra_core.sys_menu
    (id, name, pid, icon, menu_type, route_name, sort, created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    ('019fdba9-f00a-7716-918c-0ca1ae929b75'::uuid, '模板管理', '019fdba9-f00a-7716-918c-0ca1ae929b67'::uuid,
     'icon-module', 'MENU', 'DevopsNotificationTemplate', 4,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    pid = EXCLUDED.pid,
    icon = EXCLUDED.icon,
    menu_type = EXCLUDED.menu_type,
    route_name = EXCLUDED.route_name,
    sort = EXCLUDED.sort,
    deleted = NULL;

INSERT INTO spectra_security.sec_role_menu
    (id, role_id, menu_id, created_by, created_at, updated_by, updated_at, deleted, version)
SELECT gen_random_uuid(), role.id, menu.id,
       '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0
FROM spectra_security.sec_role role
JOIN spectra_core.sys_menu menu ON menu.route_name = 'DevopsNotificationTemplate'
WHERE role.code IN ('ROLE_DEV_OPS', 'ROLE_AUDIT')
  AND role.deleted IS NULL
  AND menu.deleted IS NULL
ON CONFLICT (role_id, menu_id) DO UPDATE
SET deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = spectra_security.sec_role_menu.version + 1;
