-- 系统初始化完成后，DEV_OPS 仍需完成一次系统设置引导。
ALTER TABLE spectra_core.sys_system_state
    DROP CONSTRAINT IF EXISTS ck_sys_system_state_key;

ALTER TABLE spectra_core.sys_system_state
    ADD CONSTRAINT ck_sys_system_state_key
        CHECK (state_key IN ('SYSTEM', 'SYSTEM_GUIDE'));

ALTER TABLE spectra_core.sys_system_state
    DROP CONSTRAINT IF EXISTS ck_sys_system_state_value;

ALTER TABLE spectra_core.sys_system_state
    ADD CONSTRAINT ck_sys_system_state_value
        CHECK ((state_key = 'SYSTEM' AND state IN ('UNINITIALIZED', 'INITIALIZING', 'INITIALIZED'))
            OR (state_key = 'SYSTEM_GUIDE' AND state IN ('PENDING', 'COMPLETED')));

INSERT INTO spectra_core.sys_system_state
    (id, state_key, state, created_by, created_at, updated_by, updated_at, version)
VALUES
    ('00000000-0000-0000-0000-000000000001', 'SYSTEM_GUIDE', 'PENDING',
     '00000000-0000-0000-0000-000000000000', TIMESTAMPTZ '1996-10-15 00:00:00+08:00',
     '00000000-0000-0000-0000-000000000000', TIMESTAMPTZ '1996-10-15 00:00:00+08:00', 0)
ON CONFLICT (state_key) DO NOTHING;
