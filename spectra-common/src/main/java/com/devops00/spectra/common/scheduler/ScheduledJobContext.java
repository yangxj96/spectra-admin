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

package com.devops00.spectra.common.scheduler;

import lombok.Builder;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 离散任务处理器执行上下文。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/26
 */
@Builder
public record ScheduledJobContext(UUID executionId,
                                  String jobKey,
                                  String handlerKey,
                                  long jobRevision,
                                  String handlerVersion,
                                  String fireKey,
                                  String instanceId,
                                  Instant scheduledAt,
                                  Instant deadline,
                                  Map<String, Object> parameters,
                                  String actorType,
                                  String actorId) {

    public ScheduledJobContext {
        if (executionId == null || jobKey == null || handlerKey == null || fireKey == null || scheduledAt == null) {
            throw new IllegalArgumentException("执行上下文缺少必填标识或计划时间");
        }
        parameters = parameters == null ? Map.of() : Map.copyOf(parameters);
        actorType = actorType == null || actorType.isBlank() ? "SYSTEM_JOB" : actorType;
        actorId = actorId == null || actorId.isBlank() ? jobKey : actorId;
    }
}
