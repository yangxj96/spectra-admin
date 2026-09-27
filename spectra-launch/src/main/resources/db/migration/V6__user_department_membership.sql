-- 用户主部门以 sys_user.primary_department_id 为唯一事实来源；关系表只保留关联部门。
DO $$
DECLARE
    conflicting_user_ids text;
BEGIN
    SELECT string_agg(conflict.user_id::text, ', ' ORDER BY conflict.user_id)
    INTO conflicting_user_ids
    FROM (
        SELECT DISTINCT user_row.id AS user_id
        FROM spectra_core.sys_user_department_membership membership
        JOIN spectra_core.sys_user user_row ON user_row.id = membership.user_id
        WHERE membership.deleted IS NULL
          AND membership.membership_type = 'PRIMARY'
          AND user_row.primary_department_id IS NOT NULL
          AND user_row.primary_department_id <> membership.department_id
    ) conflict;

    IF conflicting_user_ids IS NOT NULL THEN
        RAISE EXCEPTION USING
            ERRCODE = '23514',
            MESSAGE = format(
                'V6 found PRIMARY membership conflicts with sys_user.primary_department_id for user IDs: %s',
                conflicting_user_ids
            );
    END IF;

    SELECT string_agg(conflict.user_id::text, ', ' ORDER BY conflict.user_id)
    INTO conflicting_user_ids
    FROM (
        SELECT DISTINCT user_row.id AS user_id
        FROM spectra_core.sys_user_department_membership membership
        JOIN spectra_core.sys_user user_row ON user_row.id = membership.user_id
        LEFT JOIN spectra_core.sys_user_department_membership primary_membership
               ON primary_membership.user_id = user_row.id
              AND primary_membership.deleted IS NULL
              AND primary_membership.membership_type = 'PRIMARY'
        WHERE membership.deleted IS NULL
          AND membership.membership_type = 'ASSOCIATED'
          AND COALESCE(user_row.primary_department_id, primary_membership.department_id) = membership.department_id
    ) conflict;

    IF conflicting_user_ids IS NOT NULL THEN
        RAISE EXCEPTION USING
            ERRCODE = '23514',
            MESSAGE = format(
                'V6 found ASSOCIATED membership equal to sys_user.primary_department_id for user IDs: %s',
                conflicting_user_ids
            );
    END IF;
END
$$;

-- 旧版本可能只在 PRIMARY 关系表中记录主部门；先回填空主部门字段。
UPDATE spectra_core.sys_user user_row
SET primary_department_id = membership.department_id
FROM spectra_core.sys_user_department_membership membership
WHERE membership.user_id = user_row.id
  AND membership.deleted IS NULL
  AND membership.membership_type = 'PRIMARY'
  AND user_row.primary_department_id IS NULL;

-- 主部门已写入 sys_user；删除全部冗余 PRIMARY 关系，保留关联关系及其软删除历史。
DELETE FROM spectra_core.sys_user_department_membership
WHERE membership_type = 'PRIMARY';

-- 该部分索引的谓词仍引用 membership_type，必须在删除列之前移除。
DROP INDEX spectra_core.uk_sys_user_primary_department_membership;

ALTER TABLE spectra_core.sys_user_department_membership
    DROP CONSTRAINT ck_sys_user_department_membership_type,
    DROP CONSTRAINT uk_sys_user_department_membership_pair,
    DROP COLUMN membership_type;

DROP INDEX spectra_core.idx_sys_user_department_membership_department;

CREATE UNIQUE INDEX uk_sys_user_department_membership_active_pair
    ON spectra_core.sys_user_department_membership (user_id, department_id)
    WHERE deleted IS NULL;

CREATE INDEX idx_sys_user_department_membership_department
    ON spectra_core.sys_user_department_membership (department_id, user_id)
    WHERE deleted IS NULL;
