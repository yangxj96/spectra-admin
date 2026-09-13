-- 系统安全策略配置使用字典组驱动下拉选项。
INSERT INTO spectra_core.sys_dict_group (
    id,
    name,
    code,
    pid,
    state,
    remark,
    builtin,
    hide,
    created_by,
    created_at,
    updated_by,
    updated_at,
    deleted,
    version
)
SELECT
    '019f4f2e-5e4f-7a31-9d02-4c91a1b2d701',
    '安全策略',
    'sys_security_profile',
    NULL,
    true,
    '系统初始化使用的安全策略选项',
    true,
    true,
    '00000000-0000-0000-0000-000000000000',
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000000',
    CURRENT_TIMESTAMP,
    NULL,
    0
WHERE NOT EXISTS (
    SELECT 1
    FROM spectra_core.sys_dict_group
    WHERE code = 'sys_security_profile'
      AND deleted IS NULL
);

INSERT INTO spectra_core.sys_dict_item (
    id,
    gid,
    label,
    value,
    sort,
    state,
    remark,
    default_flag,
    created_by,
    created_at,
    updated_by,
    updated_at,
    deleted,
    version
)
SELECT
    '019f4f2e-5e50-7f18-a934-54e0a1967101',
    dict_group.id,
    '标准模式',
    'STANDARD',
    1,
    0,
    NULL,
    true,
    '00000000-0000-0000-0000-000000000000',
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000000',
    CURRENT_TIMESTAMP,
    NULL,
    0
FROM spectra_core.sys_dict_group AS dict_group
WHERE dict_group.code = 'sys_security_profile'
  AND dict_group.deleted IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM spectra_core.sys_dict_item AS dict_item
      WHERE dict_item.gid = dict_group.id
        AND dict_item.value = 'STANDARD'
        AND dict_item.deleted IS NULL
  );

INSERT INTO spectra_core.sys_dict_item (
    id,
    gid,
    label,
    value,
    sort,
    state,
    remark,
    default_flag,
    created_by,
    created_at,
    updated_by,
    updated_at,
    deleted,
    version
)
SELECT
    '019f4f2e-5e51-77b3-b8c2-287c3a5d7102',
    dict_group.id,
    '严格模式',
    'STRICT',
    2,
    0,
    NULL,
    false,
    '00000000-0000-0000-0000-000000000000',
    CURRENT_TIMESTAMP,
    '00000000-0000-0000-0000-000000000000',
    CURRENT_TIMESTAMP,
    NULL,
    0
FROM spectra_core.sys_dict_group AS dict_group
WHERE dict_group.code = 'sys_security_profile'
  AND dict_group.deleted IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM spectra_core.sys_dict_item AS dict_item
      WHERE dict_item.gid = dict_group.id
        AND dict_item.value = 'STRICT'
        AND dict_item.deleted IS NULL
  );

UPDATE spectra_core.sys_config
SET dict_code = 'sys_security_profile',
    updated_at = CURRENT_TIMESTAMP,
    version = COALESCE(version, 0) + 1
WHERE key = 'security.profile'
  AND deleted IS NULL
  AND dict_code IS DISTINCT FROM 'sys_security_profile';
