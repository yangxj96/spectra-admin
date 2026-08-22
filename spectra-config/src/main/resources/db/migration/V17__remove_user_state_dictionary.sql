-- 用户状态由 UserStatus 枚举统一定义，不再维护 sys_user_state 字典。
DELETE FROM spectra_core.sys_dict_item
WHERE gid IN (
    SELECT id FROM spectra_core.sys_dict_group WHERE code = 'sys_user_state'
);

DELETE FROM spectra_core.sys_dict_group
WHERE code = 'sys_user_state';
