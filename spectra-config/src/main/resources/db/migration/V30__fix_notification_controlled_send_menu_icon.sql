-- 修正受控发送菜单图标，使用前端已有的图标资源。
UPDATE spectra_core.sys_menu
SET icon = 'icon-module',
    updated_at = CURRENT_TIMESTAMP,
    version = version + 1
WHERE route_name = 'DevopsNotificationSend'
  AND deleted IS NULL;
