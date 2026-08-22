-- 角色授权编辑会先逻辑删除原有边界，再写入新的边界。
-- 唯一性只约束当前有效记录，避免历史逻辑删除记录阻塞重复编辑。
ALTER TABLE spectra_security.sec_assignment_permission_boundary
    DROP CONSTRAINT IF EXISTS uk_sec_assignment_permission_boundary_assignment_permission;

CREATE UNIQUE INDEX IF NOT EXISTS uk_sec_assignment_permission_boundary_assignment_permission
    ON spectra_security.sec_assignment_permission_boundary (assignment_id, permission_id)
    WHERE deleted IS NULL;

ALTER TABLE spectra_security.sec_assignment_grant_boundary
    DROP CONSTRAINT IF EXISTS uk_sec_assignment_grant_boundary_assignment_permission;

CREATE UNIQUE INDEX IF NOT EXISTS uk_sec_assignment_grant_boundary_assignment_permission
    ON spectra_security.sec_assignment_grant_boundary (assignment_id, permission_id)
    WHERE deleted IS NULL;
