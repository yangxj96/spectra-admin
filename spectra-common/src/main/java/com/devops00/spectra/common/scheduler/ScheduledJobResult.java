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

/**
 * 离散任务处理结果。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/26
 */
@Builder
public record ScheduledJobResult(Status status,
                                 String errorCode,
                                 String sanitizedMessage,
                                 Map<String, Object> resultSummary,
                                 Instant retryAt) {

    public enum Status {
        SUCCEEDED,
        FAILED,
        RETRYABLE,
        UNKNOWN
    }

    public ScheduledJobResult {
        if (status == null) {
            throw new IllegalArgumentException("执行结果状态不能为空");
        }
        resultSummary = resultSummary == null ? Map.of() : Map.copyOf(resultSummary);
    }
}
