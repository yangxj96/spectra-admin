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

package com.devops00.spectra.core.system.javabean.enums;

import lombok.Getter;

/**
 * 可用于服务监控告警的指标。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Getter
public enum ServiceMonitorAlertMetric {

    CPU_USAGE("CPU_USAGE", "CPU 使用率", true),
    SYSTEM_MEMORY_USAGE("SYSTEM_MEMORY_USAGE", "系统内存使用率", true),
    JVM_HEAP_USAGE("JVM_HEAP_USAGE", "JVM 堆内存使用率", true),
    ERROR_RATE("ERROR_RATE", "HTTP 5xx 错误率", true),
    P95_RESPONSE_MS("P95_RESPONSE_MS", "请求响应 P95", true),
    DATABASE_STATUS("DATABASE_STATUS", "PostgreSQL 状态", false),
    REDIS_STATUS("REDIS_STATUS", "Redis 状态", false);

    private final String code;
    private final String label;
    private final boolean numeric;

    ServiceMonitorAlertMetric(String code, String label, boolean numeric) {
        this.code = code;
        this.label = label;
        this.numeric = numeric;
    }

    /**
     * 转换、解析或规范化数据（{@code fromCode}）。
     */
    public static ServiceMonitorAlertMetric fromCode(String code) {
        for (var metric : values()) {
            if (metric.code.equalsIgnoreCase(code)) {
                return metric;
            }
        }
        return null;
    }
}
