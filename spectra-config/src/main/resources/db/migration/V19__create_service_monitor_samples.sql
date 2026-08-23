-- 单体服务监控历史采样，仅保存当前应用实例的本地趋势数据。
CREATE TABLE spectra_core.sys_service_monitor_sample (
    id                          UUID DEFAULT uuidv7() PRIMARY KEY,
    collected_at                TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    cpu_usage                   DOUBLE PRECISION NOT NULL DEFAULT 0,
    cpu_logical_cores           INTEGER NOT NULL DEFAULT 0,
    system_memory_usage         DOUBLE PRECISION NOT NULL DEFAULT 0,
    system_memory_total_bytes  BIGINT NOT NULL DEFAULT 0,
    system_memory_used_bytes   BIGINT NOT NULL DEFAULT 0,
    system_memory_available_bytes BIGINT NOT NULL DEFAULT 0,
    jvm_heap_usage              DOUBLE PRECISION NOT NULL DEFAULT 0,
    jvm_heap_used_bytes         BIGINT NOT NULL DEFAULT 0,
    jvm_heap_max_bytes          BIGINT NOT NULL DEFAULT 0,
    jvm_non_heap_used_bytes     BIGINT NOT NULL DEFAULT 0,
    live_thread_count            INTEGER NOT NULL DEFAULT 0,
    peak_thread_count            INTEGER NOT NULL DEFAULT 0,
    gc_count                    BIGINT NOT NULL DEFAULT 0,
    qps                         DOUBLE PRECISION NOT NULL DEFAULT 0,
    error_rate                  DOUBLE PRECISION NOT NULL DEFAULT 0,
    p95_response_ms             DOUBLE PRECISION NOT NULL DEFAULT 0,
    request_metrics_available   BOOLEAN NOT NULL DEFAULT FALSE,
    status                      VARCHAR(16) NOT NULL DEFAULT 'DOWN',
    created_by                  UUID,
    created_at                  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                  UUID,
    updated_at                  TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                     TIMESTAMP(6) WITH TIME ZONE,
    version                     BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_sys_service_monitor_sample_status CHECK (status IN ('HEALTHY', 'WARNING', 'DEGRADED', 'DOWN')),
    CONSTRAINT ck_sys_service_monitor_sample_values CHECK (
        cpu_usage >= 0 AND cpu_usage <= 100
        AND system_memory_usage >= 0 AND system_memory_usage <= 100
        AND jvm_heap_usage >= 0 AND jvm_heap_usage <= 100
        AND cpu_logical_cores >= 0
        AND system_memory_total_bytes >= 0
        AND system_memory_used_bytes >= 0
        AND system_memory_available_bytes >= 0
        AND jvm_heap_used_bytes >= 0
        AND jvm_heap_max_bytes >= 0
        AND jvm_non_heap_used_bytes >= 0
        AND live_thread_count >= 0
        AND peak_thread_count >= 0
        AND gc_count >= 0
        AND qps >= 0
        AND error_rate >= 0
        AND p95_response_ms >= 0
    )
);

CREATE INDEX idx_sys_service_monitor_sample_collected_at
    ON spectra_core.sys_service_monitor_sample (collected_at DESC);

COMMENT ON TABLE spectra_core.sys_service_monitor_sample IS '单体服务监控历史采样表';
COMMENT ON COLUMN spectra_core.sys_service_monitor_sample.collected_at IS '监控采集时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_sample.cpu_usage IS 'CPU 使用率（百分比）';
COMMENT ON COLUMN spectra_core.sys_service_monitor_sample.system_memory_usage IS '系统内存使用率（百分比）';
COMMENT ON COLUMN spectra_core.sys_service_monitor_sample.jvm_heap_usage IS 'JVM 堆内存使用率（百分比）';
COMMENT ON COLUMN spectra_core.sys_service_monitor_sample.request_metrics_available IS '是否采集到 HTTP 请求指标';
COMMENT ON COLUMN spectra_core.sys_service_monitor_sample.status IS '采样时的服务状态';
