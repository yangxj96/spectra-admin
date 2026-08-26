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

import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerRuntimeStatus;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.EnumSet;
import java.util.Map;

/** 高频循环运行会话状态机。 */
@Component
public class LoopStateMachine {

    private static final Map<SchedulerRuntimeStatus, EnumSet<SchedulerRuntimeStatus>> TRANSITIONS = transitions();

    /** 校验并返回目标状态。新会话通过新记录进入 STARTING。 */
    public SchedulerRuntimeStatus transition(SchedulerRuntimeStatus current, SchedulerRuntimeStatus target) {
        if (current == null
                || target == null
                || !TRANSITIONS.getOrDefault(current, EnumSet.noneOf(SchedulerRuntimeStatus.class)).contains(target)) {
            throw new IllegalStateException("非法循环状态转换: " + current + " -> " + target);
        }
        return target;
    }

    private static Map<SchedulerRuntimeStatus, EnumSet<SchedulerRuntimeStatus>> transitions() {
        var transitions = new EnumMap<SchedulerRuntimeStatus, EnumSet<SchedulerRuntimeStatus>>(
                SchedulerRuntimeStatus.class);
        transitions.put(SchedulerRuntimeStatus.STARTING, EnumSet.of(
                SchedulerRuntimeStatus.RUNNING,
                SchedulerRuntimeStatus.CRASHED,
                SchedulerRuntimeStatus.UNKNOWN,
                SchedulerRuntimeStatus.STOPPED));
        transitions.put(SchedulerRuntimeStatus.RUNNING, EnumSet.of(
                SchedulerRuntimeStatus.DEGRADED,
                SchedulerRuntimeStatus.DRAINING,
                SchedulerRuntimeStatus.STOPPED,
                SchedulerRuntimeStatus.CRASHED,
                SchedulerRuntimeStatus.UNKNOWN));
        transitions.put(SchedulerRuntimeStatus.DEGRADED, EnumSet.of(
                SchedulerRuntimeStatus.RUNNING,
                SchedulerRuntimeStatus.DRAINING,
                SchedulerRuntimeStatus.STOPPED,
                SchedulerRuntimeStatus.CRASHED,
                SchedulerRuntimeStatus.UNKNOWN));
        transitions.put(SchedulerRuntimeStatus.DRAINING, EnumSet.of(
                SchedulerRuntimeStatus.STOPPED,
                SchedulerRuntimeStatus.CRASHED,
                SchedulerRuntimeStatus.UNKNOWN));
        transitions.put(SchedulerRuntimeStatus.STOPPED, EnumSet.noneOf(SchedulerRuntimeStatus.class));
        transitions.put(SchedulerRuntimeStatus.CRASHED, EnumSet.noneOf(SchedulerRuntimeStatus.class));
        transitions.put(SchedulerRuntimeStatus.UNKNOWN, EnumSet.noneOf(SchedulerRuntimeStatus.class));
        return Map.copyOf(transitions);
    }
}
