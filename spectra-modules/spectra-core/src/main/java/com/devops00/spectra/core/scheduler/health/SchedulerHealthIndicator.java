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

import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import com.devops00.spectra.core.scheduler.service.SchedulerBootstrapService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/** 调度器数据库、注册表和启动门禁健康检查。 */
@Component("scheduler")
@RequiredArgsConstructor
public class SchedulerHealthIndicator implements DependencyHealthContributor {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final SchedulerBootstrapService bootstrapService;
    private final SchedulerJobMapper jobMapper;

    @Override
    public String contributorName() {
        return "scheduler";
    }

    @Override
    public String moduleName() {
        return "core";
    }

    @Override
    public String dependencyType() {
        return "SCHEDULER";
    }

    @Override
    public Duration timeout() {
        return TIMEOUT;
    }

    @Override
    public DependencyHealthResult check() {
        var start = System.nanoTime();
        var checkedAt = Instant.now();
        try {
            bootstrapService.retryStartIfNeeded();
            jobMapper.selectCount(null);
        } catch (RuntimeException exception) {
            bootstrapService.markDatabaseUnavailable();
            return result(DependencyHealthStatus.DOWN, start, checkedAt,
                    "DATABASE_UNAVAILABLE", "调度器数据库不可用");
        }
        if (!bootstrapService.isReady()) {
            var state = bootstrapService.getState();
            var disabled = "DISABLED".equalsIgnoreCase(state);
            return result(disabled ? DependencyHealthStatus.UNKNOWN : DependencyHealthStatus.DOWN,
                    start, checkedAt, disabled ? "SCHEDULER_DISABLED" : "SCHEDULER_NOT_READY",
                    disabled ? "调度器未启用" : "调度器未就绪");
        }
        return result(DependencyHealthStatus.UP, start, checkedAt, null, "调度器检查正常");
    }

    private DependencyHealthResult result(DependencyHealthStatus status, long start, Instant checkedAt,
                                          String errorCode, String safeSummary) {
        return new DependencyHealthResult(contributorName(), moduleName(), dependencyType(), status,
                Duration.ofNanos(System.nanoTime() - start), checkedAt, errorCode, safeSummary);
    }
}
