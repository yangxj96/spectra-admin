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

CREATE TABLE spectra_core.quartz_job_execution_history
(
    id                  UUID DEFAULT uuidv7() CONSTRAINT __canonical_quartz_job_execution_history_id_not_null NOT NULL,
    fire_instance_id    VARCHAR(255) CONSTRAINT __canonical_quartz_job_execution_history_fire_instance_id_not_null NOT NULL,
    job_key             VARCHAR(190) CONSTRAINT __canonical_quartz_job_execution_history_job_key_not_null NOT NULL,
    trigger_key         VARCHAR(190) CONSTRAINT __canonical_quartz_job_execution_history_trigger_key_not_null NOT NULL,
    job_type            VARCHAR(128) CONSTRAINT __canonical_quartz_job_execution_history_job_type_not_null NOT NULL,
    job_class_name      VARCHAR(512) CONSTRAINT __canonical_quartz_job_execution_history_job_class_name_not_null NOT NULL,
    trigger_type        VARCHAR(32) CONSTRAINT __canonical_quartz_job_execution_history_trigger_type_not_null NOT NULL,
    status              VARCHAR(32) CONSTRAINT __canonical_quartz_job_execution_history_status_not_null NOT NULL,
    scheduled_fire_at   TIMESTAMP(6) WITH TIME ZONE,
    actual_fire_at      TIMESTAMP(6) WITH TIME ZONE CONSTRAINT __canonical_quartz_job_execution_history_actual_fire_at_not_null NOT NULL,
    started_at          TIMESTAMP(6) WITH TIME ZONE CONSTRAINT __canonical_quartz_job_execution_history_started_at_not_null NOT NULL,
    finished_at         TIMESTAMP(6) WITH TIME ZONE,
    duration_ms         BIGINT,
    scheduler_instance  VARCHAR(255),
    correlation_id      VARCHAR(128),
    parameter_version   VARCHAR(64),
    parameter_sha256    VARCHAR(64),
    result_summary      VARCHAR(2000),
    error_code          VARCHAR(128),
    error_message       VARCHAR(2000),
    created_by          UUID,
    created_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by          UUID,
    updated_at          TIMESTAMP(6) WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted             TIMESTAMP(6) WITH TIME ZONE,
    version             BIGINT DEFAULT 0 CONSTRAINT __canonical_quartz_job_execution_history_version_not_null NOT NULL,
    CONSTRAINT pk_quartz_job_execution_history PRIMARY KEY (id),
    CONSTRAINT uk_quartz_job_execution_history_fire_instance_id UNIQUE (fire_instance_id),
    CONSTRAINT ck_quartz_history_status CHECK
        (status IN ('RUNNING', 'SUCCEEDED', 'FAILED', 'VETOED', 'ABANDONED')),
    CONSTRAINT ck_quartz_history_duration CHECK (duration_ms IS NULL OR duration_ms >= 0),
    CONSTRAINT ck_quartz_history_finished_order CHECK
        (finished_at IS NULL OR finished_at >= started_at),
    CONSTRAINT ck_quartz_history_terminal_finished CHECK
        ((status = 'RUNNING' AND finished_at IS NULL)
            OR (status IN ('SUCCEEDED', 'FAILED', 'VETOED', 'ABANDONED') AND finished_at IS NOT NULL))
);

CREATE INDEX idx_quartz_history_job_started
    ON spectra_core.quartz_job_execution_history (job_key, started_at DESC);
CREATE INDEX idx_quartz_history_trigger_started
    ON spectra_core.quartz_job_execution_history (trigger_key, started_at DESC);
CREATE INDEX idx_quartz_history_status_started
    ON spectra_core.quartz_job_execution_history (status, started_at DESC);
CREATE INDEX idx_quartz_history_created_at
    ON spectra_core.quartz_job_execution_history (created_at);

COMMENT ON TABLE spectra_core.quartz_job_execution_history IS 'Quartz Job 执行历史；只保存脱敏摘要，不保存 JobDataMap';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.id IS '执行历史 UUID 主键';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.fire_instance_id IS 'Quartz fireInstanceId，单次执行唯一标识';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.job_key IS 'Quartz JobKey 的稳定字符串';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.trigger_key IS 'Quartz TriggerKey 的稳定字符串';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.job_type IS '代码白名单中的 Job 类型键';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.job_class_name IS '实际执行的白名单 Job 类名';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.trigger_type IS '触发来源类型，例如 CRON、SIMPLE 或 MANUAL';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.parameter_sha256 IS '已校验非敏感参数的摘要，不保存参数原文';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.status IS '执行状态：RUNNING、SUCCEEDED、FAILED、VETOED 或 ABANDONED';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.scheduled_fire_at IS 'Quartz 原计划触发时间';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.actual_fire_at IS 'Quartz 实际触发时间';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.started_at IS '业务 Job 开始执行时间';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.finished_at IS '业务 Job 结束执行时间';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.duration_ms IS '业务 Job 执行耗时毫秒数';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.scheduler_instance IS '执行该 Job 的 Quartz 实例标识';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.correlation_id IS '应用链路关联标识';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.parameter_version IS '参数 schema 版本';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.result_summary IS '脱敏后的执行结果摘要';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.error_code IS '脱敏后的稳定错误编码';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.error_message IS '脱敏后的错误说明';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.created_by IS '创建人主键；系统调度执行可为空';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.updated_by IS '最后更新人主键；系统内部变更可为空';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.updated_at IS '最后更新时间';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.deleted IS 'BaseEntity 兼容字段；历史清理直接物理删除，不通过该字段过滤';
COMMENT ON COLUMN spectra_core.quartz_job_execution_history.version IS '乐观锁版本号；历史记录状态回写使用版本控制';
