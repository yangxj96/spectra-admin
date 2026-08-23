-- 修复 V23/V25 复用访问控制菜单 UUID 导致的系统管理菜单层级错误。
-- 保留原角色管理菜单 ID 及已有授权，只为访问控制目录和授权方案分配新的 UUID。

INSERT INTO spectra_core.sys_menu
    (id, name, pid, icon, menu_type, route_name, sort, created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    ('019fdba9-f00a-7716-918c-0ca1ae929b80'::uuid, '访问控制',
     '019bdfc5-b32c-74e9-90ac-0540954c4e4a'::uuid, 'icon-setting-role', 'DIRECTORY', NULL, 1,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    pid = EXCLUDED.pid,
    icon = EXCLUDED.icon,
    menu_type = EXCLUDED.menu_type,
    route_name = EXCLUDED.route_name,
    sort = EXCLUDED.sort,
    deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = spectra_core.sys_menu.version + 1;

UPDATE spectra_core.sys_menu
SET name = '角色管理',
    pid = '019fdba9-f00a-7716-918c-0ca1ae929b80'::uuid,
    icon = 'icon-module',
    menu_type = 'MENU',
    route_name = 'SystemRoleManagement',
    sort = 0,
    deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE id = '019bdfc5-b370-70ca-a33c-25044878eeda'::uuid;

INSERT INTO spectra_core.sys_menu
    (id, name, pid, icon, menu_type, route_name, sort, created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    ('019fdba9-f00a-7716-918c-0ca1ae929b82'::uuid, '授权方案',
     '019fdba9-f00a-7716-918c-0ca1ae929b80'::uuid, 'icon-module', 'MENU', 'SystemAuthorizationProfiles', 1,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
     '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    pid = EXCLUDED.pid,
    icon = EXCLUDED.icon,
    menu_type = EXCLUDED.menu_type,
    route_name = EXCLUDED.route_name,
    sort = EXCLUDED.sort,
    deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = spectra_core.sys_menu.version + 1;

-- 原来拥有角色管理菜单的角色继续保留角色管理，并补齐授权方案入口。
INSERT INTO spectra_security.sec_role_menu
    (id, role_id, menu_id, created_by, created_at, updated_by, updated_at, deleted, version)
SELECT gen_random_uuid(), role_menu.role_id,
       '019fdba9-f00a-7716-918c-0ca1ae929b82'::uuid,
       '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0
FROM spectra_security.sec_role_menu role_menu
JOIN spectra_security.sec_role role ON role.id = role_menu.role_id
WHERE role_menu.menu_id = '019bdfc5-b370-70ca-a33c-25044878eeda'::uuid
  AND role_menu.deleted IS NULL
  AND role.deleted IS NULL
ON CONFLICT (role_id, menu_id) DO UPDATE
SET deleted = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = spectra_security.sec_role_menu.version + 1;
