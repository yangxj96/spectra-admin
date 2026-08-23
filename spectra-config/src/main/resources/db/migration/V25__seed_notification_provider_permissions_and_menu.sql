-- 通知 Provider 配置权限和运维菜单。

INSERT INTO spectra_security.sec_permission
    (id, code, name, resource_code, action_code, allowed_scope_modes, state, system_managed,
     created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    (gen_random_uuid(), 'notification:provider:read', '通知 Provider 查看', 'notification:provider', 'read', 'NONE', 'ACTIVE', true,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0),
    (gen_random_uuid(), 'notification:provider:configure', '通知 Provider 配置', 'notification:provider', 'configure', 'NONE', 'ACTIVE', true,
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
  ON permission.code IN ('notification:provider:read', 'notification:provider:configure')
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
  ON permission.code = 'notification:provider:read'
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
    ('019fdba9-f00a-7716-918c-0ca1ae929b76'::uuid, '渠道配置', '019fdba9-f00a-7716-918c-0ca1ae929b67'::uuid,
     'icon-setting', 'MENU', 'DevopsNotificationProvider', 5,
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
JOIN spectra_core.sys_menu menu ON menu.route_name = 'DevopsNotificationProvider'
WHERE role.code IN ('ROLE_DEV_OPS', 'ROLE_AUDIT')
  AND role.deleted IS NULL
  AND menu.deleted IS NULL
ON CONFLICT (role_id, menu_id) DO UPDATE
SET deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = spectra_security.sec_role_menu.version + 1;
