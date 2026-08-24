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

package com.devops00.spectra.core.security.audit.observability;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import org.springframework.stereotype.Component;

import java.util.Locale;

/**
 * Security Audit 查询/导出指标门面。标签只使用固定低基数枚举，不记录用户、事件 ID 或 IP。
 */
@Component
public class SecurityAuditMetrics {

    private final MeterRegistry registry;

    public SecurityAuditMetrics(MeterRegistry registry) {
        this.registry = registry;
    }

    /**
     * 更新或推进目标状态（{@code recordQuery}）。
     */
    public void recordQuery(String operation, String outcome) {
        Counter.builder("security_audit_queries_total")
                .tags(Tags.of("operation", safeTag(operation), "outcome", safeTag(outcome)))
                .register(registry)
                .increment();
    }

    /**
     * 处理内部业务逻辑（{@code safeTag}）。
     */
    private static String safeTag(String value) {
        if (value == null || value.isBlank()) {
            return "UNKNOWN";
        }
        return value.trim().toUpperCase(Locale.ROOT);
    }
}
