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

package com.devops00.spectra.core.scheduler.javabean.vo;

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationSource;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerOperationType;
import lombok.Builder;

import java.time.Instant;
import java.util.UUID;

/** 统一调度操作记录视图。 */
@Builder
public record SchedulerOperationVO(UUID id, UUID jobId, UUID executionId, SchedulerOperationType operationType,
                                   SchedulerOperationSource source, SchedulerOperationStatus status,
                                   String idempotencyKey, String reason, UUID requestedBy, Instant requestedAt,
                                   Instant finishedAt, String resultCode, String resultMessage) {
}
