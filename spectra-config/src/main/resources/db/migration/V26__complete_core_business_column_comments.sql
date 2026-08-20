-- V26：补齐 Core 组织及初始化状态表的业务字段注释。
-- V25 已执行后不得改写，新增注释使用独立增量迁移维护。

COMMENT ON COLUMN spectra_core.sys_user_department_membership.user_id IS '用户ID';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.department_id IS '部门ID';
COMMENT ON COLUMN spectra_core.sys_user_department_membership.membership_type IS '成员关系：PRIMARY-主部门，ASSOCIATED-关联部门';

COMMENT ON COLUMN spectra_core.sys_department_closure.ancestor_id IS '祖先部门ID';
COMMENT ON COLUMN spectra_core.sys_department_closure.descendant_id IS '后代部门ID';
COMMENT ON COLUMN spectra_core.sys_department_closure.depth IS '层级深度，0表示部门自身';

COMMENT ON COLUMN spectra_core.sys_organization_version.singleton_key IS '单例键，固定为 SYSTEM';
COMMENT ON COLUMN spectra_core.sys_organization_version.organization_version IS '组织结构版本号';
COMMENT ON COLUMN spectra_core.sys_organization_version.changed_at IS '最近变更时间';

COMMENT ON COLUMN spectra_core.sys_system_state.state_key IS '状态键，固定为 SYSTEM';
COMMENT ON COLUMN spectra_core.sys_system_state.state IS '初始化状态：UNINITIALIZED/INITIALIZING/INITIALIZED';
COMMENT ON COLUMN spectra_core.sys_system_state.initialization_id IS '初始化流程ID';
COMMENT ON COLUMN spectra_core.sys_system_state.initialized_at IS '系统初始化完成时间';
COMMENT ON COLUMN spectra_core.sys_system_state.initialized_by IS '完成初始化的用户ID';
