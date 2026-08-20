-- 同步系统初始化与系统设置引导状态表的表级、字段级注释。
COMMENT ON TABLE spectra_core.sys_system_state IS '系统初始化与系统设置引导状态表；每个状态键只保留一条记录';
COMMENT ON COLUMN spectra_core.sys_system_state.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_system_state.state_key IS '状态机键：SYSTEM=首次系统初始化；SYSTEM_GUIDE=系统设置引导';
COMMENT ON COLUMN spectra_core.sys_system_state.state IS '状态值按状态机解释：SYSTEM=UNINITIALIZED/INITIALIZING/INITIALIZED；SYSTEM_GUIDE=PENDING/COMPLETED';
COMMENT ON COLUMN spectra_core.sys_system_state.initialization_id IS '首次系统初始化流程ID，仅 SYSTEM 状态机使用';
COMMENT ON COLUMN spectra_core.sys_system_state.initialized_at IS '首次系统初始化完成时间，仅 SYSTEM 状态机使用';
COMMENT ON COLUMN spectra_core.sys_system_state.initialized_by IS '完成首次系统初始化的用户ID，仅 SYSTEM 状态机使用';
COMMENT ON COLUMN spectra_core.sys_system_state.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_system_state.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_system_state.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_system_state.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_system_state.deleted IS '删除时间（NULL表示未删除）';
COMMENT ON COLUMN spectra_core.sys_system_state.version IS '乐观锁版本号';
