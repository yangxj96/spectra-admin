/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.system.health;

import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** Core 健康聚合器回归。 */
class CoreHealthAggregatorTest {

    @Test
    void aggregatorUsesCommonPriorityAndReturnsAllResults() {
        var aggregator = new CoreHealthAggregator(new CoreHealthRegistry(List.of(
                contributor("unknown", DependencyHealthStatus.UNKNOWN),
                contributor("degraded", DependencyHealthStatus.DEGRADED),
                contributor("up", DependencyHealthStatus.UP))));

        var snapshot = aggregator.snapshot();

        assertEquals(DependencyHealthStatus.DEGRADED, snapshot.status());
        assertEquals(List.of("degraded", "unknown", "up"), snapshot.results()
                .stream()
                .map(DependencyHealthResult::contributorName)
                .toList());
    }

    @Test
    void aggregatorMapsContributorExceptionToSafeDownResult() {
        var aggregator = new CoreHealthAggregator(new CoreHealthRegistry(List.of(
                contributorThatThrows("database"))));

        var snapshot = aggregator.snapshot();

        assertEquals(DependencyHealthStatus.DOWN, snapshot.status());
        assertEquals("HEALTH_CHECK_FAILED", snapshot.results().getFirst().errorCode());
        org.junit.jupiter.api.Assertions.assertFalse(snapshot.results().getFirst().safeSummary().contains("secret"));
    }

    @Test
    void aggregatorMapsTimeoutToDownResult() {
        var aggregator = new CoreHealthAggregator(new CoreHealthRegistry(List.of(
                slowContributor("database", Duration.ofMillis(10)))));

        var snapshot = aggregator.snapshot();

        assertEquals(DependencyHealthStatus.DOWN, snapshot.status());
        assertEquals("HEALTH_CHECK_TIMEOUT", snapshot.results().getFirst().errorCode());
    }

    private static DependencyHealthContributor contributor(String name, DependencyHealthStatus status) {
        return contributor(name, status, Duration.ofSeconds(1), () -> result(name, status));
    }

    private static DependencyHealthContributor contributorThatThrows(String name) {
        return contributor(name, DependencyHealthStatus.UP, Duration.ofSeconds(1), () -> {
            throw new IllegalStateException("jdbc://secret-host/password=secret");
        });
    }

    private static DependencyHealthContributor slowContributor(String name, Duration timeout) {
        return contributor(name, DependencyHealthStatus.UP, timeout, () -> {
            try {
                Thread.sleep(200L);
            } catch (InterruptedException exception) {
                Thread.currentThread().interrupt();
            }
            return result(name, DependencyHealthStatus.UP);
        });
    }

    private static DependencyHealthContributor contributor(String name, DependencyHealthStatus status,
                                                           Duration timeout,
                                                           java.util.function.Supplier<DependencyHealthResult> check) {
        return new DependencyHealthContributor() {
            @Override
            public String contributorName() {
                return name;
            }

            @Override
            public String moduleName() {
                return "test";
            }

            @Override
            public String dependencyType() {
                return "MODULE";
            }

            @Override
            public Duration timeout() {
                return timeout;
            }

            @Override
            public DependencyHealthResult check() {
                return check.get();
            }
        };
    }

    private static DependencyHealthResult result(String name, DependencyHealthStatus status) {
        return new DependencyHealthResult(name, "test", "MODULE", status, Duration.ZERO,
                Instant.now(), null, "safe");
    }
}
