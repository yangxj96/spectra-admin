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

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerResolutionStatus;

import java.util.UUID;

/** UNKNOWN 执行的人工解决命令。 */
public record ExecutionResolution(SchedulerResolutionStatus status, String reason, UUID resolvedBy) {

    public ExecutionResolution {
        if (status == null
                || status == SchedulerResolutionStatus.UNRESOLVED
                || reason == null
                || reason.isBlank()) {
            throw new IllegalArgumentException("UNKNOWN 解决状态和原因不能为空");
        }
    }
}
