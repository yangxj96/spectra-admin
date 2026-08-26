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

package com.devops00.spectra.notification.dispatch;

import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledLoopContext;
import com.devops00.spectra.common.scheduler.ScheduledLoopCycleResult;
import com.devops00.spectra.common.scheduler.ScheduledLoopHandler;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.Set;

/** 通知任务 Worker 的 LOOP 调度适配器。 */
@Component
public class NotificationTaskLoopHandler implements ScheduledLoopHandler {

    private static final ScheduledJobDescriptor DESCRIPTOR = ScheduledJobDescriptor.builder()
            .jobKey("notification.task-worker")
            .handlerKey("notification.task-worker")
            .name("通知任务循环")
            .module("notification")
            .jobType(ScheduledJobType.LOOP)
            .runScope(ScheduledRunScope.PER_INSTANCE)
            .scheduleKind(ScheduledScheduleKind.FIXED_DELAY)
            .effectType(ScheduledEffectType.EXTERNAL_UNKNOWN)
            .parameterSchema(Map.of())
            .supportedActions(Set.of("VIEW", "START", "DRAIN_STOP"))
            .executionPolicy(Map.of("heartbeatIntervalMs", 2000L, "leaseDurationMs", 15000L,
                    "errorLogIntervalMs", 60000L))
            .build();

    private final NotificationTaskWorker worker;
    private volatile Instant lastLeaseRecoveryAt;

    public NotificationTaskLoopHandler(NotificationTaskWorker worker) {
        this.worker = worker;
    }

    @Override
    public ScheduledJobDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public ScheduledLoopCycleResult runCycle(ScheduledLoopContext context) {
        var now = Instant.now();
        var lastRecovery = lastLeaseRecoveryAt;
        var recoverLeases = lastRecovery == null || !lastRecovery.plusSeconds(30).isAfter(now);
        var processed = worker.processPending(50, context.instanceId(), recoverLeases);
        if (recoverLeases) {
            lastLeaseRecoveryAt = now;
        }
        return ScheduledLoopCycleResult.builder()
                .processed(processed)
                .failed(0)
                .context(Map.of("selected", processed))
                .build();
    }
}
