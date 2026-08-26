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

package com.devops00.spectra.core.scheduler.health;

import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.service.ScheduledJobRegistry;
import com.devops00.spectra.core.scheduler.service.SchedulerBootstrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

/** 调度器数据库、注册表和启动门禁健康检查。 */
@Component("scheduler")
@RequiredArgsConstructor
public class SchedulerHealthIndicator implements HealthIndicator {

    private final SchedulerBootstrapService bootstrapService;
    private final SchedulerJobMapper jobMapper;
    private final ScheduledJobRegistry registry;

    @Override
    public Health health() {
        bootstrapService.retryStartIfNeeded();
        long jobCount;
        try {
            jobCount = jobMapper.selectCount(null);
        } catch (RuntimeException exception) {
            bootstrapService.markDatabaseUnavailable();
            return Health.down()
                    .withDetail("reason", "DATABASE_UNAVAILABLE")
                    .withDetail("state", bootstrapService.getState())
                    .build();
        }
        var builder = bootstrapService.isReady() ? Health.up() : Health.down();
        return builder
                .withDetail("state", bootstrapService.getState())
                .withDetail("ready", bootstrapService.isReady())
                .withDetail("running", bootstrapService.isRunning())
                .withDetail("jobCount", jobCount)
                .withDetail("registeredHandlerCount", registry.descriptors().size())
                .withDetail("failureCode", bootstrapService.getFailureCode())
                .withDetail("systemTimezone", bootstrapService.getSystemZone().getId())
                .build();
    }
}
