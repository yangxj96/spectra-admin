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

import java.util.Map;
import java.util.Set;

/**
 * 代码注册的任务能力描述。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/26
 */
@Builder
public record ScheduledJobDescriptor(String jobKey,
                                     String handlerKey,
                                     String name,
                                     String module,
                                     ScheduledJobType jobType,
                                     ScheduledRunScope runScope,
                                     ScheduledScheduleKind scheduleKind,
                                     ScheduledEffectType effectType,
                                     Map<String, Object> parameterSchema,
                                     Set<String> supportedActions,
                                     Map<String, Object> executionPolicy) {

    public ScheduledJobDescriptor {
        if (jobKey == null || jobKey.isBlank()) {
            throw new IllegalArgumentException("jobKey 不能为空");
        }
        if (handlerKey == null || handlerKey.isBlank()) {
            throw new IllegalArgumentException("handlerKey 不能为空");
        }
        if (jobType == null || runScope == null || scheduleKind == null || effectType == null) {
            throw new IllegalArgumentException("任务类型、运行范围、调度类型和副作用类型不能为空");
        }
        parameterSchema = parameterSchema == null ? Map.of() : Map.copyOf(parameterSchema);
        supportedActions = supportedActions == null ? Set.of() : Set.copyOf(supportedActions);
        executionPolicy = executionPolicy == null ? Map.of() : Map.copyOf(executionPolicy);
    }
}
