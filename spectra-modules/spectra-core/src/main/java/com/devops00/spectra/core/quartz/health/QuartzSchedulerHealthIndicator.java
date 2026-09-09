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

package com.devops00.spectra.core.quartz.health;

import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import com.devops00.spectra.core.quartz.configuration.QuartzSchedulerLifecycle;
import lombok.RequiredArgsConstructor;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

/** Quartz JDBC Cluster 状态和数据库就绪状态健康检查。 */
@Component("quartzScheduler")
@RequiredArgsConstructor
public class QuartzSchedulerHealthIndicator implements DependencyHealthContributor {

    private static final Duration TIMEOUT = Duration.ofSeconds(3);

    private final QuartzSchedulerLifecycle lifecycle;
    private final Scheduler scheduler;

    /** 返回固定 contributor 名称。 */
    @Override
    public String contributorName() {
        return "quartzScheduler";
    }

    /** 返回所属模块。 */
    @Override
    public String moduleName() {
        return "core";
    }

    /** 返回依赖类型。 */
    @Override
    public String dependencyType() {
        return "SCHEDULER";
    }

    /** 返回健康探针预算。 */
    @Override
    public Duration timeout() {
        return TIMEOUT;
    }

    /** 返回不包含连接串和凭据的 Quartz 状态摘要。 */
    @Override
    public DependencyHealthResult check() {
        var checkedAt = Instant.now();
        var start = System.nanoTime();
        if (!lifecycle.isReady()) {
            return result(DependencyHealthStatus.DOWN, start, checkedAt,
                    lifecycle.getFailureCode(), "Quartz Scheduler 未就绪: " + lifecycle.getState());
        }
        try {
            var metadata = scheduler.getMetaData();
            if (!metadata.isJobStoreSupportsPersistence() || !metadata.isJobStoreClustered()) {
                return result(DependencyHealthStatus.DOWN, start, checkedAt,
                        "QUARTZ_STORE_INVALID", "Quartz 未使用持久化集群 JobStore");
            }
            var summary = "state=" + lifecycle.getState()
                    + ", started=" + metadata.isStarted()
                    + ", standby=" + metadata.isInStandbyMode()
                    + ", shutdown=" + metadata.isShutdown()
                    + ", instance=" + metadata.getSchedulerInstanceId()
                    + ", clustered=" + metadata.isJobStoreClustered();
            return result(DependencyHealthStatus.UP, start, checkedAt, null, summary);
        } catch (SchedulerException exception) {
            return result(DependencyHealthStatus.DOWN, start, checkedAt,
                    "QUARTZ_STATUS_UNAVAILABLE", "Quartz Scheduler 状态不可用");
        }
    }

    /** 组装统一健康结果并保持摘要脱敏。 */
    private DependencyHealthResult result(DependencyHealthStatus status, long start, Instant checkedAt,
                                          String errorCode, String summary) {
        return new DependencyHealthResult(contributorName(), moduleName(), dependencyType(), status,
                Duration.ofNanos(System.nanoTime() - start), checkedAt, errorCode, summary);
    }
}
