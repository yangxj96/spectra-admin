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

package com.devops00.spectra.core.quartz.javabean.vo;

import com.devops00.spectra.core.quartz.javabean.enums.QuartzExecutionHistoryStatus;

import java.time.LocalDateTime;
import java.util.UUID;

/** Quartz 单次执行历史的脱敏展示对象。 */
public record QuartzExecutionHistoryVO(
                                       UUID id,
                                       String fireInstanceId,
                                       String jobKey,
                                       String triggerKey,
                                       String jobType,
                                       String jobClassName,
                                       String triggerType,
                                       QuartzExecutionHistoryStatus status,
                                       LocalDateTime scheduledFireAt,
                                       LocalDateTime actualFireAt,
                                       LocalDateTime startedAt,
                                       LocalDateTime finishedAt,
                                       Long durationMs,
                                       String schedulerInstance,
                                       String correlationId,
                                       String parameterVersion,
                                       String parameterSha256,
                                       String resultSummary,
                                       String errorCode,
                                       String errorMessage) {
}
