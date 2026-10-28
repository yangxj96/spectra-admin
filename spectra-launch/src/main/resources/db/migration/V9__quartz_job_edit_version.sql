ALTER TABLE spectra_quartz.QRTZ_JOB_DETAILS
    ADD COLUMN EDIT_VERSION BIGINT NOT NULL DEFAULT 0;

COMMENT ON COLUMN spectra_quartz.QRTZ_JOB_DETAILS.EDIT_VERSION IS
    '管理端 Job 编辑版本；提交时通过 SQL 条件更新检查，区别于 Job 参数 schema version';
