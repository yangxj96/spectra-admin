/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at

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
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerCommandType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** 高频循环控制命令。 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "scheduler_control_command", schema = "spectra_core")
public class SchedulerControlCommandEntity extends BaseEntity {

    @TableField(value = "job_id")
    private UUID jobId;

    @TableField(value = "target_runtime_id")
    private UUID targetRuntimeId;

    @TableField(value = "target_session_key")
    private String targetSessionKey;

    @TableField(value = "expected_runtime_version")
    private Long expectedRuntimeVersion;

    @TableField(value = "command_type")
    private SchedulerCommandType commandType;

    @TableField(value = "status")
    private SchedulerCommandStatus status;

    @TableField(value = "idempotency_key")
    private String idempotencyKey;

    @TableField(value = "reason")
    private String reason;

    @TableField(value = "requested_by")
    private UUID requestedBy;

    @TableField(value = "requested_at")
    private Instant requestedAt;

    @TableField(value = "deadline_at")
    private Instant deadlineAt;

    @TableField(value = "applied_at")
    private Instant appliedAt;

    @TableField(value = "finished_at")
    private Instant finishedAt;

    @TableField(value = "result_code")
    private String resultCode;

    @TableField(value = "result_message")
    private String resultMessage;
}
