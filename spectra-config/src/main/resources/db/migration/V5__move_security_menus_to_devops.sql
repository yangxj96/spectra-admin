-- 新增运维管理顶级菜单，并将安全运维、系统配置迁移到该菜单下。
INSERT INTO spectra_core.sys_menu
    (id, name, pid, icon, menu_type, route_name, sort, created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    ('019fdba9-f00a-7716-918c-0ca1ae929b65'::uuid, '运维管理', NULL, 'icon-setting', 'DIRECTORY', NULL, 998,
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
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b65'::uuid,
    sort = 0
WHERE id = '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid;

UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b65'::uuid,
    route_name = 'DevopsConfigured',
    sort = 1
WHERE id = '019bdfc5-b363-750f-8cd2-010b659463a8'::uuid;

UPDATE spectra_core.sys_menu
SET icon = 'icon-setting',
    route_name = NULL
WHERE id = '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid;

UPDATE spectra_core.sys_menu
SET icon = 'icon-setting-role',
    route_name = 'DevopsSecurityContext'
WHERE id = '019fdba9-f00a-7716-918c-0ca1ae929b63'::uuid;

UPDATE spectra_core.sys_menu
SET icon = 'icon-log',
    route_name = 'DevopsSecurityAudit'
WHERE id = '019fdba9-f00a-7716-918c-0ca1ae929b64'::uuid;
