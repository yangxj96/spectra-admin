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

/**
 * 高频循环单周期统计结果。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/26
 */
@Builder
public record ScheduledLoopCycleResult(long processed,
                                       long failed,
                                       String errorCode,
                                       String sanitizedMessage,
                                       Map<String, Object> context) {

    public ScheduledLoopCycleResult {
        if (processed < 0 || failed < 0) {
            throw new IllegalArgumentException("循环统计数量不能为负数");
        }
        context = context == null ? Map.of() : Map.copyOf(context);
    }

    public static ScheduledLoopCycleResult empty() {
        return new ScheduledLoopCycleResult(0, 0, null, null, Map.of());
    }
}
