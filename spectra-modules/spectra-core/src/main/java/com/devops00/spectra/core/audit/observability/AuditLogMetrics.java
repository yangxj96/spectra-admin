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

package com.devops00.spectra.core.audit.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * 采集和维护审计日志相关的运行指标。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
public class AuditLogMetrics {

    private final MeterRegistry registry;

    public AuditLogMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    public void recordQuery(String operation, String outcome) {
        Counter.builder("audit_log_queries_total")
                .tags(Tags.of("operation", safeTag(operation), "outcome", safeTag(outcome)))
                .register(registry)
                .increment();
    }

    /**
     * 处理安全标签相关数据。
     */
    private static String safeTag(String value) {
        return value == null || value.isBlank() ? "UNKNOWN" : value.trim().toUpperCase(Locale.ROOT);
    }
}
