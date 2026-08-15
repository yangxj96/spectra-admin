-- Align the database with the current core entity mappings.

ALTER TABLE spectra_core.sys_region
    ALTER COLUMN level TYPE INTEGER USING NULLIF(BTRIM(level), '')::INTEGER;

ALTER TABLE spectra_core.sys_dict_group
    ADD COLUMN IF NOT EXISTS pid UUID,
    ADD COLUMN IF NOT EXISTS state BOOLEAN DEFAULT TRUE,
    ADD COLUMN IF NOT EXISTS remark VARCHAR(255),
    ADD COLUMN IF NOT EXISTS builtin BOOLEAN DEFAULT FALSE,
    ADD COLUMN IF NOT EXISTS hide BOOLEAN DEFAULT FALSE;

UPDATE spectra_core.sys_dict_group
SET state = TRUE
WHERE state IS NULL;

UPDATE spectra_core.sys_dict_group
SET builtin = FALSE
WHERE builtin IS NULL;

UPDATE spectra_core.sys_dict_group
SET hide = FALSE
WHERE hide IS NULL;

ALTER TABLE spectra_core.sys_dict_group
    ALTER COLUMN state SET NOT NULL,
    ALTER COLUMN state SET DEFAULT TRUE,
    ALTER COLUMN builtin SET NOT NULL,
    ALTER COLUMN builtin SET DEFAULT FALSE,
    ALTER COLUMN hide SET NOT NULL,
    ALTER COLUMN hide SET DEFAULT FALSE;

CREATE INDEX IF NOT EXISTS idx_sys_dict_group_pid
    ON spectra_core.sys_dict_group (pid) WHERE deleted IS NULL;

COMMENT ON COLUMN spectra_core.sys_dict_group.pid IS '上级字典组 ID';
COMMENT ON COLUMN spectra_core.sys_dict_group.state IS '是否启用';
COMMENT ON COLUMN spectra_core.sys_dict_group.remark IS '备注';
COMMENT ON COLUMN spectra_core.sys_dict_group.builtin IS '是否内置字典组';
COMMENT ON COLUMN spectra_core.sys_dict_group.hide IS '是否隐藏';
