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

package com.devops00.spectra.core.system.health;

import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthSnapshot;
import com.devops00.spectra.common.health.DependencyHealthSnapshotProvider;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Core 统一健康聚合器。
 *
 * <p>聚合器是唯一执行 contributor、处理超时/异常并计算总状态的入口。下游适配器只消费快照，不重新执行
 * 检查，也不定义第二套状态优先级。</p>
 */
@Component
public class CoreHealthAggregator implements DependencyHealthSnapshotProvider {

    private final CoreHealthRegistry registry;
    private final ExecutorService checkExecutor = Executors.newVirtualThreadPerTaskExecutor();

    public CoreHealthAggregator(CoreHealthRegistry registry) {
        this.registry = registry;
    }

    @Override
    public DependencyHealthSnapshot snapshot() {
        var start = System.nanoTime();
        var results = registry.contributors()
                .stream()
                .map(this::checkContributor)
                .sorted(java.util.Comparator.comparing(DependencyHealthResult::contributorName))
                .toList();
        var status = results.isEmpty()
                ? DependencyHealthStatus.UNKNOWN
                : results.stream()
                        .map(DependencyHealthResult::status)
                        .reduce(DependencyHealthStatus.UP, CoreHealthAggregator::combine);
        return new DependencyHealthSnapshot(status, results,
                Duration.ofNanos(System.nanoTime() - start), Instant.now());
    }

    @PreDestroy
    void shutdown() {
        checkExecutor.shutdownNow();
    }

    private DependencyHealthResult checkContributor(DependencyHealthContributor contributor) {
        var start = System.nanoTime();
        var metadata = registry.metadata(contributor.contributorName());
        var future = checkExecutor.submit(contributor::check);
        try {
            var result = future.get(metadata.timeout().toNanos(), TimeUnit.NANOSECONDS);
            if (result == null) {
                return failure(contributor, start, "HEALTH_CHECK_EMPTY", "健康检查未返回结果");
            }
            if (!metadata.contributorName().equals(result.contributorName())
                    || !metadata.moduleName().equals(result.moduleName())
                    || !metadata.dependencyType().equals(result.dependencyType())) {
                return failure(contributor, start, "HEALTH_CHECK_INVALID_RESULT", "健康检查返回的归属信息无效");
            }
            return result;
        } catch (TimeoutException exception) {
            future.cancel(true);
            return failure(contributor, start, "HEALTH_CHECK_TIMEOUT", "健康检查超时");
        } catch (InterruptedException exception) {
            future.cancel(true);
            Thread.currentThread().interrupt();
            return failure(contributor, start, "HEALTH_CHECK_INTERRUPTED", "健康检查被中断");
        } catch (ExecutionException | RuntimeException exception) {
            return failure(contributor, start, "HEALTH_CHECK_FAILED", "健康检查执行失败");
        }
    }

    private static DependencyHealthResult failure(DependencyHealthContributor contributor, long start,
                                                  String errorCode, String safeSummary) {
        return new DependencyHealthResult(contributor.contributorName(), contributor.moduleName(),
                contributor.dependencyType(), DependencyHealthStatus.DOWN,
                Duration.ofNanos(System.nanoTime() - start), Instant.now(), errorCode, safeSummary);
    }

    private static DependencyHealthStatus combine(DependencyHealthStatus left, DependencyHealthStatus right) {
        if (left == DependencyHealthStatus.DOWN || right == DependencyHealthStatus.DOWN) {
            return DependencyHealthStatus.DOWN;
        }
        if (left == DependencyHealthStatus.DEGRADED || right == DependencyHealthStatus.DEGRADED) {
            return DependencyHealthStatus.DEGRADED;
        }
        if (left == DependencyHealthStatus.UNKNOWN || right == DependencyHealthStatus.UNKNOWN) {
            return DependencyHealthStatus.UNKNOWN;
        }
        return DependencyHealthStatus.UP;
    }
}
