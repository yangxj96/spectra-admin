/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

-- 文件管理后台一次性补齐：任务、资产、引用和文件类型策略统一归入独立目录。

INSERT INTO spectra_security.sec_permission
    (id, code, name, resource_code, action_code, allowed_scope_modes, state, system_managed)
VALUES
    ('00000000-0000-0000-0000-000000000431', 'file:admin:manage', '文件管理操作',
     'file:admin', 'manage', 'NONE', 'ACTIVE', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO spectra_security.sec_role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM spectra_security.sec_role role
JOIN spectra_security.sec_permission permission ON permission.code = 'file:admin:manage'
WHERE role.code IN ('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS')
ON CONFLICT (role_id, permission_id) DO NOTHING;

-- 文件管理目录与系统维护同级；DevopsFileManagement 是该目录的稳定代码标识，目录本身不绑定路由。
UPDATE spectra_core.sys_menu
SET sort = sort + 1,
    updated_at = CURRENT_TIMESTAMP
WHERE pid = '019fdba9-f00a-7716-918c-0ca1ae929b65'::uuid
  AND sort >= 4
  AND deleted IS NULL;

INSERT INTO spectra_core.sys_menu (
    id, name, pid, icon, menu_type, route_name, sort,
    created_by, created_at, updated_by, updated_at, deleted, version
)
SELECT
    '019fdba9-f00a-7716-918c-0ca1ae929b84'::uuid,
    '文件管理',
    '019fdba9-f00a-7716-918c-0ca1ae929b65'::uuid,
    'icon-setting',
    'DIRECTORY',
    NULL,
    4,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    NULL,
    0
WHERE NOT EXISTS (
    SELECT 1 FROM spectra_core.sys_menu
    WHERE id = '019fdba9-f00a-7716-918c-0ca1ae929b84'::uuid AND deleted IS NULL
);

-- 现有上传与资产页面迁移到新目录，不保留旧的系统维护路径别名。
UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b84'::uuid,
    name = '文件上传',
    sort = 0,
    updated_at = CURRENT_TIMESTAMP
WHERE route_name = 'DevopsFileUpload' AND deleted IS NULL;

UPDATE spectra_core.sys_menu
SET pid = '019fdba9-f00a-7716-918c-0ca1ae929b84'::uuid,
    name = '文件资产',
    sort = 1,
    updated_at = CURRENT_TIMESTAMP
WHERE route_name = 'DevopsStorage' AND deleted IS NULL;

INSERT INTO spectra_core.sys_menu (
    id, name, pid, icon, menu_type, route_name, sort,
    created_by, created_at, updated_by, updated_at, deleted, version
)
SELECT
    item.id, item.name, '019fdba9-f00a-7716-918c-0ca1ae929b84'::uuid,
    item.icon, 'MENU', item.route_name, item.sort,
    '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000000'::uuid, CURRENT_TIMESTAMP, NULL, 0
FROM (VALUES
    ('019fdba9-f00a-7716-918c-0ca1ae929b85'::uuid, '上传任务', 'icon-module', 'DevopsUploadTasks', 2),
    ('019fdba9-f00a-7716-918c-0ca1ae929b86'::uuid, '文件引用', 'icon-module', 'DevopsFileReferences', 3),
    ('019fdba9-f00a-7716-918c-0ca1ae929b87'::uuid, '文件类型策略', 'icon-setting', 'DevopsFileTypes', 4)
) AS item(id, name, icon, route_name, sort)
WHERE NOT EXISTS (
    SELECT 1 FROM spectra_core.sys_menu existing
    WHERE existing.route_name = item.route_name AND existing.deleted IS NULL
);

-- 新页面关系必须在菜单迁移之后建立，确保普通系统管理员可见完整文件管理目录。
INSERT INTO spectra_security.sec_role_menu (role_id, menu_id)
SELECT role.id, menu.id
FROM spectra_security.sec_role role
JOIN spectra_core.sys_menu menu ON menu.route_name IN (
    'DevopsFileUpload', 'DevopsStorage', 'DevopsUploadTasks', 'DevopsFileReferences', 'DevopsFileTypes')
WHERE role.code IN ('ROLE_ADMIN_SYSTEM', 'ROLE_DEV_OPS')
  AND menu.deleted IS NULL
ON CONFLICT (role_id, menu_id) DO NOTHING;
