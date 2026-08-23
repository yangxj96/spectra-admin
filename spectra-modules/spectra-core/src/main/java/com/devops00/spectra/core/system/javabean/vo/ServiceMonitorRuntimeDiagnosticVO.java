/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.javabean.vo;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/** 服务监控只读运行时诊断信息。 */
@Data
@Builder
public class ServiceMonitorRuntimeDiagnosticVO {

    private LocalDateTime generatedAt;
    @Builder.Default
    private List<MemoryPool> memoryPools = List.of();
    @Builder.Default
    private List<GarbageCollector> garbageCollectors = List.of();
    @Builder.Default
    private List<ThreadStateCount> threadStates = List.of();
    private ConnectionPool connectionPool;
    private RedisDiagnostic redis;
    @Builder.Default
    private List<SlowEndpoint> slowEndpoints = List.of();

    @Data
    @Builder
    public static class MemoryPool {
        private String name;
        private long usedBytes;
        private long committedBytes;
        private long maxBytes;
        private double usage;
    }

    @Data
    @Builder
    public static class GarbageCollector {
        private String name;
        private long collectionCount;
        private long collectionTimeMs;
    }

    @Data
    @Builder
    public static class ThreadStateCount {
        private String state;
        private long count;
    }

    @Data
    @Builder
    public static class ConnectionPool {
        private String name;
        private String status;
        private Integer active;
        private Integer idle;
        private Integer total;
        private Integer maximum;
    }

    @Data
    @Builder
    public static class RedisDiagnostic {
        private String status;
        private long latencyMs;
    }

    @Data
    @Builder
    public static class SlowEndpoint {
        private String method;
        private String uri;
        private String status;
        private long count;
        private double p95ResponseMs;
    }
}
