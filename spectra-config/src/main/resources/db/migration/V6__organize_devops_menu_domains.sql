-- 将现有监控、系统维护和安全菜单整理为运维管理下的三级菜单，
-- 并预定义后续通知、调度、审计和安全运维功能。

INSERT INTO spectra_core.sys_menu
    (id, name, pid, icon, menu_type, route_name, sort, created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    ('019fdba9-f00a-7716-918c-0ca1ae929b66'::uuid, '运行监控', '019fdba9-f00a-7716-918c-0ca1ae929b65'::uuid,
     'icon-setting', 'DIRECTORY', NULL, 0,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b67'::uuid, '通知中心', '019fdba9-f00a-7716-918c-0ca1ae929b65'::uuid,
     'icon-setting', 'DIRECTORY', NULL, 1,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b68'::uuid, '任务调度', '019fdba9-f00a-7716-918c-0ca1ae929b65'::uuid,
     'icon-setting', 'DIRECTORY', NULL, 2,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b69'::uuid, '系统维护', '019fdba9-f00a-7716-918c-0ca1ae929b65'::uuid,
     'icon-setting', 'DIRECTORY', NULL, 3,
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
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b66'::uuid,
    route_name = 'DevopsMonitorServer',
    sort = 0,
    deleted = NULL
WHERE id = '019bdfc5-b34d-74fd-8ad8-f2f7976634d1'::uuid;

UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b66'::uuid,
    route_name = 'DevopsMonitorCache',
    sort = 1,
    deleted = NULL
WHERE id = '019bdfc5-b355-701e-99f2-7012b17490de'::uuid;

UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b68'::uuid,
    route_name = 'DevopsSchedulerTask',
    sort = 0,
    deleted = NULL
WHERE id = '019bdfc5-b352-7d24-b5af-8d0a0042a4f9'::uuid;

UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b69'::uuid,
    route_name = 'DevopsConfigured',
    sort = 0,
    deleted = NULL
WHERE id = '019bdfc5-b363-750f-8cd2-010b659463a8'::uuid;

UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b69'::uuid,
    route_name = 'DevopsStorage',
    sort = 1,
    deleted = NULL
WHERE id = '019bdfc5-b35f-74b6-abe3-3816db511129'::uuid;

UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid,
    route_name = 'DevopsSecurityOnline',
    sort = 2,
    deleted = NULL
WHERE id = '019bdfc5-b350-7168-84d6-ffaaf874b6fc'::uuid;

UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b65'::uuid,
    sort = 4,
    deleted = NULL
WHERE id = '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid;

UPDATE spectra_core.sys_menu
SET deleted = COALESCE(deleted, CURRENT_TIMESTAMP)
WHERE id = '019bdfc5-b328-7de0-9e8c-2ac0cc51969e'::uuid;

UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid,
    sort = 0,
    deleted = NULL
WHERE id = '019fdba9-f00a-7716-918c-0ca1ae929b63'::uuid;

UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid,
    sort = 1,
    deleted = NULL
WHERE id = '019fdba9-f00a-7716-918c-0ca1ae929b64'::uuid;

INSERT INTO spectra_core.sys_menu
    (id, name, pid, icon, menu_type, route_name, sort, created_by, created_at, updated_by, updated_at, deleted, version)
VALUES
    ('019fdba9-f00a-7716-918c-0ca1ae929b6a'::uuid, '应用健康检查', '019fdba9-f00a-7716-918c-0ca1ae929b66'::uuid,
     'icon-module', 'MENU', 'DevopsApplicationHealth', 2,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b6b'::uuid, '通知运行概览', '019fdba9-f00a-7716-918c-0ca1ae929b67'::uuid,
     'icon-module', 'MENU', 'DevopsNotificationOverview', 0,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b6c'::uuid, '通知请求', '019fdba9-f00a-7716-918c-0ca1ae929b67'::uuid,
     'icon-module', 'MENU', 'DevopsNotificationRequest', 1,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b6d'::uuid, '投递任务', '019fdba9-f00a-7716-918c-0ca1ae929b67'::uuid,
     'icon-module', 'MENU', 'DevopsNotificationDeliveryTask', 2,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b6e'::uuid, '投递记录', '019fdba9-f00a-7716-918c-0ca1ae929b67'::uuid,
     'icon-module', 'MENU', 'DevopsNotificationDeliveryRecord', 3,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b6f'::uuid, '调度执行记录', '019fdba9-f00a-7716-918c-0ca1ae929b68'::uuid,
     'icon-module', 'MENU', 'DevopsSchedulerExecution', 1,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b70'::uuid, '缓存清理', '019fdba9-f00a-7716-918c-0ca1ae929b69'::uuid,
     'icon-module', 'MENU', 'DevopsCacheClear', 2,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b71'::uuid, '操作日志', '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid,
     'icon-log', 'MENU', 'DevopsOperationLog', 3,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b72'::uuid, '安全事件', '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid,
     'icon-security', 'MENU', 'DevopsSecurityEvent', 4,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b73'::uuid, '加密密钥', '019fdba9-f00a-7716-918c-0ca1ae929b69'::uuid,
     'icon-setting-role', 'MENU', 'DevopsEncryptionKey', 3,
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000'::uuid, TIMESTAMPTZ '1996-10-15 00:00:00+08:00', NULL, 0),
    ('019fdba9-f00a-7716-918c-0ca1ae929b74'::uuid, '会话踢出', '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid,
     'icon-security', 'MENU', 'DevopsSessionKick', 5,
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
