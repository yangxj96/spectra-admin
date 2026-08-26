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

package com.devops00.spectra.core.scheduler.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.mybatis.PgJsonbTypeHandler;
import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.common.scheduler.ScheduledTriggerType;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerExecutionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerResolutionStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** 离散调度执行记录。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "scheduler_execution", schema = "spectra_core", autoResultMap = true)
public class SchedulerExecutionEntity extends BaseEntity {

    @TableField(value = "job_id")
    private UUID jobId;

    @TableField(value = "fire_key")
    private String fireKey;

    @TableField(value = "trigger_type")
    private ScheduledTriggerType triggerType;

    @TableField(value = "status")
    private SchedulerExecutionStatus status;

    @TableField(value = "job_revision")
    private Long jobRevision;

    @TableField(value = "handler_version")
    private String handlerVersion;

    @TableField(value = "schedule_kind_snapshot")
    private ScheduledScheduleKind scheduleKindSnapshot;

    @TableField(value = "schedule_expression_snapshot")
    private String scheduleExpressionSnapshot;

    @TableField(value = "parameters_snapshot", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> parametersSnapshot;

    @TableField(value = "effect_type")
    private ScheduledEffectType effectType;

    @TableField(value = "scheduled_at")
    private Instant scheduledAt;

    @TableField(value = "queued_at")
    private Instant queuedAt;

    @TableField(value = "started_at")
    private Instant startedAt;

    @TableField(value = "finished_at")
    private Instant finishedAt;

    @TableField(value = "next_retry_at")
    private Instant nextRetryAt;

    @TableField(value = "deadline_at")
    private Instant deadlineAt;

    @TableField(value = "attempt_no")
    private Integer attemptNo;

    @TableField(value = "max_attempts")
    private Integer maxAttempts;

    @TableField(value = "locked_by")
    private String lockedBy;

    @TableField(value = "locked_at")
    private Instant lockedAt;

    @TableField(value = "lease_expires_at")
    private Instant leaseExpiresAt;

    @TableField(value = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    @TableField(value = "last_error_code")
    private String lastErrorCode;

    @TableField(value = "last_error_message")
    private String lastErrorMessage;

    @TableField(value = "result_summary", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> resultSummary;

    @TableField(value = "original_execution_id")
    private UUID originalExecutionId;

    @TableField(value = "resolution_status")
    private SchedulerResolutionStatus resolutionStatus;

    @TableField(value = "resolution_reason")
    private String resolutionReason;

    @TableField(value = "resolved_by")
    private UUID resolvedBy;

    @TableField(value = "resolved_at")
    private Instant resolvedAt;
}
