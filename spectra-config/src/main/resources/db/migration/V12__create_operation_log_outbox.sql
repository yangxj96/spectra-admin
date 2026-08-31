/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

-- 普通操作日志使用 PostgreSQL outbox 保证业务事务与日志事件一致。
-- outbox 事件只追加和推进状态，不因消费失败删除，永久失败进入 DEAD_LETTER 等待人工处置。

ALTER TABLE spectra_core.sys_log
    ADD COLUMN outbox_event_id uuid;

COMMENT ON COLUMN spectra_core.sys_log.outbox_event_id IS '操作日志来源 outbox 事件 ID；用于消费幂等和追溯';

CREATE UNIQUE INDEX uk_sys_log_outbox_event_id
    ON spectra_core.sys_log (outbox_event_id)
    WHERE outbox_event_id IS NOT NULL;

CREATE TABLE spectra_core.sys_operation_log_outbox (
    event_id         uuid NOT NULL,
    idempotency_key  varchar(200) NOT NULL,
    payload          jsonb NOT NULL,
    status           varchar(32) NOT NULL DEFAULT 'PENDING',
    available_at     timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    processed_at     timestamp(6) with time zone,
    attempts         integer NOT NULL DEFAULT 0,
    lease_owner      varchar(200),
    lease_until      timestamp(6) with time zone,
    last_error       varchar(2000),
    created_by       uuid,
    created_at       timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by       uuid,
    updated_at       timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted          timestamp(6) with time zone,
    version          bigint NOT NULL DEFAULT 0,
    CONSTRAINT pk_sys_operation_log_outbox PRIMARY KEY (event_id),
    CONSTRAINT uk_sys_operation_log_outbox_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT ck_sys_operation_log_outbox_status
        CHECK (status IN ('PENDING', 'PROCESSING', 'PROCESSED', 'DEAD_LETTER')),
    CONSTRAINT ck_sys_operation_log_outbox_attempts CHECK (attempts >= 0),
    CONSTRAINT ck_sys_operation_log_outbox_processed_status CHECK (
        (status = 'PROCESSED' AND processed_at IS NOT NULL)
        OR (status <> 'PROCESSED' AND processed_at IS NULL)
    )
);

COMMENT ON TABLE spectra_core.sys_operation_log_outbox IS '普通操作日志事务 outbox；PostgreSQL 是事件可靠性的事实源';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.event_id IS '操作审计事件唯一 ID';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.idempotency_key IS '业务幂等键；同一操作事件只能进入一次 outbox';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.payload IS '完整脱敏操作审计事件 payload';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.status IS 'PENDING 待处理、PROCESSING 已租约、PROCESSED 已落库、DEAD_LETTER 永久失败';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.available_at IS '下一次允许消费时间';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.processed_at IS '成功写入 sys_log 的时间';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.attempts IS '已领取处理次数';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.lease_owner IS '当前消费租约持有者';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.lease_until IS '消费租约到期时间';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.last_error IS '最近一次脱敏后的失败原因';
COMMENT ON COLUMN spectra_core.sys_operation_log_outbox.deleted IS '逻辑删除时间；消费流程不删除事件';

CREATE INDEX idx_sys_operation_log_outbox_pending
    ON spectra_core.sys_operation_log_outbox (available_at, event_id)
    WHERE processed_at IS NULL AND deleted IS NULL;

CREATE INDEX idx_sys_operation_log_outbox_lease
    ON spectra_core.sys_operation_log_outbox (status, lease_until, available_at)
    WHERE processed_at IS NULL AND deleted IS NULL;

-- 普通操作日志 worker 由现有单体调度内核以单实例循环驱动。
INSERT INTO spectra_core.scheduler_job (
    id, job_key, name, module, description, handler_key, job_type, run_scope, definition_status, desired_state,
    schedule_kind, cron_expression, fixed_delay_ms, initial_delay_ms, next_fire_at, misfire_policy, concurrency_policy,
    execution_policy, parameters, revision, created_at, updated_at, version
) VALUES
    ('00000000-0000-0000-0000-000000000407', 'system.operation-log.outbox', '普通操作日志 Outbox', 'system',
     '以数据库租约消费普通操作日志事件，成功后幂等写入 sys_log，失败进入重试或人工处置。',
     'system.operation-log.outbox', 'LOOP', 'SINGLETON', 'REGISTERED', 'RUNNING', 'FIXED_DELAY', NULL, 5000, 1000,
     NULL, 'SKIP', 'ALLOW',
     '{"heartbeatIntervalMs":3000,"leaseDurationMs":30000,"errorLogIntervalMs":60000,"batchSize":100,"maxAttempts":10}'::jsonb,
     '{}'::jsonb, 1, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP, 0)
ON CONFLICT (job_key) DO NOTHING;
