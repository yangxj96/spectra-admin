-- 使用 IconPicker 已注册的图标修复安全运维菜单图标。
UPDATE spectra_core.sys_menu
SET icon = CASE id
    WHEN '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid THEN 'icon-setting'
    WHEN '019fdba9-f00a-7716-918c-0ca1ae929b63'::uuid THEN 'icon-setting-role'
    WHEN '019fdba9-f00a-7716-918c-0ca1ae929b64'::uuid THEN 'icon-log'
    ELSE icon
END
WHERE id IN (
    '019fdba9-f00a-7716-918c-0ca1ae929b62'::uuid,
    '019fdba9-f00a-7716-918c-0ca1ae929b63'::uuid,
    '019fdba9-f00a-7716-918c-0ca1ae929b64'::uuid
) AND icon = 'icon-security';
