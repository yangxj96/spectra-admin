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

package com.devops00.spectra.framework.health;

import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthSnapshot;
import com.devops00.spectra.common.health.DependencyHealthSnapshotProvider;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Actuator 健康适配器回归。 */
class ActuatorHealthContributorAdapterTest {

    @Test
    void adapterMapsCommonAggregateAndSafeDetailsToActuatorHealth() {
        var checkedAt = Instant.parse("2026-08-31T08:00:00Z");
        DependencyHealthResult result = new DependencyHealthResult("database", "framework", "DATABASE",
                DependencyHealthStatus.DEGRADED, Duration.ofMillis(8), checkedAt,
                "DATABASE_SLOW", "数据库检查存在延迟");
        DependencyHealthSnapshotProvider provider = () -> new DependencyHealthSnapshot(
                DependencyHealthStatus.DEGRADED, List.of(result), Duration.ofMillis(9), checkedAt);

        var health = new ActuatorHealthContributorAdapter(provider).health();

        assertEquals("DEGRADED", health.getStatus().getCode());
        assertEquals("DEGRADED", health.getDetails().get("status"));
        assertTrue(health.getDetails().containsKey("database"));
        assertTrue(health.getDetails().toString().contains("DATABASE_SLOW"));
    }
}
