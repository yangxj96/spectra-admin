-- 第三阶段服务监控：告警规则、告警事件、运行时诊断任务。
-- 诊断文件只保存随机相对文件名，绝不把本机绝对路径写入数据库。

ALTER TABLE spectra_core.sys_service_monitor_sample
    ADD COLUMN database_status VARCHAR(16) NOT NULL DEFAULT 'UNKNOWN',
    ADD COLUMN redis_status VARCHAR(16) NOT NULL DEFAULT 'UNKNOWN';

COMMENT ON COLUMN spectra_core.sys_service_monitor_sample.database_status IS '采样时 PostgreSQL 连接状态：UP/DOWN/UNKNOWN';
COMMENT ON COLUMN spectra_core.sys_service_monitor_sample.redis_status IS '采样时 Redis 连接状态：UP/DOWN/UNKNOWN';

CREATE TABLE spectra_core.sys_service_monitor_alert_rule (
    id                   UUID DEFAULT uuidv7() PRIMARY KEY,
    code                 VARCHAR(80) NOT NULL,
    name                 VARCHAR(100) NOT NULL,
    metric_code          VARCHAR(80) NOT NULL,
    operator_code        VARCHAR(16) NOT NULL DEFAULT 'GTE',
    threshold_value      DOUBLE PRECISION,
    expected_value       VARCHAR(80),
    severity             VARCHAR(16) NOT NULL DEFAULT 'WARNING',
    enabled              BOOLEAN NOT NULL DEFAULT TRUE,
    consecutive_failures INTEGER NOT NULL DEFAULT 1,
    cooldown_seconds     INTEGER NOT NULL DEFAULT 300,
    remark               VARCHAR(500),
    created_by           UUID,
    created_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by           UUID,
    updated_at           TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted              TIMESTAMP(6) WITH TIME ZONE,
    version              BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT uk_sys_service_monitor_alert_rule_code UNIQUE (code),
    CONSTRAINT ck_sys_service_monitor_alert_rule_operator CHECK (operator_code IN ('GTE', 'GT', 'LTE', 'LT', 'EQ', 'NE')),
    CONSTRAINT ck_sys_service_monitor_alert_rule_severity CHECK (severity IN ('WARNING', 'CRITICAL')),
    CONSTRAINT ck_sys_service_monitor_alert_rule_values CHECK (
        consecutive_failures BETWEEN 1 AND 10
        AND cooldown_seconds BETWEEN 0 AND 86400
        AND (threshold_value IS NULL OR threshold_value >= 0)
    )
);

COMMENT ON TABLE spectra_core.sys_service_monitor_alert_rule IS '单体服务监控告警规则表';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.code IS '稳定规则编码';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.name IS '规则名称';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.metric_code IS '监控指标编码';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.operator_code IS '比较运算符：GTE/GT/LTE/LT/EQ/NE';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.threshold_value IS '数值指标阈值';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.expected_value IS '状态指标期望值';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.severity IS '告警级别：WARNING/CRITICAL';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.enabled IS '是否启用';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.consecutive_failures IS '连续触发次数';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.cooldown_seconds IS '通知冷却时间（秒）';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.remark IS '规则说明';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.deleted IS '删除时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_rule.version IS '乐观锁版本';

CREATE TABLE spectra_core.sys_service_monitor_alert_event (
    id                 UUID DEFAULT uuidv7() PRIMARY KEY,
    rule_id            UUID NOT NULL,
    rule_code          VARCHAR(80) NOT NULL,
    rule_name          VARCHAR(100) NOT NULL,
    metric_code        VARCHAR(80) NOT NULL,
    severity           VARCHAR(16) NOT NULL,
    state              VARCHAR(16) NOT NULL DEFAULT 'ACTIVE',
    current_value      VARCHAR(120),
    threshold_value    DOUBLE PRECISION,
    expected_value     VARCHAR(80),
    message            VARCHAR(500) NOT NULL,
    first_occurred_at  TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    last_occurred_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    recovered_at       TIMESTAMP(6) WITH TIME ZONE,
    occurrence_count   INTEGER NOT NULL DEFAULT 1,
    last_notified_at   TIMESTAMP(6) WITH TIME ZONE,
    created_by         UUID,
    created_at         TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by         UUID,
    updated_at         TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted            TIMESTAMP(6) WITH TIME ZONE,
    version            BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_sys_service_monitor_alert_event_severity CHECK (severity IN ('WARNING', 'CRITICAL')),
    CONSTRAINT ck_sys_service_monitor_alert_event_state CHECK (state IN ('ACTIVE', 'RECOVERED')),
    CONSTRAINT ck_sys_service_monitor_alert_event_occurrence CHECK (occurrence_count >= 1)
);

COMMENT ON TABLE spectra_core.sys_service_monitor_alert_event IS '单体服务监控告警事件表';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.rule_id IS '告警规则ID';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.rule_code IS '规则编码快照';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.rule_name IS '规则名称快照';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.metric_code IS '监控指标编码';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.severity IS '告警级别';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.state IS '事件状态：ACTIVE/RECOVERED';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.current_value IS '当前指标值';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.threshold_value IS '数值阈值快照';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.expected_value IS '状态期望值快照';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.message IS '脱敏告警说明';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.first_occurred_at IS '首次触发时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.last_occurred_at IS '最近触发时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.recovered_at IS '恢复时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.occurrence_count IS '连续触发次数';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.last_notified_at IS '最近通知时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.deleted IS '删除时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_alert_event.version IS '乐观锁版本';

CREATE UNIQUE INDEX uk_sys_service_monitor_alert_event_active_rule
    ON spectra_core.sys_service_monitor_alert_event (rule_id)
    WHERE state = 'ACTIVE' AND deleted IS NULL;
CREATE INDEX idx_sys_service_monitor_alert_event_state_time
    ON spectra_core.sys_service_monitor_alert_event (state, last_occurred_at DESC);

CREATE TABLE spectra_core.sys_service_monitor_diagnostic_task (
    id             UUID DEFAULT uuidv7() PRIMARY KEY,
    task_type      VARCHAR(20) NOT NULL,
    status         VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    file_name      VARCHAR(120),
    display_name   VARCHAR(200),
    file_size      BIGINT,
    error_message  VARCHAR(300),
    requested_at   TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    started_at     TIMESTAMP(6) WITH TIME ZONE,
    completed_at   TIMESTAMP(6) WITH TIME ZONE,
    expires_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL,
    created_by     UUID,
    created_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by     UUID,
    updated_at     TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted        TIMESTAMP(6) WITH TIME ZONE,
    version        BIGINT NOT NULL DEFAULT 0,
    CONSTRAINT ck_sys_service_monitor_diagnostic_task_type CHECK (task_type IN ('THREAD_DUMP', 'HEAP_DUMP')),
    CONSTRAINT ck_sys_service_monitor_diagnostic_task_status CHECK (status IN ('PENDING', 'RUNNING', 'SUCCEEDED', 'FAILED', 'EXPIRED')),
    CONSTRAINT ck_sys_service_monitor_diagnostic_task_size CHECK (file_size IS NULL OR file_size >= 0)
);

COMMENT ON TABLE spectra_core.sys_service_monitor_diagnostic_task IS '单体服务监控诊断任务表';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.id IS '主键ID';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.task_type IS '诊断类型：THREAD_DUMP/HEAP_DUMP';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.status IS '任务状态';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.file_name IS '系统生成的相对文件名';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.display_name IS '前端展示名称';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.file_size IS '文件大小（字节）';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.error_message IS '脱敏失败原因';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.requested_at IS '请求时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.started_at IS '开始执行时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.completed_at IS '完成时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.expires_at IS '文件过期时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.created_by IS '创建人';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.updated_by IS '最后更新人';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.deleted IS '删除时间';
COMMENT ON COLUMN spectra_core.sys_service_monitor_diagnostic_task.version IS '乐观锁版本';

CREATE INDEX idx_sys_service_monitor_diagnostic_task_status_time
    ON spectra_core.sys_service_monitor_diagnostic_task (status, requested_at DESC);
CREATE INDEX idx_sys_service_monitor_diagnostic_task_expires_at
    ON spectra_core.sys_service_monitor_diagnostic_task (expires_at);

INSERT INTO spectra_security.sec_permission
    (id, code, name, resource_code, action_code, allowed_scope_modes, state, system_managed,
     created_by, created_at, updated_by, updated_at, deleted, version)
SELECT uuidv7(), 'system:monitor:alert', '服务监控告警查看', 'system:monitor', 'alert', 'NONE', 'ACTIVE', TRUE,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM spectra_security.sec_permission WHERE code = 'system:monitor:alert');

INSERT INTO spectra_security.sec_permission
    (id, code, name, resource_code, action_code, allowed_scope_modes, state, system_managed,
     created_by, created_at, updated_by, updated_at, deleted, version)
SELECT uuidv7(), 'system:monitor:configure', '服务监控告警配置', 'system:monitor', 'configure', 'NONE', 'ACTIVE', TRUE,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM spectra_security.sec_permission WHERE code = 'system:monitor:configure');

INSERT INTO spectra_security.sec_permission
    (id, code, name, resource_code, action_code, allowed_scope_modes, state, system_managed,
     created_by, created_at, updated_by, updated_at, deleted, version)
SELECT uuidv7(), 'system:monitor:diagnose', '服务监控诊断', 'system:monitor', 'diagnose', 'NONE', 'ACTIVE', TRUE,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP,
       '00000000-0000-0000-0000-000000000000', CURRENT_TIMESTAMP, NULL, 0
WHERE NOT EXISTS (SELECT 1 FROM spectra_security.sec_permission WHERE code = 'system:monitor:diagnose');

INSERT INTO spectra_core.sys_service_monitor_alert_rule
    (code, name, metric_code, operator_code, threshold_value, expected_value, severity, enabled,
     consecutive_failures, cooldown_seconds, remark)
SELECT 'CPU_USAGE', 'CPU 使用率过高', 'CPU_USAGE', 'GTE', 80, NULL, 'WARNING', TRUE, 1, 300, '超过阈值时通知运维管理员'
WHERE NOT EXISTS (SELECT 1 FROM spectra_core.sys_service_monitor_alert_rule WHERE code = 'CPU_USAGE');
INSERT INTO spectra_core.sys_service_monitor_alert_rule
    (code, name, metric_code, operator_code, threshold_value, expected_value, severity, enabled,
     consecutive_failures, cooldown_seconds, remark)
SELECT 'SYSTEM_MEMORY_USAGE', '系统内存使用率过高', 'SYSTEM_MEMORY_USAGE', 'GTE', 80, NULL, 'WARNING', TRUE, 1, 300, '超过阈值时通知运维管理员'
WHERE NOT EXISTS (SELECT 1 FROM spectra_core.sys_service_monitor_alert_rule WHERE code = 'SYSTEM_MEMORY_USAGE');
INSERT INTO spectra_core.sys_service_monitor_alert_rule
    (code, name, metric_code, operator_code, threshold_value, expected_value, severity, enabled,
     consecutive_failures, cooldown_seconds, remark)
SELECT 'JVM_HEAP_USAGE', 'JVM 堆内存使用率过高', 'JVM_HEAP_USAGE', 'GTE', 75, NULL, 'WARNING', TRUE, 1, 300, '超过阈值时通知运维管理员'
WHERE NOT EXISTS (SELECT 1 FROM spectra_core.sys_service_monitor_alert_rule WHERE code = 'JVM_HEAP_USAGE');
INSERT INTO spectra_core.sys_service_monitor_alert_rule
    (code, name, metric_code, operator_code, threshold_value, expected_value, severity, enabled,
     consecutive_failures, cooldown_seconds, remark)
SELECT 'ERROR_RATE', 'HTTP 5xx 错误率过高', 'ERROR_RATE', 'GTE', 1, NULL, 'CRITICAL', TRUE, 1, 300, '有请求指标时生效'
WHERE NOT EXISTS (SELECT 1 FROM spectra_core.sys_service_monitor_alert_rule WHERE code = 'ERROR_RATE');
INSERT INTO spectra_core.sys_service_monitor_alert_rule
    (code, name, metric_code, operator_code, threshold_value, expected_value, severity, enabled,
     consecutive_failures, cooldown_seconds, remark)
SELECT 'P95_RESPONSE_MS', '请求响应时间过长', 'P95_RESPONSE_MS', 'GTE', 500, NULL, 'WARNING', TRUE, 1, 300, '有请求指标时生效'
WHERE NOT EXISTS (SELECT 1 FROM spectra_core.sys_service_monitor_alert_rule WHERE code = 'P95_RESPONSE_MS');
INSERT INTO spectra_core.sys_service_monitor_alert_rule
    (code, name, metric_code, operator_code, threshold_value, expected_value, severity, enabled,
     consecutive_failures, cooldown_seconds, remark)
SELECT 'DATABASE_STATUS', 'PostgreSQL 不可用', 'DATABASE_STATUS', 'NE', NULL, 'UP', 'CRITICAL', TRUE, 1, 300, '数据库连通性检查异常时通知运维管理员'
WHERE NOT EXISTS (SELECT 1 FROM spectra_core.sys_service_monitor_alert_rule WHERE code = 'DATABASE_STATUS');
INSERT INTO spectra_core.sys_service_monitor_alert_rule
    (code, name, metric_code, operator_code, threshold_value, expected_value, severity, enabled,
     consecutive_failures, cooldown_seconds, remark)
SELECT 'REDIS_STATUS', 'Redis 不可用', 'REDIS_STATUS', 'NE', NULL, 'UP', 'CRITICAL', TRUE, 1, 300, 'Redis 连通性检查异常时通知运维管理员'
WHERE NOT EXISTS (SELECT 1 FROM spectra_core.sys_service_monitor_alert_rule WHERE code = 'REDIS_STATUS');
