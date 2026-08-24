/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorDiagnosticTask;
import com.devops00.spectra.core.system.javabean.enums.ServiceMonitorDiagnosticType;
import com.devops00.spectra.core.system.javabean.enums.ServiceMonitorDiagnosticTaskStatus;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorDiagnosticFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorDiagnosticTaskVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorRuntimeDiagnosticVO;
import com.devops00.spectra.core.system.mapper.ServiceMonitorDiagnosticTaskMapper;
import com.devops00.spectra.core.system.service.ServiceMonitorDiagnosticService;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import javax.sql.DataSource;
import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.Duration;
import java.time.Instant;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.ToLongFunction;

import com.sun.management.HotSpotDiagnosticMXBean;

/** 服务监控运行时诊断和受控诊断文件服务实现。 */
@Slf4j
@Service
public class ServiceMonitorDiagnosticServiceImpl implements ServiceMonitorDiagnosticService {

    private static final String PENDING = ServiceMonitorDiagnosticTaskStatus.PENDING.name();
    private static final String RUNNING = ServiceMonitorDiagnosticTaskStatus.RUNNING.name();
    private static final String SUCCEEDED = ServiceMonitorDiagnosticTaskStatus.SUCCEEDED.name();
    private static final String FAILED = ServiceMonitorDiagnosticTaskStatus.FAILED.name();
    private static final String EXPIRED = ServiceMonitorDiagnosticTaskStatus.EXPIRED.name();

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final Environment environment;
    private final TimeMapper timeMapper;
    private final ServiceMonitorDiagnosticTaskMapper taskMapper;
    private final TaskExecutor taskExecutor;
    private final Path diagnosticDirectory;
    private final boolean enabled;
    private final boolean heapDumpEnabled;
    private final int retentionHours;
    private final long threadDumpMaxBytes;
    private final long heapDumpMaxBytes;

    public ServiceMonitorDiagnosticServiceImpl(MeterRegistry meterRegistry, DataSource dataSource,
                                               RedisConnectionFactory redisConnectionFactory, Environment environment,
                                               TimeMapper timeMapper, ServiceMonitorDiagnosticTaskMapper taskMapper,
                                               @Qualifier("serviceMonitorDiagnosticTaskExecutor") TaskExecutor taskExecutor) {
        this.meterRegistry = meterRegistry;
        this.dataSource = dataSource;
        this.redisConnectionFactory = redisConnectionFactory;
        this.environment = environment;
        this.timeMapper = timeMapper;
        this.taskMapper = taskMapper;
        this.taskExecutor = taskExecutor;
        var defaultDirectory = Path.of(System.getProperty("java.io.tmpdir"), "spectra-monitor-diagnostics");
        this.diagnosticDirectory = Path.of(environment.getProperty("spectra.monitor.diagnostics.directory",
                defaultDirectory.toString())).toAbsolutePath().normalize();
        this.enabled = environment.getProperty("spectra.monitor.diagnostics.enabled", Boolean.class, true);
        this.heapDumpEnabled = environment.getProperty("spectra.monitor.diagnostics.heap-dump-enabled", Boolean.class, false);
        this.retentionHours = Math.max(environment.getProperty("spectra.monitor.diagnostics.retention-hours", Integer.class, 24), 1);
        this.threadDumpMaxBytes = Math.max(environment.getProperty("spectra.monitor.diagnostics.thread-dump-max-bytes", Long.class,
                5L * 1024 * 1024), 1024L);
        this.heapDumpMaxBytes = Math.max(environment.getProperty("spectra.monitor.diagnostics.heap-dump-max-bytes", Long.class,
                512L * 1024 * 1024), 16L * 1024 * 1024);
    }

    @Override
    public ServiceMonitorRuntimeDiagnosticVO getRuntimeDiagnostic() {
        return ServiceMonitorRuntimeDiagnosticVO.builder()
                .generatedAt(timeMapper.toLocalDateTime(Instant.now()))
                .memoryPools(memoryPools())
                .garbageCollectors(garbageCollectors())
                .threadStates(threadStates())
                .connectionPool(connectionPool())
                .redis(redisDiagnostic())
                .slowEndpoints(slowEndpoints())
                .build();
    }

    private List<ServiceMonitorRuntimeDiagnosticVO.MemoryPool> memoryPools() {
        return ManagementFactory.getMemoryPoolMXBeans().stream().map(this::toMemoryPool).toList();
    }

    private ServiceMonitorRuntimeDiagnosticVO.MemoryPool toMemoryPool(MemoryPoolMXBean bean) {
        var usage = bean.getUsage();
        return ServiceMonitorRuntimeDiagnosticVO.MemoryPool.builder()
                .name(bean.getName())
                .usedBytes(value(usage, MemoryUsage::getUsed))
                .committedBytes(value(usage, MemoryUsage::getCommitted))
                .maxBytes(value(usage, MemoryUsage::getMax))
                .usage(percentage(value(usage, MemoryUsage::getUsed), value(usage, MemoryUsage::getMax)))
                .build();
    }

    private static long value(MemoryUsage usage, ToLongFunction<MemoryUsage> mapper) {
        return usage == null ? 0L : Math.max(mapper.applyAsLong(usage), 0L);
    }

    private static double percentage(long used, long max) {
        return max <= 0L ? 0D : Math.min((double) used / max * 100D, 100D);
    }

    private List<ServiceMonitorRuntimeDiagnosticVO.GarbageCollector> garbageCollectors() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream().map(this::toGarbageCollector).toList();
    }

    private ServiceMonitorRuntimeDiagnosticVO.GarbageCollector toGarbageCollector(GarbageCollectorMXBean bean) {
        return ServiceMonitorRuntimeDiagnosticVO.GarbageCollector.builder()
                .name(bean.getName())
                .collectionCount(Math.max(bean.getCollectionCount(), 0L))
                .collectionTimeMs(Math.max(bean.getCollectionTime(), 0L))
                .build();
    }

    private List<ServiceMonitorRuntimeDiagnosticVO.ThreadStateCount> threadStates() {
        var counts = new EnumMap<Thread.State, Long>(Thread.State.class);
        for (var state : Thread.State.values())
            counts.put(state, 0L);
        var bean = ManagementFactory.getThreadMXBean();
        for (var id : bean.getAllThreadIds()) {
            var info = bean.getThreadInfo(id);
            if (info != null)
                counts.compute(info.getThreadState(), (key, value) -> value + 1L);
        }
        return counts.entrySet()
                .stream()
                .map(item -> ServiceMonitorRuntimeDiagnosticVO.ThreadStateCount.builder()
                        .state(item.getKey().name())
                        .count(item.getValue())
                        .build())
                .toList();
    }

    private ServiceMonitorRuntimeDiagnosticVO.ConnectionPool connectionPool() {
        try {
            var pool = invoke(dataSource, "getHikariPoolMXBean");
            if (pool == null) {
                return ServiceMonitorRuntimeDiagnosticVO.ConnectionPool.builder()
                        .name("数据库连接池")
                        .status("UNSUPPORTED")
                        .build();
            }
            return ServiceMonitorRuntimeDiagnosticVO.ConnectionPool.builder()
                    .name("数据库连接池")
                    .status("UP")
                    .active(integer(invoke(pool, "getActiveConnections")))
                    .idle(integer(invoke(pool, "getIdleConnections")))
                    .total(integer(invoke(pool, "getTotalConnections")))
                    .maximum(integer(invoke(pool, "getMaxConnections")))
                    .build();
        } catch (RuntimeException exception) {
            return ServiceMonitorRuntimeDiagnosticVO.ConnectionPool.builder()
                    .name("数据库连接池")
                    .status("UNKNOWN")
                    .build();
        }
    }

    private static Object invoke(Object target, String methodName) {
        if (target == null)
            return null;
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (ReflectiveOperationException exception) {
            return null;
        }
    }

    private static Integer integer(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    private ServiceMonitorRuntimeDiagnosticVO.RedisDiagnostic redisDiagnostic() {
        var start = System.nanoTime();
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            var up = "PONG".equalsIgnoreCase(connection.ping());
            return ServiceMonitorRuntimeDiagnosticVO.RedisDiagnostic.builder()
                    .status(up ? "UP" : "DOWN")
                    .latencyMs(elapsedMillis(start))
                    .build();
        } catch (RuntimeException exception) {
            return ServiceMonitorRuntimeDiagnosticVO.RedisDiagnostic.builder()
                    .status("DOWN")
                    .latencyMs(elapsedMillis(start))
                    .build();
        }
    }

    private List<ServiceMonitorRuntimeDiagnosticVO.SlowEndpoint> slowEndpoints() {
        return meterRegistry.find("http.server.requests")
                .timers()
                .stream()
                .map(this::toSlowEndpoint)
                .sorted(Comparator.comparingDouble(ServiceMonitorRuntimeDiagnosticVO.SlowEndpoint::getP95ResponseMs).reversed())
                .limit(10)
                .toList();
    }

    private ServiceMonitorRuntimeDiagnosticVO.SlowEndpoint toSlowEndpoint(Timer timer) {
        var tags = timer.getId().getTags();
        return ServiceMonitorRuntimeDiagnosticVO.SlowEndpoint.builder()
                .method(tag(tags, "method", "-"))
                .uri(tag(tags, "uri", "-"))
                .status(tag(tags, "status", "-"))
                .count(timer.count())
                .p95ResponseMs(timer.percentile(0.95D, TimeUnit.MILLISECONDS))
                .build();
    }

    private static String tag(Iterable<io.micrometer.core.instrument.Tag> tags, String key, String fallback) {
        for (var tag : tags)
            if (key.equals(tag.getKey()))
                return tag.getValue();
        return fallback;
    }

    @Override
    public ServiceMonitorDiagnosticTaskVO createTask(ServiceMonitorDiagnosticFrom from) {
        if (!enabled)
            throw new DataException("服务监控诊断功能已关闭");
        var type = ServiceMonitorDiagnosticType.fromCode(from.getTaskType());
        if (type == null)
            throw new DataException("诊断类型无效");
        if (type == ServiceMonitorDiagnosticType.HEAP_DUMP && (!heapDumpEnabled || !Boolean.TRUE.equals(from.getConfirm()))) {
            throw new DataException("堆转储默认关闭，启用后必须显式确认风险");
        }
        var running = taskMapper.selectCount(new LambdaQueryWrapper<ServiceMonitorDiagnosticTask>()
                .in(ServiceMonitorDiagnosticTask::getStatus, PENDING, RUNNING)
                .isNull(ServiceMonitorDiagnosticTask::getDeleted));
        if (running > 0)
            throw new DataException("当前已有诊断任务执行中，请稍后再试");
        try {
            Files.createDirectories(diagnosticDirectory);
            if (Files.getFileStore(diagnosticDirectory).getUsableSpace() < (type == ServiceMonitorDiagnosticType.HEAP_DUMP
                    ? heapDumpMaxBytes
                    : threadDumpMaxBytes)) {
                throw new DataException("诊断目录可用空间不足");
            }
        } catch (IOException exception) {
            throw new DataException("诊断目录不可用");
        }
        var now = Instant.now();
        var task = new ServiceMonitorDiagnosticTask();
        task.setTaskType(type.getCode());
        task.setStatus(PENDING);
        task.setFileName(UUID.randomUUID() + type.getSuffix());
        task.setDisplayName("服务监控-" + type.getLabel() + "-" + now.toEpochMilli() + type.getSuffix());
        task.setRequestedAt(now);
        task.setExpiresAt(now.plus(Duration.ofHours(retentionHours)));
        taskMapper.insert(task);
        try {
            taskExecutor.execute(() -> runTask(task.getId(), type));
        } catch (RuntimeException exception) {
            task.setStatus(FAILED);
            task.setErrorMessage("诊断任务未能启动");
            task.setCompletedAt(Instant.now());
            taskMapper.updateById(task);
            throw new DataException("诊断任务未能启动");
        }
        return toVO(task);
    }

    private void runTask(UUID id, ServiceMonitorDiagnosticType type) {
        var task = taskMapper.selectById(id);
        if (task == null)
            return;
        task.setStatus(RUNNING);
        task.setStartedAt(Instant.now());
        taskMapper.updateById(task);
        var path = safePath(task.getFileName());
        try {
            if (type == ServiceMonitorDiagnosticType.THREAD_DUMP)
                writeThreadDump(path);
            else
                writeHeapDump(path);
            var size = Files.size(path);
            var limit = type == ServiceMonitorDiagnosticType.THREAD_DUMP ? threadDumpMaxBytes : heapDumpMaxBytes;
            if (size > limit)
                throw new DataException("诊断文件超过大小限制");
            task.setStatus(SUCCEEDED);
            task.setFileSize(size);
            task.setCompletedAt(Instant.now());
            taskMapper.updateById(task);
        } catch (Exception exception) {
            deleteQuietly(path);
            task.setStatus(FAILED);
            task.setErrorMessage(exception instanceof DataException ? exception.getMessage() : "诊断文件生成失败");
            task.setCompletedAt(Instant.now());
            taskMapper.updateById(task);
            log.warn("服务监控诊断任务失败: taskId={}, type={}", id, type.getCode(), exception);
        }
    }

    private void writeThreadDump(Path path) throws IOException {
        var bean = ManagementFactory.getThreadMXBean();
        try (BufferedWriter writer = Files.newBufferedWriter(path, StandardCharsets.UTF_8, StandardOpenOption.CREATE_NEW)) {
            writer.write("Spectra 服务监控线程转储\n生成时间：" + Instant.now() + "\n\n");
            for (ThreadInfo info : bean.dumpAllThreads(false, false)) {
                writer.write('"' + info.getThreadName() + '"' + " Id=" + info.getThreadId() + " " + info.getThreadState() + "\n");
                for (var element : info.getStackTrace())
                    writer.write("    at " + element + "\n");
                writer.write("\n");
                writer.flush();
                if (Files.size(path) > threadDumpMaxBytes)
                    throw new DataException("线程转储超过大小限制");
            }
        }
    }

    private void writeHeapDump(Path path) throws IOException {
        var bean = ManagementFactory.getPlatformMXBean(HotSpotDiagnosticMXBean.class);
        if (bean == null)
            throw new DataException("当前 JDK 不支持堆转储");
        if (Files.getFileStore(diagnosticDirectory).getUsableSpace() < heapDumpMaxBytes) {
            throw new DataException("诊断目录可用空间不足");
        }
        bean.dumpHeap(path.toString(), true);
    }

    @Override
    public ServiceMonitorDiagnosticTaskVO getTask(UUID id) {
        var task = taskMapper.selectOne(new LambdaQueryWrapper<ServiceMonitorDiagnosticTask>()
                .eq(ServiceMonitorDiagnosticTask::getId, id)
                .isNull(ServiceMonitorDiagnosticTask::getDeleted));
        if (task == null)
            throw new DataException("诊断任务不存在");
        if (task.getExpiresAt() != null && task.getExpiresAt().isBefore(Instant.now()) && !EXPIRED.equals(task.getStatus())) {
            task.setStatus(EXPIRED);
            taskMapper.updateById(task);
        }
        return toVO(task);
    }

    @Override
    public List<ServiceMonitorDiagnosticTaskVO> listTasks() {
        return taskMapper.selectList(new LambdaQueryWrapper<ServiceMonitorDiagnosticTask>()
                .isNull(ServiceMonitorDiagnosticTask::getDeleted)
                .orderByDesc(ServiceMonitorDiagnosticTask::getRequestedAt)
                .last("LIMIT 30"))
                .stream()
                .map(this::toVO)
                .toList();
    }

    @Override
    public DiagnosticDownload openDownload(UUID id) {
        var task = taskMapper.selectOne(new LambdaQueryWrapper<ServiceMonitorDiagnosticTask>()
                .eq(ServiceMonitorDiagnosticTask::getId, id)
                .isNull(ServiceMonitorDiagnosticTask::getDeleted));
        if (task == null || !SUCCEEDED.equals(task.getStatus()))
            throw new DataException("诊断文件尚未生成");
        if (task.getExpiresAt() == null || !task.getExpiresAt().isAfter(Instant.now()))
            throw new DataException("诊断文件已过期");
        var path = safePath(task.getFileName());
        if (!Files.isRegularFile(path))
            throw new DataException("诊断文件不存在");
        return new DiagnosticDownload(path, task.getDisplayName());
    }

    @Scheduled(fixedDelayString = "${spectra.monitor.diagnostics.cleanup-interval-ms:3600000}", initialDelay = 60000)
    public void cleanupExpiredTasks() {
        var now = Instant.now();
        var tasks = taskMapper.selectList(new LambdaQueryWrapper<ServiceMonitorDiagnosticTask>()
                .isNull(ServiceMonitorDiagnosticTask::getDeleted)
                .le(ServiceMonitorDiagnosticTask::getExpiresAt, now));
        for (var task : tasks) {
            deleteQuietly(safePath(task.getFileName()));
            taskMapper.deleteById(task.getId());
        }
    }

    private Path safePath(String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("\\") || fileName.contains("/")) {
            throw new DataException("诊断文件路径无效");
        }
        var path = diagnosticDirectory.resolve(fileName).normalize();
        if (!path.startsWith(diagnosticDirectory))
            throw new DataException("诊断文件路径无效");
        return path;
    }

    private void deleteQuietly(Path path) {
        try {
            if (path != null)
                Files.deleteIfExists(path);
        } catch (IOException exception) {
            log.warn("服务监控诊断文件清理失败");
        }
    }

    private ServiceMonitorDiagnosticTaskVO toVO(ServiceMonitorDiagnosticTask task) {
        return ServiceMonitorDiagnosticTaskVO.builder()
                .id(task.getId())
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .displayName(task.getDisplayName())
                .fileSize(task.getFileSize())
                .errorMessage(task.getErrorMessage())
                .requestedAt(timeMapper.toLocalDateTime(task.getRequestedAt()))
                .startedAt(timeMapper.toLocalDateTime(task.getStartedAt()))
                .completedAt(timeMapper.toLocalDateTime(task.getCompletedAt()))
                .expiresAt(timeMapper.toLocalDateTime(task.getExpiresAt()))
                .build();
    }

    private static long elapsedMillis(long start) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }
}
