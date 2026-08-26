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
import com.devops00.spectra.common.scheduler.ScheduledJobContext;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobHandler;
import com.devops00.spectra.common.scheduler.ScheduledJobResult;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.core.system.service.impl.ServiceMonitorDiagnosticServiceImpl;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/** 服务监控诊断文件清理的统一调度适配器。 */
@Component
public class ServiceMonitorDiagnosticCleanupHandler implements ScheduledJobHandler {

    private static final ScheduledJobDescriptor DESCRIPTOR = ScheduledJobDescriptor.builder()
            .jobKey("system.monitor.diagnostic-cleanup")
            .handlerKey("system.monitor.diagnostic-cleanup")
            .name("监控诊断清理")
            .module("system")
            .jobType(ScheduledJobType.SYSTEM)
            .runScope(ScheduledRunScope.SINGLETON)
            .scheduleKind(ScheduledScheduleKind.FIXED_DELAY)
            .effectType(ScheduledEffectType.DB_ONLY)
            .parameterSchema(Map.of())
            .supportedActions(Set.of("VIEW", "TRIGGER"))
            .executionPolicy(Map.of("timeoutMs", 300000L, "leaseDurationMs", 600000L, "maxAttempts", 1))
            .build();

    private final ServiceMonitorDiagnosticServiceImpl diagnosticService;

    public ServiceMonitorDiagnosticCleanupHandler(ServiceMonitorDiagnosticServiceImpl diagnosticService) {
        this.diagnosticService = diagnosticService;
    }

    @Override
    public ScheduledJobDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public ScheduledJobResult execute(ScheduledJobContext context) {
        diagnosticService.cleanupExpiredTasks();
        return ScheduledJobResult.builder()
                .status(ScheduledJobResult.Status.SUCCEEDED)
                .build();
    }
}
