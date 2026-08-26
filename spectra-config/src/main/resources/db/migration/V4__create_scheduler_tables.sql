/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

-- 统一单体调度内核。PostgreSQL 是状态、租约、幂等键和结果的唯一事实源。

CREATE TABLE spectra_core.scheduler_job (
    id                uuid PRIMARY KEY,
    job_key           varchar(200) NOT NULL,
    name              varchar(200) NOT NULL,
    module            varchar(100) NOT NULL,
    description       text,
    handler_key       varchar(200) NOT NULL,
    job_type          varchar(30) NOT NULL,
    run_scope         varchar(30) NOT NULL,
    definition_status varchar(30) NOT NULL,
    desired_state     varchar(30) NOT NULL,
    schedule_kind     varchar(30) NOT NULL,
    cron_expression   varchar(200),
    fixed_delay_ms    bigint,
    initial_delay_ms  bigint,
    next_fire_at      timestamp(6) with time zone,
    misfire_policy    varchar(30) NOT NULL,
    concurrency_policy varchar(30) NOT NULL,
    execution_policy  jsonb NOT NULL DEFAULT '{}'::jsonb,
    parameters        jsonb NOT NULL DEFAULT '{}'::jsonb,
    revision          bigint NOT NULL DEFAULT 1,
    created_by        uuid,
    created_at        timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by        uuid,
    updated_at        timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           timestamp(6) with time zone,
    version           bigint NOT NULL DEFAULT 0,
    CONSTRAINT uk_scheduler_job_job_key UNIQUE (job_key),
    CONSTRAINT ck_scheduler_job_type CHECK (job_type IN ('OPS', 'SYSTEM', 'LOOP')),
    CONSTRAINT ck_scheduler_job_scope CHECK (run_scope IN ('PER_INSTANCE', 'SINGLETON')),
    CONSTRAINT ck_scheduler_job_definition_status
        CHECK (definition_status IN ('REGISTERED', 'UNAVAILABLE', 'ARCHIVED')),
    CONSTRAINT ck_scheduler_job_desired_state CHECK (
        (job_type = 'LOOP' AND desired_state IN ('RUNNING', 'DRAINING', 'STOPPED'))
        OR (job_type <> 'LOOP' AND desired_state IN ('ENABLED', 'DISABLED'))
    ),
    CONSTRAINT ck_scheduler_job_schedule_kind CHECK (schedule_kind IN ('CRON', 'FIXED_DELAY', 'MANUAL')),
    CONSTRAINT ck_scheduler_job_schedule_definition CHECK (
        (schedule_kind = 'CRON' AND btrim(cron_expression) <> '' AND fixed_delay_ms IS NULL)
        OR (schedule_kind = 'FIXED_DELAY' AND fixed_delay_ms > 0 AND cron_expression IS NULL)
        OR (schedule_kind = 'MANUAL' AND cron_expression IS NULL AND fixed_delay_ms IS NULL)
    ),
    CONSTRAINT ck_scheduler_job_loop_schedule CHECK (
        job_type <> 'LOOP' OR schedule_kind = 'FIXED_DELAY'
    ),
    CONSTRAINT ck_scheduler_job_misfire_policy
        CHECK (misfire_policy IN ('SKIP', 'FIRE_ONCE', 'CATCH_UP_LIMITED')),
    CONSTRAINT ck_scheduler_job_concurrency_policy
        CHECK (concurrency_policy IN ('FORBID', 'ALLOW', 'REPLACE')),
    CONSTRAINT ck_scheduler_job_revision CHECK (revision > 0),
    CONSTRAINT ck_scheduler_job_initial_delay CHECK (initial_delay_ms IS NULL OR initial_delay_ms >= 0)
);

COMMENT ON TABLE spectra_core.scheduler_job IS '统一调度任务定义表；deleted 仅用于 BaseEntity 兼容';
COMMENT ON COLUMN spectra_core.scheduler_job.desired_state IS '离散任务使用 ENABLED/DISABLED，循环任务使用 RUNNING/DRAINING/STOPPED';
COMMENT ON COLUMN spectra_core.scheduler_job.execution_policy IS '超时、租约、重试、心跳和排空策略；只允许非敏感配置或密钥引用';
COMMENT ON COLUMN spectra_core.scheduler_job.parameters IS '任务参数；只允许参数值或密钥引用';

CREATE INDEX idx_scheduler_job_due
    ON spectra_core.scheduler_job (definition_status, desired_state, next_fire_at);

CREATE TABLE spectra_core.scheduler_execution (
    id                         uuid PRIMARY KEY,
    job_id                     uuid NOT NULL,
    fire_key                   varchar(300) NOT NULL,
    trigger_type               varchar(30) NOT NULL,
    status                     varchar(30) NOT NULL,
    job_revision               bigint NOT NULL,
    handler_version            varchar(100) NOT NULL,
    schedule_kind_snapshot     varchar(30) NOT NULL,
    schedule_expression_snapshot varchar(200),
    parameters_snapshot        jsonb NOT NULL DEFAULT '{}'::jsonb,
    effect_type                varchar(40) NOT NULL,
    scheduled_at               timestamp(6) with time zone NOT NULL,
    queued_at                  timestamp(6) with time zone NOT NULL,
    started_at                 timestamp(6) with time zone,
    finished_at                timestamp(6) with time zone,
    next_retry_at              timestamp(6) with time zone,
    deadline_at                timestamp(6) with time zone,
    attempt_no                 integer NOT NULL DEFAULT 1,
    max_attempts               integer NOT NULL DEFAULT 1,
    locked_by                  varchar(200),
    locked_at                  timestamp(6) with time zone,
    lease_expires_at           timestamp(6) with time zone,
    last_heartbeat_at          timestamp(6) with time zone,
    last_error_code            varchar(100),
    last_error_message         text,
    result_summary             jsonb NOT NULL DEFAULT '{}'::jsonb,
    original_execution_id     uuid,
    resolution_status          varchar(30) NOT NULL DEFAULT 'UNRESOLVED',
    resolution_reason          text,
    resolved_by                uuid,
    resolved_at                timestamp(6) with time zone,
    created_by                 uuid,
    created_at                 timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by                 uuid,
    updated_at                 timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                    timestamp(6) with time zone,
    version                    bigint NOT NULL DEFAULT 0,
    CONSTRAINT uk_scheduler_execution_fire_key UNIQUE (fire_key),
    CONSTRAINT fk_scheduler_execution_job FOREIGN KEY (job_id)
        REFERENCES spectra_core.scheduler_job (id) ON DELETE RESTRICT,
    CONSTRAINT fk_scheduler_execution_original FOREIGN KEY (original_execution_id)
        REFERENCES spectra_core.scheduler_execution (id) ON DELETE RESTRICT,
    CONSTRAINT ck_scheduler_execution_trigger_type
        CHECK (trigger_type IN ('SCHEDULE', 'MANUAL', 'RETRY')),
    CONSTRAINT ck_scheduler_execution_status
        CHECK (status IN ('QUEUED', 'RUNNING', 'RETRY_WAIT', 'SUCCEEDED', 'FAILED', 'UNKNOWN', 'SKIPPED', 'CANCELLED')),
    CONSTRAINT ck_scheduler_execution_schedule_kind
        CHECK (schedule_kind_snapshot IN ('CRON', 'FIXED_DELAY', 'MANUAL')),
    CONSTRAINT ck_scheduler_execution_effect_type
        CHECK (effect_type IN ('DB_ONLY', 'OUTBOX', 'EXTERNAL_IDEMPOTENT', 'EXTERNAL_UNKNOWN')),
    CONSTRAINT ck_scheduler_execution_attempts CHECK (attempt_no > 0 AND max_attempts > 0 AND attempt_no <= max_attempts),
    CONSTRAINT ck_scheduler_execution_resolution
        CHECK (resolution_status IN ('UNRESOLVED', 'CONFIRMED_SUCCESS', 'CONFIRMED_FAILED', 'RETRIED'))
);

COMMENT ON TABLE spectra_core.scheduler_execution IS '统一调度离散执行记录表；UNKNOWN 原状态不可覆盖';
COMMENT ON COLUMN spectra_core.scheduler_execution.parameters_snapshot IS '创建执行时的参数快照，不保存当前用户、租户或隐式请求上下文';

CREATE INDEX idx_scheduler_execution_queue
    ON spectra_core.scheduler_execution (status, scheduled_at);
CREATE INDEX idx_scheduler_execution_lease
    ON spectra_core.scheduler_execution (status, lease_expires_at);
CREATE INDEX idx_scheduler_execution_job_created
    ON spectra_core.scheduler_execution (job_id, created_at);

CREATE TABLE spectra_core.scheduler_loop_runtime (
    id                       uuid PRIMARY KEY,
    job_id                   uuid NOT NULL,
    session_key              varchar(300) NOT NULL,
    instance_id              varchar(200) NOT NULL,
    status                   varchar(30) NOT NULL,
    started_at               timestamp(6) with time zone NOT NULL,
    stopped_at               timestamp(6) with time zone,
    last_heartbeat_at        timestamp(6) with time zone,
    lease_expires_at         timestamp(6) with time zone,
    last_cycle_at            timestamp(6) with time zone,
    last_progress_at         timestamp(6) with time zone,
    drain_deadline_at        timestamp(6) with time zone,
    total_cycles             bigint NOT NULL DEFAULT 0,
    total_processed          bigint NOT NULL DEFAULT 0,
    total_failed             bigint NOT NULL DEFAULT 0,
    consecutive_error_count  bigint NOT NULL DEFAULT 0,
    last_error_code          varchar(100),
    last_error_message       text,
    state_reason             text,
    created_by               uuid,
    created_at               timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by               uuid,
    updated_at               timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                  timestamp(6) with time zone,
    version                  bigint NOT NULL DEFAULT 0,
    CONSTRAINT uk_scheduler_loop_runtime_session_key UNIQUE (session_key),
    CONSTRAINT fk_scheduler_loop_runtime_job FOREIGN KEY (job_id)
        REFERENCES spectra_core.scheduler_job (id) ON DELETE RESTRICT,
    CONSTRAINT ck_scheduler_loop_runtime_status
        CHECK (status IN ('STARTING', 'RUNNING', 'DEGRADED', 'DRAINING', 'STOPPED', 'CRASHED', 'UNKNOWN')),
    CONSTRAINT ck_scheduler_loop_runtime_counters
        CHECK (total_cycles >= 0 AND total_processed >= 0 AND total_failed >= 0 AND consecutive_error_count >= 0)
);

COMMENT ON TABLE spectra_core.scheduler_loop_runtime IS '高频循环运行会话表；每个会话跨多个周期，不为每周期创建执行记录';

CREATE INDEX idx_scheduler_loop_runtime_job_status
    ON spectra_core.scheduler_loop_runtime (job_id, status);
CREATE INDEX idx_scheduler_loop_runtime_instance_status
    ON spectra_core.scheduler_loop_runtime (job_id, instance_id, status);

CREATE TABLE spectra_core.scheduler_control_command (
    id                       uuid PRIMARY KEY,
    job_id                   uuid NOT NULL,
    target_runtime_id        uuid,
    target_session_key       varchar(300),
    expected_runtime_version bigint,
    command_type             varchar(30) NOT NULL,
    status                   varchar(30) NOT NULL DEFAULT 'REQUESTED',
    idempotency_key          varchar(300) NOT NULL,
    reason                   text NOT NULL,
    requested_by             uuid,
    requested_at             timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deadline_at              timestamp(6) with time zone,
    applied_at               timestamp(6) with time zone,
    finished_at              timestamp(6) with time zone,
    result_code              varchar(100),
    result_message           text,
    created_by               uuid,
    created_at               timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by               uuid,
    updated_at               timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted                  timestamp(6) with time zone,
    version                  bigint NOT NULL DEFAULT 0,
    CONSTRAINT uk_scheduler_control_command_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_scheduler_control_command_job FOREIGN KEY (job_id)
        REFERENCES spectra_core.scheduler_job (id) ON DELETE RESTRICT,
    CONSTRAINT fk_scheduler_control_command_runtime FOREIGN KEY (target_runtime_id)
        REFERENCES spectra_core.scheduler_loop_runtime (id) ON DELETE RESTRICT,
    CONSTRAINT ck_scheduler_control_command_type
        CHECK (command_type IN ('START', 'DRAIN_STOP', 'RESTART', 'FORCE_STOP', 'FORCE_RECLAIM')),
    CONSTRAINT ck_scheduler_control_command_status
        CHECK (status IN ('REQUESTED', 'APPLYING', 'APPLIED', 'FAILED', 'TIMEOUT')),
    CONSTRAINT ck_scheduler_control_command_reason CHECK (btrim(reason) <> '')
);

COMMENT ON TABLE spectra_core.scheduler_control_command IS '高频循环控制命令表；命令先持久化再应用';

CREATE INDEX idx_scheduler_control_command_status_requested
    ON spectra_core.scheduler_control_command (status, requested_at);

CREATE TABLE spectra_core.scheduler_loop_error (
    id                    uuid PRIMARY KEY,
    job_id                uuid NOT NULL,
    instance_id           varchar(200) NOT NULL,
    runtime_id            uuid,
    error_fingerprint     varchar(300) NOT NULL,
    error_code            varchar(100) NOT NULL,
    error_message         text NOT NULL,
    status                varchar(30) NOT NULL DEFAULT 'OPEN',
    first_seen_at         timestamp(6) with time zone NOT NULL,
    last_seen_at          timestamp(6) with time zone NOT NULL,
    last_logged_at        timestamp(6) with time zone,
    occurrence_count      bigint NOT NULL DEFAULT 1,
    suppressed_count      bigint NOT NULL DEFAULT 0,
    last_context          jsonb NOT NULL DEFAULT '{}'::jsonb,
    resolved_by           uuid,
    resolved_at           timestamp(6) with time zone,
    resolution_reason     text,
    created_by            uuid,
    created_at            timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by            uuid,
    updated_at            timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted               timestamp(6) with time zone,
    version               bigint NOT NULL DEFAULT 0,
    CONSTRAINT fk_scheduler_loop_error_job FOREIGN KEY (job_id)
        REFERENCES spectra_core.scheduler_job (id) ON DELETE RESTRICT,
    CONSTRAINT fk_scheduler_loop_error_runtime FOREIGN KEY (runtime_id)
        REFERENCES spectra_core.scheduler_loop_runtime (id) ON DELETE RESTRICT,
    CONSTRAINT uk_scheduler_loop_error_fingerprint UNIQUE (job_id, instance_id, error_fingerprint),
    CONSTRAINT ck_scheduler_loop_error_status CHECK (status IN ('OPEN', 'RESOLVED')),
    CONSTRAINT ck_scheduler_loop_error_counts CHECK (occurrence_count > 0 AND suppressed_count >= 0)
);

COMMENT ON TABLE spectra_core.scheduler_loop_error IS '高频循环错误聚合表；重复错误通过计数和限流日志收敛';

-- 初始任务只写入定义和期望状态。next_fire_at 由启动初始化器按 system.default-timezone 计算。
INSERT INTO spectra_core.scheduler_job (
    id, job_key, name, module, description, handler_key, job_type, run_scope, definition_status, desired_state,
    schedule_kind, cron_expression, fixed_delay_ms, initial_delay_ms, next_fire_at, misfire_policy, concurrency_policy,
    execution_policy, parameters, revision, created_at, updated_at, version
) VALUES
    ('00000000-0000-0000-0000-000000000401', 'notification.task-worker', '通知任务循环', 'notification',
     '领取并处理通知投递任务；正常周期不输出普通日志，错误进入循环聚合。', 'notification.task-worker',
     'LOOP', 'PER_INSTANCE', 'REGISTERED', 'RUNNING', 'FIXED_DELAY', NULL, 5000, 0, NULL, 'SKIP', 'ALLOW',
     '{"heartbeatIntervalMs":2000,"leaseDurationMs":15000,"errorLogIntervalMs":60000}'::jsonb, '{}'::jsonb, 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000402', 'notification.cleanup-sensitive-payload', '通知敏感载荷清理', 'notification',
     '清理通知敏感载荷和过期短时数据。', 'notification.cleanup-sensitive-payload',
     'SYSTEM', 'SINGLETON', 'REGISTERED', 'ENABLED', 'FIXED_DELAY', NULL, 3600000, 0, NULL, 'FIRE_ONCE', 'FORBID',
     '{"timeoutMs":300000,"leaseDurationMs":600000,"maxAttempts":1}'::jsonb, '{}'::jsonb, 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000403', 'system.monitor.collect-snapshot', '服务监控采样循环', 'system',
     '收集单体应用服务监控快照。', 'system.monitor.collect-snapshot',
     'LOOP', 'PER_INSTANCE', 'REGISTERED', 'RUNNING', 'FIXED_DELAY', NULL, 10000, 1000, NULL, 'SKIP', 'ALLOW',
     '{"heartbeatIntervalMs":3000,"leaseDurationMs":30000,"errorLogIntervalMs":60000}'::jsonb, '{}'::jsonb, 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000404', 'system.monitor.diagnostic-cleanup', '监控诊断清理', 'system',
     '清理已过期的服务监控诊断任务。', 'system.monitor.diagnostic-cleanup',
     'SYSTEM', 'SINGLETON', 'REGISTERED', 'ENABLED', 'FIXED_DELAY', NULL, 3600000, 60000, NULL, 'FIRE_ONCE', 'FORBID',
     '{"timeoutMs":300000,"leaseDurationMs":600000,"maxAttempts":1}'::jsonb, '{}'::jsonb, 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0),
    ('00000000-0000-0000-0000-000000000405', 'oa.contract.milestone-reminder', '合同里程碑提醒', 'oa',
     '发送合同里程碑到期提醒。', 'oa.contract.milestone-reminder',
     'OPS', 'SINGLETON', 'REGISTERED', 'ENABLED', 'CRON', '0 0 1 * * *', NULL, 0, NULL, 'SKIP', 'FORBID',
     '{"timeoutMs":300000,"leaseDurationMs":600000,"maxAttempts":3}'::jsonb, '{}'::jsonb, 1,
     CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0);

-- 调度管理权限。ROLE_ADMIN_SYSTEM 只拥有普通调度能力，ROLE_DEV_OPS 才拥有高风险动作。
INSERT INTO spectra_security.sec_permission
    (id, code, name, resource_code, action_code, allowed_scope_modes, state, system_managed)
VALUES
    ('00000000-0000-0000-0000-000000000421', 'system:scheduler:query', '调度任务查看',
     'system:scheduler', 'query', 'NONE', 'ACTIVE', true),
    ('00000000-0000-0000-0000-000000000422', 'system:scheduler:manage', 'OPS 调度配置维护',
     'system:scheduler', 'manage', 'NONE', 'ACTIVE', true),
    ('00000000-0000-0000-0000-000000000423', 'system:scheduler:execute', '调度任务受控执行',
     'system:scheduler', 'execute', 'NONE', 'ACTIVE', true),
    ('00000000-0000-0000-0000-000000000424', 'system:scheduler:retry', '调度执行安全重试',
     'system:scheduler', 'retry', 'NONE', 'ACTIVE', true),
    ('00000000-0000-0000-0000-000000000425', 'system:scheduler:resolve', '调度 UNKNOWN 结果解决',
     'system:scheduler', 'resolve', 'NONE', 'ACTIVE', true),
    ('00000000-0000-0000-0000-000000000426', 'system:scheduler:control', '循环任务普通控制',
     'system:scheduler', 'control', 'NONE', 'ACTIVE', true),
    ('00000000-0000-0000-0000-000000000427', 'system:scheduler:force-control', '循环任务高风险控制',
     'system:scheduler', 'force-control', 'NONE', 'ACTIVE', true)
ON CONFLICT (code) DO NOTHING;

INSERT INTO spectra_security.sec_role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM spectra_security.sec_role role
JOIN spectra_security.sec_permission permission ON permission.code IN (
    'system:scheduler:query', 'system:scheduler:manage', 'system:scheduler:execute',
    'system:scheduler:retry', 'system:scheduler:control')
WHERE role.code = 'ROLE_ADMIN_SYSTEM'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO spectra_security.sec_role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM spectra_security.sec_role role
JOIN spectra_security.sec_permission permission ON permission.code = 'system:scheduler:query'
WHERE role.code = 'ROLE_AUDIT'
ON CONFLICT (role_id, permission_id) DO NOTHING;

INSERT INTO spectra_security.sec_role_permission (role_id, permission_id)
SELECT role.id, permission.id
FROM spectra_security.sec_role role
JOIN spectra_security.sec_permission permission ON permission.code IN (
    'system:scheduler:query', 'system:scheduler:manage', 'system:scheduler:execute',
    'system:scheduler:retry', 'system:scheduler:resolve', 'system:scheduler:control',
    'system:scheduler:force-control')
WHERE role.code = 'ROLE_DEV_OPS'
ON CONFLICT (role_id, permission_id) DO NOTHING;
