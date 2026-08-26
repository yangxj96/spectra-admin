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

package com.devops00.spectra.core.scheduler.service;

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerExecutionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerResolutionStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/** 离散执行状态转换和 UNKNOWN 解决规则。 */
@Component
public class ExecutionStateMachine {

    private static final Map<SchedulerExecutionStatus, EnumSet<SchedulerExecutionStatus>> TRANSITIONS = transitions();

    /** 校验并返回目标状态。 */
    public SchedulerExecutionStatus transition(SchedulerExecutionStatus current, SchedulerExecutionStatus target) {
        if (current == null
                || target == null
                || !TRANSITIONS.getOrDefault(current, EnumSet.noneOf(SchedulerExecutionStatus.class))
                        .contains(target)) {
            throw new IllegalStateException("非法执行状态转换: " + current + " -> " + target);
        }
        return target;
    }

    /** 校验 UNKNOWN 只能通过独立解决状态处理。 */
    public SchedulerResolutionStatus resolveUnknown(SchedulerExecutionStatus current,
                                                    SchedulerResolutionStatus resolutionStatus) {
        if (current != SchedulerExecutionStatus.UNKNOWN
                || resolutionStatus == null
                || resolutionStatus == SchedulerResolutionStatus.UNRESOLVED) {
            throw new IllegalStateException("只有 UNKNOWN 执行可以被人工解决");
        }
        return resolutionStatus;
    }

    private static Map<SchedulerExecutionStatus, EnumSet<SchedulerExecutionStatus>> transitions() {
        var transitions = new EnumMap<SchedulerExecutionStatus, EnumSet<SchedulerExecutionStatus>>(
                SchedulerExecutionStatus.class);
        transitions.put(SchedulerExecutionStatus.QUEUED, EnumSet.of(SchedulerExecutionStatus.RUNNING,
                SchedulerExecutionStatus.SKIPPED, SchedulerExecutionStatus.CANCELLED));
        transitions.put(SchedulerExecutionStatus.RUNNING, EnumSet.of(SchedulerExecutionStatus.SUCCEEDED,
                SchedulerExecutionStatus.FAILED, SchedulerExecutionStatus.RETRY_WAIT, SchedulerExecutionStatus.UNKNOWN));
        transitions.put(SchedulerExecutionStatus.RETRY_WAIT, EnumSet.of(SchedulerExecutionStatus.QUEUED,
                SchedulerExecutionStatus.CANCELLED));
        transitions.put(SchedulerExecutionStatus.SUCCEEDED, EnumSet.noneOf(SchedulerExecutionStatus.class));
        transitions.put(SchedulerExecutionStatus.FAILED, EnumSet.noneOf(SchedulerExecutionStatus.class));
        transitions.put(SchedulerExecutionStatus.UNKNOWN, EnumSet.noneOf(SchedulerExecutionStatus.class));
        transitions.put(SchedulerExecutionStatus.SKIPPED, EnumSet.noneOf(SchedulerExecutionStatus.class));
        transitions.put(SchedulerExecutionStatus.CANCELLED, EnumSet.noneOf(SchedulerExecutionStatus.class));
        return Map.copyOf(transitions);
    }
}
