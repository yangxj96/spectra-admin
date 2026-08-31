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

package com.devops00.spectra.common.health;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.time.Duration;
import java.time.Instant;
import org.junit.jupiter.api.Test;

class DependencyHealthContractTest {

    @Test
    void shouldExposeOnlyTheUnifiedHealthStates() {
        assertEquals(
                java.util.List.of(
                        DependencyHealthStatus.UP,
                        DependencyHealthStatus.DEGRADED,
                        DependencyHealthStatus.DOWN,
                        DependencyHealthStatus.UNKNOWN),
                java.util.List.of(DependencyHealthStatus.values()));
        assertEquals(DependencyHealthStatus.DOWN, DependencyHealthStatus.fromActuator("OUT_OF_SERVICE"));
        assertEquals(DependencyHealthStatus.UNKNOWN, DependencyHealthStatus.fromActuator(""));
        assertEquals(DependencyHealthStatus.DEGRADED, DependencyHealthStatus.fromActuator("WARN"));
    }

    @Test
    void shouldKeepStableFieldsAndNormalizeSafeSummary() {
        Instant checkedAt = Instant.parse("2026-08-31T00:00:00Z");
        var result = new DependencyHealthResult(
                "upload-storage",
                "upload",
                "OBJECT_STORAGE",
                DependencyHealthStatus.DOWN,
                Duration.ofMillis(42),
                checkedAt,
                "STORAGE_UNAVAILABLE",
                "provider check failed\r\ncredentials omitted");

        assertEquals("upload-storage", result.contributorName());
        assertEquals("upload", result.moduleName());
        assertEquals("OBJECT_STORAGE", result.dependencyType());
        assertEquals(Duration.ofMillis(42), result.latency());
        assertEquals(checkedAt, result.checkedAt());
        assertEquals("STORAGE_UNAVAILABLE", result.errorCode());
        assertEquals("provider check failed  credentials omitted", result.safeSummary());
    }

    @Test
    void shouldRejectInvalidContributorIdentityAndLatency() {
        assertThrows(IllegalArgumentException.class, () -> new DependencyHealthResult(
                " ", "core", "DATABASE", DependencyHealthStatus.UP, Duration.ZERO, null, null, null));
        assertThrows(IllegalArgumentException.class, () -> new DependencyHealthResult(
                "database", "core", "DATABASE", DependencyHealthStatus.UP, Duration.ofMillis(-1), null, null, null));
    }

    @Test
    void contributorContractMustExposeSynchronousCheckAndTimeout() throws NoSuchMethodException {
        assertEquals(String.class, DependencyHealthContributor.class.getMethod("contributorName").getReturnType());
        assertEquals(String.class, DependencyHealthContributor.class.getMethod("moduleName").getReturnType());
        assertEquals(String.class, DependencyHealthContributor.class.getMethod("dependencyType").getReturnType());
        assertEquals(Duration.class, DependencyHealthContributor.class.getMethod("timeout").getReturnType());
        assertEquals(DependencyHealthResult.class, DependencyHealthContributor.class.getMethod("check").getReturnType());
    }
}
