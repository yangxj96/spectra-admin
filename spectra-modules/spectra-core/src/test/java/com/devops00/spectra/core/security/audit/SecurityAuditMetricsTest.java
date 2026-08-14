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

package com.devops00.spectra.core.security.audit;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class SecurityAuditMetricsTest {

    @Test
    void recordsOnlyLowCardinalityQueryMetrics() {
        var registry = new SimpleMeterRegistry();
        var metrics = new SecurityAuditMetrics(registry);

        metrics.recordQuery("export", "succeeded");

        assertEquals(1.0, registry.get("security_audit_queries_total")
                .tag("operation", "EXPORT")
                .tag("outcome", "SUCCEEDED")
                .counter()
                .count());
    }
}
