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

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 服务监控只读运行时诊断信息。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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

    /**
     * 封装相关数据相关的响应数据。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     *
     */
    @Data
    @Builder
    public static class MemoryPool {
        private String name;
        private long usedBytes;
        private long committedBytes;
        private long maxBytes;
        private double usage;
    }

    /**
     * 封装相关数据相关的响应数据。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     *
     */
    @Data
    @Builder
    public static class GarbageCollector {
        private String name;
        private long collectionCount;
        private long collectionTimeMs;
    }

    /**
     * 封装状态统计相关的响应数据。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    @Data
    @Builder
    public static class ThreadStateCount {
        private String state;
        private long count;
    }

    /**
     * 封装相关数据相关的响应数据。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     *
     */
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

    /**
     * 封装Redis诊断相关的响应数据。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
    @Data
    @Builder
    public static class RedisDiagnostic {
        private String status;
        private long latencyMs;
    }

    /**
     * 封装端点相关的响应数据。
     *
     * @author yangxj96
     * @version 1.0
     * @since 2026/09/13
     */
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
