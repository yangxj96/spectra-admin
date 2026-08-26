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

package com.devops00.spectra.core.system.service.scheduler;

import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledLoopContext;
import com.devops00.spectra.common.scheduler.ScheduledLoopCycleResult;
import com.devops00.spectra.common.scheduler.ScheduledLoopHandler;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.system.service.impl.ServiceMonitorServiceImpl;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/** 服务监控采样循环的统一调度适配器。 */
@Component
public class ServiceMonitorLoopHandler implements ScheduledLoopHandler {

    private static final ScheduledJobDescriptor DESCRIPTOR = ScheduledJobDescriptor.builder()
            .jobKey("system.monitor.collect-snapshot")
            .handlerKey("system.monitor.collect-snapshot")
            .name("服务监控采样循环")
            .module("system")
            .jobType(ScheduledJobType.LOOP)
            .runScope(ScheduledRunScope.PER_INSTANCE)
            .scheduleKind(ScheduledScheduleKind.FIXED_DELAY)
            .effectType(ScheduledEffectType.DB_ONLY)
            .parameterSchema(Map.of())
            .supportedActions(Set.of("VIEW", "START", "DRAIN_STOP"))
            .executionPolicy(Map.of("heartbeatIntervalMs", 3000L, "leaseDurationMs", 30000L,
                    "errorLogIntervalMs", 60000L))
            .build();

    private final ServiceMonitorServiceImpl monitorService;

    public ServiceMonitorLoopHandler(ServiceMonitorServiceImpl monitorService) {
        this.monitorService = monitorService;
    }

    @Override
    public ScheduledJobDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public ScheduledLoopCycleResult runCycle(ScheduledLoopContext context) {
        monitorService.collectSnapshotForScheduler();
        return ScheduledLoopCycleResult.builder()
                .processed(1)
                .failed(0)
                .context(Map.of("sample", "stored"))
                .build();
    }
}
