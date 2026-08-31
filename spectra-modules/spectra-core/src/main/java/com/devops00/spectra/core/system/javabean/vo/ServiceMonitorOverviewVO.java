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

package com.devops00.spectra.core.system.javabean.vo;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务监控总览。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/23 00:00
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ServiceMonitorOverviewVO {

    /** 采集时间。 */
    private LocalDateTime collectedAt;

    /** 服务总体状态：HEALTHY、WARNING、DEGRADED 或 DOWN。 */
    private String status;

    /** 状态说明。 */
    private String statusMessage;

    /** 监控数据新鲜度：CURRENT、DELAYED、STALE 或 UNAVAILABLE。 */
    private String dataFreshness;

    /** 当前快照距离响应时间的秒数。 */
    private long dataAgeSeconds;

    /** 应用名称。 */
    private String serviceName;

    /** 主机名称。 */
    private String hostName;

    /** 操作系统名称。 */
    private String osName;

    /** 服务运行时长，单位为秒。 */
    private long uptimeSeconds;

    /** 当前指标摘要。 */
    private Summary summary;

    /** 最近一段时间的指标趋势。 */
    @Builder.Default
    private List<Point> history = List.of();

    /** 关键依赖状态。 */
    @Builder.Default
    private List<Dependency> dependencies = List.of();

    /** Actuator 健康组件状态。 */
    @Builder.Default
    private List<HealthComponent> healthComponents = List.of();

    /** 健康组件聚合检查耗时，单位为毫秒。 */
    private long healthCheckLatencyMs;

    /**
     * 当前指标摘要。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {

        /** CPU 使用率，单位为百分比。 */
        private double cpuUsage;

        /** CPU 逻辑核心数。 */
        private int cpuLogicalCores;

        /** 系统内存使用率，单位为百分比。 */
        private double systemMemoryUsage;

        /** 系统内存总量，单位为字节。 */
        private long systemMemoryTotalBytes;

        /** 系统内存已使用量，单位为字节。 */
        private long systemMemoryUsedBytes;

        /** 系统内存可用量，单位为字节。 */
        private long systemMemoryAvailableBytes;

        /** JVM 堆内存使用率，单位为百分比。 */
        private double jvmHeapUsage;

        /** JVM 堆内存已使用量，单位为字节。 */
        private long jvmHeapUsedBytes;

        /** JVM 堆内存最大量，单位为字节。 */
        private long jvmHeapMaxBytes;

        /** JVM 非堆内存已使用量，单位为字节。 */
        private long jvmNonHeapUsedBytes;

        /** 当前活动线程数。 */
        private int liveThreadCount;

        /** 峰值线程数。 */
        private int peakThreadCount;

        /** 已发生的 GC 次数。 */
        private long gcCount;

        /** 最近采样周期的每秒请求数。 */
        private double qps;

        /** 最近采样周期的错误率，单位为百分比。 */
        private double errorRate;

        /** 请求响应时间 P95，单位为毫秒。 */
        private double p95ResponseMs;

        /** 是否采集到 HTTP 请求指标。 */
        private boolean requestMetricsAvailable;
    }

    /**
     * 历史趋势点。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Point {

        /** 采集时间。 */
        private LocalDateTime collectedAt;

        /** CPU 使用率，单位为百分比。 */
        private double cpuUsage;

        /** 系统内存使用率，单位为百分比。 */
        private double systemMemoryUsage;

        /** JVM 堆内存使用率，单位为百分比。 */
        private double jvmHeapUsage;

        /** 当前活动线程数。 */
        private int liveThreadCount;

        /** 已发生的 GC 次数。 */
        private long gcCount;

        /** 最近采样周期的每秒请求数。 */
        private double qps;

        /** 最近采样周期的错误率，单位为百分比。 */
        private double errorRate;

        /** 请求响应时间 P95，单位为毫秒。 */
        private double p95ResponseMs;
    }

    /**
     * 关键依赖状态。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Dependency {

        /** 依赖名称。 */
        private String name;

        /** 依赖状态：UP、DEGRADED、DOWN 或 UNKNOWN。 */
        private String status;

        /** 检查耗时，单位为毫秒。 */
        private long latencyMs;

        /** 脱敏后的状态说明。 */
        private String message;
    }

    /**
     * 应用健康组件状态。
     */
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class HealthComponent {

        /** 组件名称。 */
        private String name;

        /** 组件状态：UP、DEGRADED、DOWN 或 UNKNOWN。 */
        private String status;

        /** 脱敏后的状态说明。 */
        private String message;

        /** 最近一次检查时间。 */
        private LocalDateTime checkedAt;
    }
}
