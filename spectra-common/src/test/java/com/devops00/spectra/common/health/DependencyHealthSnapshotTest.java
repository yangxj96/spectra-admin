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

package com.devops00.spectra.common.health;

import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

/** 健康聚合快照公共契约回归。 */
class DependencyHealthSnapshotTest {

    @Test
    void snapshotCopiesResultsAndKeepsCommonMetadata() {
        var checkedAt = Instant.parse("2026-08-31T08:00:00Z");
        var result = new DependencyHealthResult("database", "framework", "DATABASE",
                DependencyHealthStatus.UP, Duration.ofMillis(4), checkedAt, null, "数据库检查正常");
        var source = new java.util.ArrayList<>(List.of(result));

        var snapshot = new DependencyHealthSnapshot(DependencyHealthStatus.UP, source,
                Duration.ofMillis(5), checkedAt);
        source.clear();

        assertEquals(List.of(result), snapshot.results());
        assertEquals(DependencyHealthStatus.UP, snapshot.status());
        assertEquals(Duration.ofMillis(5), snapshot.latency());
        assertEquals(checkedAt, snapshot.checkedAt());
    }

    @Test
    void snapshotProviderExposesOneAggregationEntryPoint() throws NoSuchMethodException {
        assertEquals(DependencyHealthSnapshot.class,
                DependencyHealthSnapshotProvider.class.getMethod("snapshot").getReturnType());
    }
}
