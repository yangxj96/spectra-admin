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
import static org.junit.jupiter.api.Assertions.assertThrows;

/** Core 健康 contributor 注册表回归。 */
class CoreHealthRegistryTest {

    @Test
    void registryKeepsContributorMetadataAndStableNameOrder() {
        var registry = new CoreHealthRegistry(List.of(
                contributor("redis", "framework", "REDIS"),
                contributor("database", "framework", "DATABASE")));

        assertEquals(List.of("database", "redis"), registry.contributors()
                .stream()
                .map(DependencyHealthContributor::contributorName)
                .toList());
        assertEquals("framework", registry.metadata("redis").moduleName());
        assertEquals("REDIS", registry.metadata("redis").dependencyType());
    }

    @Test
    void registryRejectsDuplicateContributorNames() {
        var duplicate = assertThrows(IllegalStateException.class, () -> new CoreHealthRegistry(List.of(
                contributor("database", "framework", "DATABASE"),
                contributor("database", "core", "MODULE"))));

        org.junit.jupiter.api.Assertions.assertTrue(duplicate.getMessage().contains("database"));
    }

    private static DependencyHealthContributor contributor(String name, String module, String type) {
        return new DependencyHealthContributor() {
            @Override
            public String contributorName() {
                return name;
            }

            @Override
            public String moduleName() {
                return module;
            }

            @Override
            public String dependencyType() {
                return type;
            }

            @Override
            public Duration timeout() {
                return Duration.ofSeconds(1);
            }

            @Override
            public DependencyHealthResult check() {
                return new DependencyHealthResult(name, module, type, DependencyHealthStatus.UP,
                        Duration.ZERO, Instant.now(), null, "ok");
            }
        };
    }
}
