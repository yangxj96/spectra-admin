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

package com.devops00.spectra.core.scheduler.javabean.domain;

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationSource;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/** 统一调度操作查询的数据库行模型。 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SchedulerOperationHistoryRow {

    private UUID id;
    private UUID jobId;
    private UUID executionId;
    private SchedulerOperationType operationType;
    private SchedulerOperationStatus status;
    private String idempotencyKey;
    private String reason;
    private UUID requestedBy;
    private Instant requestedAt;
    private Instant finishedAt;
    private String resultCode;
    private String resultMessage;
    private SchedulerOperationSource source;
}
