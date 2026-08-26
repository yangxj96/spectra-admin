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

-- OPS/SYSTEM 人工调度操作审计；LOOP 控制命令仍以 scheduler_control_command 为事实源。
CREATE TABLE spectra_core.scheduler_operation_audit (
    id                uuid PRIMARY KEY,
    job_id            uuid NOT NULL,
    execution_id      uuid,
    operation_type    varchar(30) NOT NULL,
    status            varchar(30) NOT NULL DEFAULT 'SUCCEEDED',
    idempotency_key   varchar(300) NOT NULL,
    reason            text NOT NULL,
    requested_by      uuid,
    requested_at      timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    finished_at       timestamp(6) with time zone,
    result_code       varchar(100),
    result_message    text,
    created_by        uuid,
    created_at        timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by        uuid,
    updated_at        timestamp(6) with time zone NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted           timestamp(6) with time zone,
    version           bigint NOT NULL DEFAULT 0,
    CONSTRAINT uk_scheduler_operation_audit_idempotency_key UNIQUE (idempotency_key),
    CONSTRAINT fk_scheduler_operation_audit_job FOREIGN KEY (job_id)
        REFERENCES spectra_core.scheduler_job (id) ON DELETE RESTRICT,
    CONSTRAINT fk_scheduler_operation_audit_execution FOREIGN KEY (execution_id)
        REFERENCES spectra_core.scheduler_execution (id) ON DELETE RESTRICT,
    CONSTRAINT ck_scheduler_operation_audit_type CHECK (operation_type IN (
        'CREATE', 'UPDATE', 'ENABLE', 'DISABLE', 'ARCHIVE', 'REREGISTER',
        'TRIGGER', 'RETRY', 'CANCEL', 'RESOLVE'
    )),
    CONSTRAINT ck_scheduler_operation_audit_status CHECK (status IN (
        'REQUESTED', 'APPLYING', 'APPLIED', 'SUCCEEDED', 'FAILED', 'TIMEOUT'
    )),
    CONSTRAINT ck_scheduler_operation_audit_reason CHECK (btrim(reason) <> '')
);

COMMENT ON TABLE spectra_core.scheduler_operation_audit IS 'OPS/SYSTEM 人工调度操作审计记录表；LOOP 控制命令保留在 scheduler_control_command';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.id IS '调度操作审计主键';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.job_id IS '关联的调度任务定义主键';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.execution_id IS '关联的离散执行主键；任务定义操作为空';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.operation_type IS '操作类型：CREATE、UPDATE、ENABLE、DISABLE、ARCHIVE、REREGISTER、TRIGGER、RETRY、CANCEL 或 RESOLVE';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.status IS '操作状态：REQUESTED、APPLYING、APPLIED、SUCCEEDED、FAILED 或 TIMEOUT';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.idempotency_key IS '操作幂等键';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.reason IS '提交操作的控制原因';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.requested_by IS '操作申请人主键';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.requested_at IS '操作申请时间';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.finished_at IS '操作完成时间';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.result_code IS '操作结果编码';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.result_message IS '操作结果说明';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.created_by IS '创建人主键';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.created_at IS '创建时间';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.updated_by IS '最后修改人主键';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.updated_at IS '最后修改时间';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.deleted IS 'BaseEntity 兼容字段；操作记录不使用该字段过滤';
COMMENT ON COLUMN spectra_core.scheduler_operation_audit.version IS '乐观锁版本号';

CREATE INDEX idx_scheduler_operation_audit_job_requested
    ON spectra_core.scheduler_operation_audit (job_id, requested_at DESC, id DESC);
