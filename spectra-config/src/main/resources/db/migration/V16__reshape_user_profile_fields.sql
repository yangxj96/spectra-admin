-- 用户资料模型直接切换为企业应用字段，不保留旧显示名称及个人扩展字段。
ALTER TABLE spectra_core.sys_user
    ADD COLUMN employee_no VARCHAR(64);

-- 现有开发数据没有正式工号，使用不可重复的临时工号完成结构切换；新建和编辑接口始终要求业务工号。
UPDATE spectra_core.sys_user
SET employee_no = 'EMP-' || replace(id::text, '-', '')
WHERE employee_no IS NULL;

ALTER TABLE spectra_core.sys_user
    ALTER COLUMN employee_no SET NOT NULL,
    ADD CONSTRAINT ck_sys_user_employee_no_not_blank CHECK (btrim(employee_no) <> '');

ALTER TABLE spectra_core.sys_user
    DROP COLUMN IF EXISTS username,
    DROP COLUMN IF EXISTS gender,
    DROP COLUMN IF EXISTS birthday,
    DROP COLUMN IF EXISTS country,
    DROP COLUMN IF EXISTS city,
    DROP COLUMN IF EXISTS locked_until;

CREATE UNIQUE INDEX uk_sys_user_employee_no
    ON spectra_core.sys_user (employee_no)
    WHERE deleted IS NULL;

-- 用户性别已从用户模型移除，对应字典组及其字典项一并清理。
DELETE FROM spectra_core.sys_dict_item
WHERE gid IN (
    SELECT id FROM spectra_core.sys_dict_group WHERE code = 'sys_user_gender'
);
DELETE FROM spectra_core.sys_dict_group
WHERE code = 'sys_user_gender';
