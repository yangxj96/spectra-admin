-- 使用 IconPicker 已注册的图标修复新增安全运维菜单图标。
UPDATE spectra_core.sys_menu
SET icon = 'icon-log'
WHERE id = '019fdba9-f00a-7716-918c-0ca1ae929b72'::uuid;

UPDATE spectra_core.sys_menu
SET icon = 'icon-disable'
WHERE id = '019fdba9-f00a-7716-918c-0ca1ae929b74'::uuid;
