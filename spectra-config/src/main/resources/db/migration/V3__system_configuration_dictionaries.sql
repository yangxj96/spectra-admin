-- 仅更新系统配置表中的控件类型和字典编码，语言与时区选项复用已有字典数据。
UPDATE spectra_core.sys_config
SET type = 2,
    dict_code = 'sys_language',
    updated_at = CURRENT_TIMESTAMP,
    version = COALESCE(version, 0) + 1
WHERE key = 'system.default-locale'
  AND deleted IS NULL
  AND (type IS DISTINCT FROM 2 OR dict_code IS DISTINCT FROM 'sys_language');

UPDATE spectra_core.sys_config
SET type = 2,
    dict_code = 'sys_timezone',
    updated_at = CURRENT_TIMESTAMP,
    version = COALESCE(version, 0) + 1
WHERE key = 'system.default-timezone'
  AND deleted IS NULL
  AND (type IS DISTINCT FROM 2 OR dict_code IS DISTINCT FROM 'sys_timezone');

UPDATE spectra_core.sys_config
SET type = 0,
    dict_code = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = COALESCE(version, 0) + 1
WHERE key IN ('system.name', 'system.short-name', 'system.logo', 'copyright.name', 'copyright.url')
  AND deleted IS NULL
  AND (type IS DISTINCT FROM 0 OR dict_code IS NOT NULL);

UPDATE spectra_core.sys_config
SET type = 4,
    dict_code = NULL,
    updated_at = CURRENT_TIMESTAMP,
    version = COALESCE(version, 0) + 1
WHERE key = 'notification.allowed-link-prefixes'
  AND deleted IS NULL
  AND (type IS DISTINCT FROM 4 OR dict_code IS NOT NULL);
