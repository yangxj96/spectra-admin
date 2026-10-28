ALTER TABLE spectra_core.sys_service_monitor_diagnostic_task
    ADD COLUMN instance_id varchar(160) NOT NULL DEFAULT 'unknown';

COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.instance_id IS
    '生成诊断文件的运行实例标识；unknown 表示升级前历史任务，仅可在文件实际所在节点清理或下载';

CREATE INDEX idx_sys_service_monitor_diagnostic_task_instance_status
    ON spectra_core.sys_service_monitor_diagnostic_task USING btree (instance_id, status, requested_at DESC);
