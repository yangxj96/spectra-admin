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
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerLoopErrorStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/** 高频循环错误聚合。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "scheduler_loop_error", schema = "spectra_core", autoResultMap = true)
public class SchedulerLoopErrorEntity extends BaseEntity {

    @TableField(value = "job_id")
    private UUID jobId;

    @TableField(value = "instance_id")
    private String instanceId;

    @TableField(value = "runtime_id")
    private UUID runtimeId;

    @TableField(value = "error_fingerprint")
    private String errorFingerprint;

    @TableField(value = "error_code")
    private String errorCode;

    @TableField(value = "error_message")
    private String errorMessage;

    @TableField(value = "status")
    private SchedulerLoopErrorStatus status;

    @TableField(value = "first_seen_at")
    private Instant firstSeenAt;

    @TableField(value = "last_seen_at")
    private Instant lastSeenAt;

    @TableField(value = "last_logged_at")
    private Instant lastLoggedAt;

    @TableField(value = "occurrence_count")
    private Long occurrenceCount;

    @TableField(value = "suppressed_count")
    private Long suppressedCount;

    @TableField(value = "last_context", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> lastContext;

    @TableField(value = "resolved_by")
    private UUID resolvedBy;

    @TableField(value = "resolved_at")
    private Instant resolvedAt;

    @TableField(value = "resolution_reason")
    private String resolutionReason;
}
