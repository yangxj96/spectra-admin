-- Register the unified file upload console under Devops > System Maintenance.
-- The upload page is a static Web route; the menu record supplies navigation
-- visibility and the route guard's requiredMenu authorization contract.

UPDATE spectra_core.sys_menu
SET sort = sort + 1
WHERE pid = '019fdba9-f00a-7716-918c-0ca1ae929b69'::uuid
  AND route_name IN ('DevopsStorage', 'DevopsCacheClear', 'DevopsEncryptionKey')
  AND deleted IS NULL;

INSERT INTO spectra_core.sys_menu (
    id,
    name,
    pid,
    icon,
    menu_type,
    route_name,
    sort,
    created_by,
    created_at,
    updated_by,
    updated_at,
    deleted,
    version
)
SELECT
    '019fdba9-f00a-7716-918c-0ca1ae929b83'::uuid,
    '文件上传',
    '019fdba9-f00a-7716-918c-0ca1ae929b69'::uuid,
    'icon-module',
    'MENU',
    'DevopsFileUpload',
    1,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000000'::uuid,
    CURRENT_TIMESTAMP,
    NULL,
    0
WHERE NOT EXISTS (
    SELECT 1
    FROM spectra_core.sys_menu
    WHERE route_name = 'DevopsFileUpload'
      AND deleted IS NULL
);
