-- 将系统管理中的访问控制调整为目录，并拆分为角色管理和授权方案两个子菜单。
-- 保留原访问控制菜单 ID 作为角色管理菜单 ID，避免已有角色菜单授权失效。

INSERT INTO spectra_core.sys_menu
    (id, name, pid, icon, menu_type, route_name, sort, created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    ('019fdba9-f00a-7716-918c-0ca1ae929b75'::uuid, '访问控制',
     '019bdfc5-b32c-74e9-90ac-0540954c4e4a'::uuid, 'icon-setting-role', 'DIRECTORY', NULL, 1,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    pid = EXCLUDED.pid,
    icon = EXCLUDED.icon,
    menu_type = EXCLUDED.menu_type,
    route_name = EXCLUDED.route_name,
    sort = EXCLUDED.sort,
    deleted = NULL;

UPDATE spectra_core.sys_menu
SET name = '角色管理',
    pid = '019fdba9-f00a-7716-918c-0ca1ae929b75'::uuid,
    icon = 'icon-module',
    menu_type = 'MENU',
    route_name = 'SystemRoleManagement',
    sort = 0,
    deleted = NULL
WHERE id = '019bdfc5-b370-70ca-a33c-25044878eeda'::uuid;

INSERT INTO spectra_core.sys_menu
    (id, name, pid, icon, menu_type, route_name, sort, created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    ('019fdba9-f00a-7716-918c-0ca1ae929b76'::uuid, '授权方案',
     '019fdba9-f00a-7716-918c-0ca1ae929b75'::uuid, 'icon-module', 'MENU', 'SystemAuthorizationProfiles', 1,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0)
ON CONFLICT (id) DO UPDATE
SET name = EXCLUDED.name,
    pid = EXCLUDED.pid,
    icon = EXCLUDED.icon,
    menu_type = EXCLUDED.menu_type,
    route_name = EXCLUDED.route_name,
    sort = EXCLUDED.sort,
    deleted = NULL;

-- 原来能看到访问控制的角色继续保留角色管理，并新增授权方案入口。
INSERT INTO spectra_security.sec_role_menu
    (role_id, menu_id, created_by, created_at, updated_by, updated_at, deleted, version)
SELECT role_id,
       '019fdba9-f00a-7716-918c-0ca1ae929b76'::uuid,
       '00000000-0000-0000-0000-000000000000'::uuid,
       CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000'::uuid,
       CURRENT_TIMESTAMP,
       NULL,
       0
FROM spectra_security.sec_role_menu
WHERE menu_id = '019bdfc5-b370-70ca-a33c-25044878eeda'::uuid
  AND deleted IS NULL
ON CONFLICT (role_id, menu_id) DO UPDATE
SET deleted = NULL,
    updated_at = CURRENT_TIMESTAMP;
