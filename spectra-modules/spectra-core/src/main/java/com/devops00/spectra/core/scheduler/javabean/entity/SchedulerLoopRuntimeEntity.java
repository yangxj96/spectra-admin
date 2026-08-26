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
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerRuntimeStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** 高频循环运行会话。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "scheduler_loop_runtime", schema = "spectra_core")
public class SchedulerLoopRuntimeEntity extends BaseEntity {

    @TableField(value = "job_id")
    private UUID jobId;

    @TableField(value = "session_key")
    private String sessionKey;

    @TableField(value = "instance_id")
    private String instanceId;

    @TableField(value = "status")
    private SchedulerRuntimeStatus status;

    @TableField(value = "started_at")
    private Instant startedAt;

    @TableField(value = "stopped_at")
    private Instant stoppedAt;

    @TableField(value = "last_heartbeat_at")
    private Instant lastHeartbeatAt;

    @TableField(value = "lease_expires_at")
    private Instant leaseExpiresAt;

    @TableField(value = "last_cycle_at")
    private Instant lastCycleAt;

    @TableField(value = "last_progress_at")
    private Instant lastProgressAt;

    @TableField(value = "drain_deadline_at")
    private Instant drainDeadlineAt;

    @TableField(value = "total_cycles")
    private Long totalCycles;

    @TableField(value = "total_processed")
    private Long totalProcessed;

    @TableField(value = "total_failed")
    private Long totalFailed;

    @TableField(value = "consecutive_error_count")
    private Long consecutiveErrorCount;

    @TableField(value = "last_error_code")
    private String lastErrorCode;

    @TableField(value = "last_error_message")
    private String lastErrorMessage;

    @TableField(value = "state_reason")
    private String stateReason;
}
