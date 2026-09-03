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

package com.devops00.spectra.core.notification.service;

import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobContext;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobHandler;
import com.devops00.spectra.common.scheduler.ScheduledJobResult;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;

/** 通知敏感载荷清理的统一调度适配器。 */
@Component
public class NotificationCleanupScheduledHandler implements ScheduledJobHandler {

    private static final ScheduledJobDescriptor DESCRIPTOR = ScheduledJobDescriptor.builder()
            .jobKey("notification.cleanup-sensitive-payload")
            .handlerKey("notification.cleanup-sensitive-payload")
            .name("通知敏感载荷清理")
            .module("notification")
            .jobType(ScheduledJobType.SYSTEM)
            .runScope(ScheduledRunScope.SINGLETON)
            .scheduleKind(ScheduledScheduleKind.FIXED_DELAY)
            .effectType(ScheduledEffectType.DB_ONLY)
            .parameterSchema(Map.of())
            .supportedActions(Set.of("VIEW", "TRIGGER"))
            .executionPolicy(Map.of("timeoutMs", 300000L, "leaseDurationMs", 600000L, "maxAttempts", 1))
            .build();

    private final NotificationCleanupService cleanupService;

    public NotificationCleanupScheduledHandler(NotificationCleanupService cleanupService) {
        this.cleanupService = cleanupService;
    }

    @Override
    public ScheduledJobDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @Override
    public ScheduledJobResult execute(ScheduledJobContext context) {
        var result = cleanupService.cleanupSensitivePayloads();
        return ScheduledJobResult.builder()
                .status(ScheduledJobResult.Status.SUCCEEDED)
                .resultSummary(Map.of("requestCount", result.requestCount(), "taskCount", result.taskCount()))
                .build();
    }
}
