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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorSample;
import com.devops00.spectra.core.system.javabean.enums.ServiceMonitorDependencyStatus;
import com.devops00.spectra.core.system.javabean.enums.ServiceMonitorFreshness;
import com.devops00.spectra.core.system.javabean.enums.ServiceMonitorHistoryRange;
import com.devops00.spectra.core.system.javabean.enums.ServiceMonitorHealthStatus;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorHistoryFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorOverviewVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorHistoryVO;
import com.devops00.spectra.core.system.mapper.ServiceMonitorSampleMapper;
import com.devops00.spectra.core.system.service.ServiceMonitorAlertService;
import com.devops00.spectra.core.system.service.ServiceMonitorService;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.sun.management.OperatingSystemMXBean;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.health.actuate.endpoint.CompositeHealthDescriptor;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import javax.sql.DataSource;

import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryUsage;
import java.net.InetAddress;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Instant;
import java.time.Duration;
import java.time.LocalDateTime;
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
    private static final long HISTORY_CLEANUP_INTERVAL_SECONDS = 3600L;
    private static final int DEFAULT_HISTORY_RETENTION_DAYS = 7;

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final TimeMapper timeMapper;
    private final Environment environment;
    private final ObjectProvider<HealthEndpoint> healthEndpointProvider;
    private final ServiceMonitorSampleMapper sampleMapper;
    private final ServiceMonitorAlertService alertService;
    private final long collectionIntervalSeconds;
    private final int historyRetentionDays;
    private final OperatingSystemMXBean operatingSystem = ManagementFactory.getPlatformMXBean(OperatingSystemMXBean.class);
    private final Deque<Sample> history = new ArrayDeque<>(HISTORY_LIMIT);
    private final Object sampleLock = new Object();

    private volatile Sample latestSample;
    private long previousRequestCount = -1L;
    private long previousErrorCount = -1L;
    private long previousRequestNanos = -1L;
    private volatile Instant lastHistoryCleanupAt;

    public ServiceMonitorServiceImpl(MeterRegistry meterRegistry, DataSource dataSource,
                                     RedisConnectionFactory redisConnectionFactory, TimeMapper timeMapper, Environment environment,
                                     ObjectProvider<HealthEndpoint> healthEndpointProvider, ServiceMonitorSampleMapper sampleMapper,
                                     ServiceMonitorAlertService alertService) {
        this.meterRegistry = meterRegistry;
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
        this.timeMapper = timeMapper;
        this.environment = environment;
        this.healthEndpointProvider = healthEndpointProvider;
        this.sampleMapper = sampleMapper;
        this.alertService = alertService;
        var intervalMillis = environment.getProperty("spectra.monitor.collection-interval-ms", Long.class, 10000L);
        this.collectionIntervalSeconds = Math.max(intervalMillis / 1000L, 1L);
        var retentionDays = environment.getProperty("spectra.monitor.history-retention-days", Integer.class,
                DEFAULT_HISTORY_RETENTION_DAYS);
        this.historyRetentionDays = Math.max(retentionDays, 1);
    }

    /**
     * 按固定间隔采集一次监控快照，并保存单体应用本地历史趋势。
     */
    public void collectSnapshot() {
        try {
            collectSnapshotInternal();
        } catch (RuntimeException exception) {
            log.warn("服务监控快照采集失败，保留上一份快照");
        }
    }

    /** 统一调度 LOOP 使用的采集入口；失败向调度器暴露以进入错误聚合。 */
    public void collectSnapshotForScheduler() {
        collectSnapshotInternal();
    }

    private void collectSnapshotInternal() {
        try {
            var sample = collectSample();
            var persisted = persistSample(sample);
            try {
                alertService.evaluate(persisted);
            } catch (RuntimeException exception) {
                log.warn("服务监控告警评估失败，保留当前监控快照", exception);
            }
            synchronized (sampleLock) {
                latestSample = sample;
                history.addLast(sample);
                while (history.size() > HISTORY_LIMIT) {
                    history.removeFirst();
                }
            }
        } catch (RuntimeException exception) {
            throw exception;
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

    @Override
    public ServiceMonitorHistoryVO getHistory(ServiceMonitorHistoryFrom from) {
        var range = ServiceMonitorHistoryRange.fromCode(from == null ? null : from.getRange());
        var to = Instant.now();
        var start = to.minus(range.getDuration());
        List<ServiceMonitorSample> samples;
        try {
            var query = new LambdaQueryWrapper<ServiceMonitorSample>()
                    .ge(ServiceMonitorSample::getCollectedAt, start)
                    .le(ServiceMonitorSample::getCollectedAt, to)
                    .orderByAsc(ServiceMonitorSample::getCollectedAt);
            samples = sampleMapper.selectList(query);
        } catch (RuntimeException exception) {
            throw new DataException("服务监控历史查询失败", exception);
        }
        var points = downsample(samples, range.getMaxPoints()).stream().map(this::toPoint).toList();
        return ServiceMonitorHistoryVO.builder()
                .range(range.getCode())
                .from(toLocalDateTime(start))
                .to(toLocalDateTime(to))
                .points(points)
                .build();
    }

    /**
     * 处理内部业务逻辑（{@code collectSample}）。
     */
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
        var health = collectHealth();
        var status = resolveStatus(cpuUsage, systemMemoryUsage, heapUsage.usage(), requestMetrics, dependencies,
                health.status());
        var runtime = ManagementFactory.getRuntimeMXBean();

        var databaseStatus = dependencies.stream()
                .filter(item -> "PostgreSQL".equals(item.getName()))
                .map(ServiceMonitorOverviewVO.Dependency::getStatus)
                .findFirst()
                .orElse(ServiceMonitorDependencyStatus.UNKNOWN.name());
        var redisStatus = dependencies.stream()
                .filter(item -> "Redis".equals(item.getName()))
                .map(ServiceMonitorOverviewVO.Dependency::getStatus)
                .findFirst()
                .orElse(ServiceMonitorDependencyStatus.UNKNOWN.name());
        return new Sample(now, cpuUsage, logicalCores, systemMemoryUsage, totalMemory, usedMemory, availableMemory,
                heapUsage.used(), heapUsage.max(), heapUsage.usage(), nonHeapUsage.getUsed(), threadBean.getThreadCount(),
                threadBean.getPeakThreadCount(), collectGcCount(), requestMetrics.qps(), requestMetrics.errorRate(),
                requestMetrics.p95ResponseMs(), requestMetrics.available(), dependencies, health.components(),
                health.status(), health.latencyMs(), status,
                databaseStatus, redisStatus,
                runtime.getUptime() / 1000L);
    }

    /**
     * 处理内部业务逻辑（{@code collectHealth}）。
     */
    private HealthSnapshot collectHealth() {
        var start = System.nanoTime();
        try {
            var descriptor = healthEndpointProvider.getObject().health();
            var components = new ArrayList<ServiceMonitorOverviewVO.HealthComponent>();
            var status = descriptor.getStatus();
            if (status == null) {
                return new HealthSnapshot("UNKNOWN", List.of(healthComponent("application", "UNKNOWN")), elapsedMillis(start));
            }
            if (descriptor instanceof CompositeHealthDescriptor composite) {
                var compositeComponents = composite.getComponents();
                if (compositeComponents != null && !compositeComponents.isEmpty()) {
                    compositeComponents
                            .forEach((name, component) -> components.add(healthComponent(name,
                                    component.getStatus() == null ? "UNKNOWN" : component.getStatus().getCode())));
                } else {
                    components.add(healthComponent("application", status.getCode()));
                }
            } else {
                components.add(healthComponent("application", status.getCode()));
            }
            return new HealthSnapshot(status.getCode(), List.copyOf(components), elapsedMillis(start));
        } catch (RuntimeException exception) {
            return new HealthSnapshot("UNKNOWN", List.of(healthComponent("application", "UNKNOWN")),
                    elapsedMillis(start));
        }
    }

    /**
     * 处理内部业务逻辑（{@code healthComponent}）。
     */
    private static ServiceMonitorOverviewVO.HealthComponent healthComponent(String name, String status) {
        var normalizedStatus = status == null || status.isBlank() ? "UNKNOWN" : status;
        return ServiceMonitorOverviewVO.HealthComponent.builder()
                .name(name)
                .status(normalizedStatus)
                .message(healthMessage(normalizedStatus))
                .build();
    }

    /**
     * 处理内部业务逻辑（{@code healthMessage}）。
     */
    private static String healthMessage(String status) {
        return switch (status) {
            case "UP" -> "检查正常";
            case "DOWN" -> "检查未通过";
            case "OUT_OF_SERVICE" -> "服务不可用";
            default -> "检查状态未知";
        };
    }

    /**
     * 处理内部业务逻辑（{@code collectRequestMetrics}）。
     */
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

    /**
     * 校验并确保数据满足当前约束（{@code checkDatabase}）。
     */
    private ServiceMonitorOverviewVO.Dependency checkDatabase() {
        var start = System.nanoTime();
        try (Connection connection = dataSource.getConnection()) {
            var available = connection.isValid(2);
            return dependency("PostgreSQL", available, start, available ? "连接正常" : "数据库连接不可用");
        } catch (SQLException | RuntimeException exception) {
            return dependency("PostgreSQL", false, start, "数据库连接不可用");
        }
    }

    /**
     * 校验并确保数据满足当前约束（{@code checkRedis}）。
     */
    private ServiceMonitorOverviewVO.Dependency checkRedis() {
        var start = System.nanoTime();
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            var available = "PONG".equalsIgnoreCase(connection.ping());
            return dependency("Redis", available, start, available ? "连接正常" : "Redis 连接不可用");
        } catch (RuntimeException exception) {
            return dependency("Redis", false, start, "Redis 连接不可用");
        }
    }

    /**
     * 处理内部业务逻辑（{@code dependency}）。
     */
    private static ServiceMonitorOverviewVO.Dependency dependency(String name, boolean available, long start,
                                                                  String message) {
        return ServiceMonitorOverviewVO.Dependency.builder()
                .name(name)
                .status((available ? ServiceMonitorDependencyStatus.UP : ServiceMonitorDependencyStatus.DOWN).name())
                .latencyMs(TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start))
                .message(message)
                .build();
    }

    /**
     * 转换、解析或规范化数据（{@code resolveStatus}）。
     */
    private static ServiceMonitorHealthStatus resolveStatus(double cpuUsage, double systemMemoryUsage, double jvmHeapUsage,
                                                            RequestMetrics requestMetrics,
                                                            List<ServiceMonitorOverviewVO.Dependency> dependencies,
                                                            String healthStatus) {
        var downCount = dependencies.stream()
                .filter(item -> ServiceMonitorDependencyStatus.DOWN.name().equals(item.getStatus()))
                .count();
        if (downCount == dependencies.size()) {
            return ServiceMonitorHealthStatus.DOWN;
        }
        if (downCount > 0L) {
            return ServiceMonitorHealthStatus.DEGRADED;
        }
        if ("DOWN".equals(healthStatus) || "OUT_OF_SERVICE".equals(healthStatus)) {
            return ServiceMonitorHealthStatus.DEGRADED;
        }
        if ("UNKNOWN".equals(healthStatus)) {
            return ServiceMonitorHealthStatus.WARNING;
        }
        if (cpuUsage >= WARNING_CPU_USAGE
                || systemMemoryUsage >= WARNING_MEMORY_USAGE
                || jvmHeapUsage >= WARNING_JVM_HEAP_USAGE
                || requestMetrics.errorRate() >= WARNING_ERROR_RATE
                || requestMetrics.p95ResponseMs() >= WARNING_P95_RESPONSE_MS) {
            return ServiceMonitorHealthStatus.WARNING;
        }
        return ServiceMonitorHealthStatus.HEALTHY;
    }

    /**
     * 转换、解析或规范化数据（{@code toOverview}）。
     */
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
        var freshness = freshness(sample.collectedAt());
        var status = effectiveStatus(sample.status(), freshness.status());
        return ServiceMonitorOverviewVO.builder()
                .collectedAt(toLocalDateTime(sample.collectedAt()))
                .status(status.name())
                .statusMessage(statusMessage(status, freshness.status()))
                .dataFreshness(freshness.status().name())
                .dataAgeSeconds(freshness.ageSeconds())
                .serviceName(environment.getProperty("spring.application.name", "spectra-admin"))
                .hostName(getHostName())
                .osName(System.getProperty("os.name", "Unknown"))
                .uptimeSeconds(sample.uptimeSeconds())
                .summary(summary)
                .history(historyPoints)
                .dependencies(sample.dependencies())
                .healthComponents(toHealthComponents(sample))
                .healthCheckLatencyMs(sample.healthCheckLatencyMs())
                .build();
    }

    /**
     * 转换、解析或规范化数据（{@code toPoint}）。
     */
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

    /**
     * 转换、解析或规范化数据（{@code toPoint}）。
     */
    private ServiceMonitorOverviewVO.Point toPoint(ServiceMonitorSample sample) {
        return ServiceMonitorOverviewVO.Point.builder()
                .collectedAt(toLocalDateTime(sample.getCollectedAt()))
                .cpuUsage(sample.getCpuUsage())
                .systemMemoryUsage(sample.getSystemMemoryUsage())
                .jvmHeapUsage(sample.getJvmHeapUsage())
                .liveThreadCount(sample.getLiveThreadCount())
                .gcCount(sample.getGcCount())
                .qps(sample.getQps())
                .errorRate(sample.getErrorRate())
                .p95ResponseMs(sample.getP95ResponseMs())
                .build();
    }

    /**
     * 转换、解析或规范化数据（{@code toHealthComponents}）。
     */
    private List<ServiceMonitorOverviewVO.HealthComponent> toHealthComponents(Sample sample) {
        var checkedAt = toLocalDateTime(sample.collectedAt());
        return sample.healthComponents()
                .stream()
                .map(item -> ServiceMonitorOverviewVO.HealthComponent.builder()
                        .name(item.getName())
                        .status(item.getStatus())
                        .message(item.getMessage())
                        .checkedAt(checkedAt)
                        .build())
                .toList();
    }

    /**
     * 处理内部业务逻辑（{@code emptyOverview}）。
     */
    private ServiceMonitorOverviewVO emptyOverview() {
        var summary = ServiceMonitorOverviewVO.Summary.builder().build();
        return ServiceMonitorOverviewVO.builder()
                .collectedAt(toLocalDateTime(Instant.now()))
                .status(ServiceMonitorHealthStatus.DOWN.name())
                .statusMessage("监控数据暂不可用")
                .dataFreshness(ServiceMonitorFreshness.UNAVAILABLE.name())
                .dataAgeSeconds(0L)
                .serviceName(environment.getProperty("spring.application.name", "spectra-admin"))
                .hostName(getHostName())
                .osName(System.getProperty("os.name", "Unknown"))
                .summary(summary)
                .build();
    }

    /**
     * 处理内部业务逻辑（{@code persistSample}）。
     */
    private ServiceMonitorSample persistSample(Sample sample) {
        var entity = toEntity(sample);
        try {
            sampleMapper.insert(entity);
        } catch (RuntimeException exception) {
            log.warn("服务监控历史保存失败，保留当前监控快照", exception);
        }
        var now = sample.collectedAt();
        if (lastHistoryCleanupAt != null
                && Duration.between(lastHistoryCleanupAt, now).getSeconds() < HISTORY_CLEANUP_INTERVAL_SECONDS) {
            return entity;
        }
        try {
            var cutoff = now.minus(Duration.ofDays(historyRetentionDays));
            sampleMapper.delete(new LambdaQueryWrapper<ServiceMonitorSample>()
                    .lt(ServiceMonitorSample::getCollectedAt, cutoff));
            lastHistoryCleanupAt = now;
        } catch (RuntimeException exception) {
            log.warn("服务监控历史清理失败", exception);
        }
        return entity;
    }

    /**
     * 转换、解析或规范化数据（{@code toEntity}）。
     */
    private static ServiceMonitorSample toEntity(Sample sample) {
        var entity = new ServiceMonitorSample();
        entity.setCollectedAt(sample.collectedAt());
        entity.setCpuUsage(sample.cpuUsage());
        entity.setCpuLogicalCores(sample.cpuLogicalCores());
        entity.setSystemMemoryUsage(sample.systemMemoryUsage());
        entity.setSystemMemoryTotalBytes(sample.totalMemory());
        entity.setSystemMemoryUsedBytes(sample.usedMemory());
        entity.setSystemMemoryAvailableBytes(sample.availableMemory());
        entity.setJvmHeapUsage(sample.jvmHeapUsage());
        entity.setJvmHeapUsedBytes(sample.jvmHeapUsed());
        entity.setJvmHeapMaxBytes(sample.jvmHeapMax());
        entity.setJvmNonHeapUsedBytes(sample.jvmNonHeapUsed());
        entity.setLiveThreadCount(sample.liveThreadCount());
        entity.setPeakThreadCount(sample.peakThreadCount());
        entity.setGcCount(sample.gcCount());
        entity.setQps(sample.qps());
        entity.setErrorRate(sample.errorRate());
        entity.setP95ResponseMs(sample.p95ResponseMs());
        entity.setRequestMetricsAvailable(sample.requestMetricsAvailable());
        entity.setDatabaseStatus(sample.databaseStatus());
        entity.setRedisStatus(sample.redisStatus());
        entity.setStatus(sample.status().name());
        return entity;
    }

    private Freshness freshness(Instant collectedAt) {
        var ageSeconds = Math.max(Duration.between(collectedAt, Instant.now()).getSeconds(), 0L);
        if (ageSeconds <= collectionIntervalSeconds * 2L) {
            return new Freshness(ServiceMonitorFreshness.CURRENT, ageSeconds);
        }
        if (ageSeconds <= collectionIntervalSeconds * 6L) {
            return new Freshness(ServiceMonitorFreshness.DELAYED, ageSeconds);
        }
        return new Freshness(ServiceMonitorFreshness.STALE, ageSeconds);
    }

    /**
     * 处理内部业务逻辑（{@code effectiveStatus}）。
     */
    private static ServiceMonitorHealthStatus effectiveStatus(ServiceMonitorHealthStatus status,
                                                              ServiceMonitorFreshness freshness) {
        if (freshness == ServiceMonitorFreshness.STALE && status != ServiceMonitorHealthStatus.DOWN) {
            return ServiceMonitorHealthStatus.DEGRADED;
        }
        if (freshness == ServiceMonitorFreshness.DELAYED && status == ServiceMonitorHealthStatus.HEALTHY) {
            return ServiceMonitorHealthStatus.WARNING;
        }
        return status;
    }

    /**
     * 转换、解析或规范化数据（{@code toLocalDateTime}）。
     */
    private LocalDateTime toLocalDateTime(Instant instant) {
        return timeMapper.toLocalDateTime(instant);
    }

    /**
     * 查询或获取目标数据（{@code statusMessage}）。
     */
    private static String statusMessage(ServiceMonitorHealthStatus status, ServiceMonitorFreshness freshness) {
        if (freshness == ServiceMonitorFreshness.STALE) {
            return "监控数据已过期，当前状态仅供参考";
        }
        if (freshness == ServiceMonitorFreshness.DELAYED && status == ServiceMonitorHealthStatus.WARNING) {
            return "监控数据采集延迟";
        }
        return switch (status) {
            case HEALTHY -> "服务运行正常";
            case WARNING -> "服务存在需要关注的指标";
            case DEGRADED -> "部分关键依赖不可用";
            case DOWN -> "关键依赖均不可用";
        };
    }

    /**
     * 查询或获取目标数据（{@code getHostName}）。
     */
    private static String getHostName() {
        try {
            return InetAddress.getLocalHost().getHostName();
        } catch (Exception exception) {
            return "本机";
        }
    }

    /**
     * 处理内部业务逻辑（{@code collectGcCount}）。
     */
    private static long collectGcCount() {
        return ManagementFactory.getGarbageCollectorMXBeans()
                .stream()
                .mapToLong(GarbageCollectorMXBean::getCollectionCount)
                .filter(value -> value >= 0L)
                .sum();
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeUsage}）。
     */
    private static MemorySnapshot normalizeUsage(MemoryUsage usage) {
        var max = usage.getMax() > 0L ? usage.getMax() : usage.getCommitted();
        return new MemorySnapshot(usage.getUsed(), max, ratioAsPercentage(usage.getUsed(), max));
    }

    /**
     * 处理内部业务逻辑（{@code ratioAsPercentage}）。
     */
    private static double ratioAsPercentage(long numerator, long denominator) {
        return denominator <= 0L ? 0D : percentage((double) numerator / denominator);
    }

    /**
     * 处理内部业务逻辑（{@code percentage}）。
     */
    private static double percentage(double ratio) {
        return Math.max(0D, Math.min(ratio * 100D, 100D));
    }

    /**
     * 处理内部业务逻辑（{@code elapsedMillis}）。
     */
    private static long elapsedMillis(long start) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }

    /**
     * 处理内部业务逻辑（{@code downsample}）。
     */
    private static <T> List<T> downsample(List<T> values, int maxPoints) {
        if (values.size() <= maxPoints) {
            return values;
        }
        var result = new ArrayList<T>(maxPoints);
        for (var index = 0; index < maxPoints; index++) {
            var sourceIndex = (int) Math.round(index * (values.size() - 1D) / (maxPoints - 1D));
            result.add(values.get(sourceIndex));
        }
        return result;
    }

    private record MemorySnapshot(long used, long max, double usage) {
    }

    private record RequestMetrics(boolean available, double qps, double errorRate, double p95ResponseMs) {

        /**
         * 处理内部业务逻辑（{@code unavailable}）。
         */
        private static RequestMetrics unavailable() {
            return new RequestMetrics(false, 0D, 0D, 0D);
        }
    }

    private record HealthSnapshot(String status, List<ServiceMonitorOverviewVO.HealthComponent> components,
                                  long latencyMs) {
    }

    private record Freshness(ServiceMonitorFreshness status, long ageSeconds) {
    }

    private record Sample(Instant collectedAt, double cpuUsage, int cpuLogicalCores, double systemMemoryUsage,
                          long totalMemory,
                          long usedMemory, long availableMemory, long jvmHeapUsed, long jvmHeapMax, double jvmHeapUsage,
                          long jvmNonHeapUsed, int liveThreadCount, int peakThreadCount, long gcCount, double qps, double errorRate,
                          double p95ResponseMs, boolean requestMetricsAvailable, List<ServiceMonitorOverviewVO.Dependency> dependencies,
                          List<ServiceMonitorOverviewVO.HealthComponent> healthComponents, String healthStatus,
                          long healthCheckLatencyMs, ServiceMonitorHealthStatus status, String databaseStatus, String redisStatus,
                          long uptimeSeconds) {
    }
}
