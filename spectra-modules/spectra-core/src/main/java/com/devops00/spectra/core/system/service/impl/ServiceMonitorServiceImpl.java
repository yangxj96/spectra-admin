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

package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorOverviewVO;
import com.devops00.spectra.core.system.service.ServiceMonitorService;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.sun.management.OperatingSystemMXBean;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadMXBean;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 服务监控总览。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/23 00:00
 */
@Slf4j
@Service
public class ServiceMonitorServiceImpl implements ServiceMonitorService {

    private static final int HISTORY_LIMIT = 180;
    private static final double WARNING_CPU_USAGE = 80D;
    private static final double WARNING_MEMORY_USAGE = 80D;
    private static final double WARNING_JVM_HEAP_USAGE = 75D;
    private static final double WARNING_ERROR_RATE = 1D;
    private static final double WARNING_P95_RESPONSE_MS = 500D;

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final TimeMapper timeMapper;
    private final Environment environment;
    private final OperatingSystemMXBean operatingSystem = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private final Deque<Sample> history = new ArrayDeque<>(HISTORY_LIMIT);
    private final Object sampleLock = new Object();

    private volatile Sample latestSample;
    private long previousRequestCount = -1L;
    private long previousErrorCount = -1L;
    private long previousRequestNanos = -1L;

    public ServiceMonitorServiceImpl(MeterRegistry meterRegistry, DataSource dataSource,
                                     RedisConnectionFactory redisConnectionFactory, TimeMapper timeMapper, Environment environment) {
        this.meterRegistry = meterRegistry;
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
        this.timeMapper = timeMapper;
        this.environment = environment;
    }

    /**
     * 按固定间隔采集一次监控快照。历史数据只保留在当前实例内存中，避免第一期引入额外的监控存储和数据清理任务。
     */
    @Scheduled(fixedDelayString = "${spectra.monitor.collection-interval-ms:10000}", initialDelay = 1000)
    public void collectSnapshot() {
        try {
            var sample = collectSample();
            synchronized (sampleLock) {
                latestSample = sample;
                history.addLast(sample);
                while (history.size() > HISTORY_LIMIT) {
                    history.removeFirst();
                }
            }
        } catch (RuntimeException exception) {
            log.warn("服务监控快照采集失败，保留上一份快照");
        }
    }

    @Override
    public ServiceMonitorOverviewVO getOverview() {
        if (latestSample == null) {
            collectSnapshot();
        }

        synchronized (sampleLock) {
            if (latestSample == null) {
                return emptyOverview();
            }
            return toOverview(latestSample, List.copyOf(history));
        }
    }

    private Sample collectSample() {
        var now = Instant.now();
        var cpuLoad = operatingSystem.getCpuLoad();
        var cpuUsage = cpuLoad < 0D ? 0D : percentage(cpuLoad);
        var logicalCores = operatingSystem.getAvailableProcessors();
        var totalMemory = operatingSystem.getTotalMemorySize();
        var availableMemory = operatingSystem.getFreeMemorySize();
        var usedMemory = Math.max(totalMemory - availableMemory, 0L);
        var systemMemoryUsage = ratioAsPercentage(usedMemory, totalMemory);

        var memoryBean = ManagementFactory.getMemoryMXBean();
        var heapUsage = normalizeUsage(memoryBean.getHeapMemoryUsage());
        var nonHeapUsage = memoryBean.getNonHeapMemoryUsage();
        var threadBean = ManagementFactory.getThreadMXBean();
        var requestMetrics = collectRequestMetrics();
        var dependencies = List.of(checkDatabase(), checkRedis());
        var status = resolveStatus(cpuUsage, systemMemoryUsage, heapUsage.usage(), requestMetrics, dependencies);
        var runtime = ManagementFactory.getRuntimeMXBean();

        return new Sample(now, cpuUsage, logicalCores, systemMemoryUsage, totalMemory, usedMemory, availableMemory,
                heapUsage.used(), heapUsage.max(), heapUsage.usage(), nonHeapUsage.getUsed(), threadBean.getThreadCount(),
                threadBean.getPeakThreadCount(), collectGcCount(), requestMetrics.qps(), requestMetrics.errorRate(),
                requestMetrics.p95ResponseMs(), requestMetrics.available(), dependencies, status,
                runtime.getUptime() / 1000L);
    }

    private RequestMetrics collectRequestMetrics() {
        Collection<Timer> timers = meterRegistry.find("http.server.requests").timers();
        if (timers.isEmpty()) {
            return RequestMetrics.unavailable();
        }

        long requestCount = 0L;
        long errorCount = 0L;
        double p95ResponseMs = 0D;
        for (Timer timer : timers) {
            requestCount += timer.count();
            var status = timer.getId().getTag("status");
            if (status != null && status.startsWith("5")) {
                errorCount += timer.count();
            }
            p95ResponseMs = Math.max(p95ResponseMs, timer.percentile(0.95D, TimeUnit.MILLISECONDS));
        }

        var now = System.nanoTime();
        var qps = 0D;
        var errorRate = 0D;
        if (previousRequestCount >= 0L && previousRequestNanos > 0L) {
            var elapsedSeconds = (now - previousRequestNanos) / 1_000_000_000D;
            if (elapsedSeconds > 0D) {
                qps = Math.max(requestCount - previousRequestCount, 0L) / elapsedSeconds;
            }
            var requestDelta = Math.max(requestCount - previousRequestCount, 0L);
            var errorDelta = Math.max(errorCount - previousErrorCount, 0L);
            errorRate = requestDelta == 0L ? 0D : percentage((double) errorDelta / requestDelta);
        }
        previousRequestCount = requestCount;
        previousErrorCount = errorCount;
        previousRequestNanos = now;
        return new RequestMetrics(true, qps, errorRate, p95ResponseMs);
    }

    private ServiceMonitorOverviewVO.Dependency checkDatabase() {
        var start = System.nanoTime();
        try (Connection connection = dataSource.getConnection()) {
            var available = connection.isValid(2);
            return dependency("PostgreSQL", available, start, available ? "连接正常" : "数据库连接不可用");
        } catch (SQLException | RuntimeException exception) {
            return dependency("PostgreSQL", false, start, "数据库连接不可用");
        }
    }

    private ServiceMonitorOverviewVO.Dependency checkRedis() {
        var start = System.nanoTime();
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            var available = "PONG".equalsIgnoreCase(connection.ping());
            return dependency("Redis", available, start, available ? "连接正常" : "Redis 连接不可用");
        } catch (RuntimeException exception) {
            return dependency("Redis", false, start, "Redis 连接不可用");
        }
    }

    private static ServiceMonitorOverviewVO.Dependency dependency(String name, boolean available, long start,
                                                                  String message) {
        return ServiceMonitorOverviewVO.Dependency.builder()
                .name(name)
                .status(available ? "UP" : "DOWN")
                .latencyMs(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
                .message(message)
                .build();
    }

    private static String resolveStatus(double cpuUsage, double systemMemoryUsage, double jvmHeapUsage,
                                        RequestMetrics requestMetrics, List<ServiceMonitorOverviewVO.Dependency> dependencies) {
        var downCount = dependencies.stream().filter(item -> "DOWN".equals(item.getStatus())).count();
        if (downCount == dependencies.size()) {
            return "DOWN";
        }
        if (downCount > 0L) {
            return "DEGRADED";
        }
        if (cpuUsage >= WARNING_CPU_USAGE
                || systemMemoryUsage >= WARNING_MEMORY_USAGE
                || jvmHeapUsage >= WARNING_JVM_HEAP_USAGE
                || requestMetrics.errorRate() >= WARNING_ERROR_RATE
                || requestMetrics.p95ResponseMs() >= WARNING_P95_RESPONSE_MS) {
            return "WARNING";
        }
        return "HEALTHY";
    }

    private ServiceMonitorOverviewVO toOverview(Sample sample, List<Sample> samples) {
        var historyPoints = new ArrayList<ServiceMonitorOverviewVO.Point>(samples.size());
        for (var item : samples) {
            historyPoints.add(toPoint(item));
        }

        var summary = ServiceMonitorOverviewVO.Summary.builder()
                .cpuUsage(sample.cpuUsage())
                .cpuLogicalCores(sample.cpuLogicalCores())
                .systemMemoryUsage(sample.systemMemoryUsage())
                .systemMemoryTotalBytes(sample.totalMemory())
                .systemMemoryUsedBytes(sample.usedMemory())
                .systemMemoryAvailableBytes(sample.availableMemory())
                .jvmHeapUsage(sample.jvmHeapUsage())
                .jvmHeapUsedBytes(sample.jvmHeapUsed())
                .jvmHeapMaxBytes(sample.jvmHeapMax())
                .jvmNonHeapUsedBytes(sample.jvmNonHeapUsed())
                .liveThreadCount(sample.liveThreadCount())
                .peakThreadCount(sample.peakThreadCount())
                .gcCount(sample.gcCount())
                .qps(sample.qps())
                .errorRate(sample.errorRate())
                .p95ResponseMs(sample.p95ResponseMs())
                .requestMetricsAvailable(sample.requestMetricsAvailable())
                .build();
        return ServiceMonitorOverviewVO.builder()
                .collectedAt(toLocalDateTime(sample.collectedAt()))
                .status(sample.status())
                .statusMessage(statusMessage(sample.status()))
                .serviceName(environment.getProperty("spring.application.name", "spectra-admin"))
                .hostName(getHostName())
                .osName(System.getProperty("os.name", "Unknown"))
                .uptimeSeconds(sample.uptimeSeconds())
                .summary(summary)
                .history(historyPoints)
                .dependencies(sample.dependencies())
                .build();
    }

    private ServiceMonitorOverviewVO.Point toPoint(Sample sample) {
        return ServiceMonitorOverviewVO.Point.builder()
                .collectedAt(toLocalDateTime(sample.collectedAt()))
                .cpuUsage(sample.cpuUsage())
                .systemMemoryUsage(sample.systemMemoryUsage())
                .jvmHeapUsage(sample.jvmHeapUsage())
                .liveThreadCount(sample.liveThreadCount())
                .gcCount(sample.gcCount())
                .qps(sample.qps())
                .errorRate(sample.errorRate())
                .p95ResponseMs(sample.p95ResponseMs())
                .build();
    }

    private ServiceMonitorOverviewVO emptyOverview() {
        var summary = ServiceMonitorOverviewVO.Summary.builder().build();
        return ServiceMonitorOverviewVO.builder()
                .collectedAt(toLocalDateTime(Instant.now()))
                .status("DOWN")
                .statusMessage("监控数据暂不可用")
                .serviceName(environment.getProperty("spring.application.name", "spectra-admin"))
                .hostName(getHostName())
                .osName(System.getProperty("os.name", "Unknown"))
                .summary(summary)
                .build();
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        var value = timeMapper.toLocalDateTime(instant);
        return value == null ? LocalDateTime.ofInstant(instant, ZoneOffset.UTC) : value;
    }

    private static String statusMessage(String status) {
        return switch (status) {
            case "HEALTHY" -> "服务运行正常";
            case "WARNING" -> "服务存在需要关注的指标";
            case "DEGRADED" -> "部分关键依赖不可用";
            case "DOWN" -> "关键依赖均不可用";
            default -> "监控数据暂不可用";
        };
    }

    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            return "本机";
        }
    }

    private static long collectGcCount() {
        return ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .mapToLong(GarbageCollectorMXBean::getCollectionCount)
                .filter(value -> value >= 0L)
                .sum();
    }

    private static MemorySnapshot normalizeUsage(MemoryUsage usage) {
        var max = usage.getMax() > 0L ? usage.getMax() : usage.getCommitted();
        return new MemorySnapshot(usage.getUsed(), max, ratioAsPercentage(usage.getUsed(), max));
    }

    private static double ratioAsPercentage(long numerator, long denominator) {
        return denominator <= 0L ? 0D : percentage((double) numerator / denominator);
    }

    private static double percentage(double ratio) {
        return Math.max(0D, Math.min(ratio * 100D, 100D));
    }

    private record MemorySnapshot(long used, long max, double usage) {
    }

    private record RequestMetrics(boolean available, double qps, double errorRate, double p95ResponseMs) {

        private static RequestMetrics unavailable() {
            return new RequestMetrics(false, 0D, 0D, 0D);
        }
    }

    private record Sample(Instant collectedAt, double cpuUsage, int cpuLogicalCores, double systemMemoryUsage,
                          long totalMemory,
                          long usedMemory, long availableMemory, long jvmHeapUsed, long jvmHeapMax, double jvmHeapUsage,
                          long jvmNonHeapUsed, int liveThreadCount, int peakThreadCount, long gcCount, double qps, double errorRate,
                          double p95ResponseMs, boolean requestMetricsAvailable, List<ServiceMonitorOverviewVO.Dependency> dependencies,
                          String status, long uptimeSeconds) {
    }
}
