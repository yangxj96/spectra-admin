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
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorDiagnosticTask;
import com.devops00.spectra.core.system.javabean.enums.ServiceMonitorDiagnosticType;
import com.devops00.spectra.core.system.javabean.enums.ServiceMonitorDiagnosticTaskStatus;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorDiagnosticFrom;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorDiagnosticTaskVO;
import com.devops00.spectra.core.system.javabean.vo.ServiceMonitorRuntimeDiagnosticVO;
import com.devops00.spectra.core.system.mapper.ServiceMonitorDiagnosticTaskMapper;
import com.devops00.spectra.core.system.service.ServiceMonitorDiagnosticService;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.core.task.TaskExecutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import javax.sql.DataSource;

import java.io.BufferedWriter;
import java.io.IOException;
import java.lang.management.GarbageCollectorMXBean;
import java.lang.management.ManagementFactory;
import java.lang.management.MemoryPoolMXBean;
import java.lang.management.MemoryUsage;
import java.lang.management.ThreadInfo;
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

/**
 * 服务监控运行时诊断和受控诊断文件服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Slf4j
@Service
public class ServiceMonitorDiagnosticServiceImpl implements ServiceMonitorDiagnosticService {

    private static final String PENDING = ServiceMonitorDiagnosticTaskStatus.PENDING.name();
    private static final String RUNNING = ServiceMonitorDiagnosticTaskStatus.RUNNING.name();
    private static final String SUCCEEDED = ServiceMonitorDiagnosticTaskStatus.SUCCEEDED.name();
    private static final String FAILED = ServiceMonitorDiagnosticTaskStatus.FAILED.name();
    private static final String EXPIRED = ServiceMonitorDiagnosticTaskStatus.EXPIRED.name();
    private static final String LEGACY_INSTANCE = "unknown";

    private final MeterRegistry meterRegistry;
    private final DataSource dataSource;
    private final RedisConnectionFactory redisConnectionFactory;
    private final Environment environment;
    private final TimeMapper timeMapper;
    private final ServiceMonitorDiagnosticTaskMapper taskMapper;
    private final TaskExecutor taskExecutor;
    private final Path diagnosticDirectory;
    private final String instanceId;
    private final boolean enabled;
    private final boolean heapDumpEnabled;
    private final int retentionHours;
    private final long threadDumpMaxBytes;
    private final long heapDumpMaxBytes;

    @SuppressWarnings("PMD.ExcessiveParameterList") // EX-B02-PMD-012: 诊断服务基础设施协作者逐项注入。
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
        this.instanceId = resolveInstanceId(environment);
        this.enabled = environment.getProperty("spectra.monitor.diagnostics.enabled", Boolean.class, true);
        this.heapDumpEnabled = environment.getProperty("spectra.monitor.diagnostics.heap-dump-enabled", Boolean.class, false);
        this.retentionHours = Math.max(environment.getProperty("spectra.monitor.diagnostics.retention-hours", Integer.class, 24), 1);
        this.threadDumpMaxBytes = Math.max(environment.getProperty("spectra.monitor.diagnostics.thread-dump-max-bytes", Long.class,
                5L * 1024 * 1024), 1024L);
        this.heapDumpMaxBytes = Math.max(environment.getProperty("spectra.monitor.diagnostics.heap-dump-max-bytes", Long.class,
                512L * 1024 * 1024), 16L * 1024 * 1024);
    }

    private static String resolveInstanceId(Environment environment) {
        var configured = environment.getProperty("spectra.monitor.diagnostics.instance-id");
        if (configured != null && !configured.isBlank()) {
            return normalizeInstanceId(configured.trim());
        }
        var host = environment.getProperty("HOSTNAME");
        if (host == null || host.isBlank()) {
            host = environment.getProperty("COMPUTERNAME");
        }
        if (host == null || host.isBlank()) {
            host = "localhost";
        }
        var port = environment.getProperty("server.port", "0");
        return normalizeInstanceId(host.trim() + ":" + port.trim());
    }

    private static String normalizeInstanceId(String value) {
        if (value.length() <= 160) {
            return value;
        }
        return value.substring(0, 120) + ":" + UUID.nameUUIDFromBytes(value.getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public ServiceMonitorRuntimeDiagnosticVO getRuntimeDiagnostic() {
        return ServiceMonitorRuntimeDiagnosticVO.builder()
                .generatedAt(timeMapper.toOffsetDateTime(Instant.now()))
                .memoryPools(memoryPools())
                .garbageCollectors(garbageCollectors())
                .threadStates(threadStates())
                .connectionPool(connectionPool())
                .redis(redisDiagnostic())
                .slowEndpoints(slowEndpoints())
                .build();
    }

    /**
     * 处理内部业务逻辑（{@code memoryPools}）。
     */
    private List<ServiceMonitorRuntimeDiagnosticVO.MemoryPool> memoryPools() {
        return ManagementFactory.getMemoryPoolMXBeans().stream().map(this::toMemoryPool).toList();
    }

    /**
     * 转换、解析或规范化数据（{@code toMemoryPool}）。
     */
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

    /**
     * 处理内部业务逻辑（{@code value}）。
     */
    private static long value(MemoryUsage usage, ToLongFunction<MemoryUsage> mapper) {
        return usage == null ? 0L : Math.max(mapper.applyAsLong(usage), 0L);
    }

    /**
     * 处理内部业务逻辑（{@code percentage}）。
     */
    private static double percentage(long used, long max) {
        return max <= 0L ? 0D : Math.min((double) used / max * 100D, 100D);
    }

    /**
     * 处理内部业务逻辑（{@code garbageCollectors}）。
     */
    private List<ServiceMonitorRuntimeDiagnosticVO.GarbageCollector> garbageCollectors() {
        return ManagementFactory.getGarbageCollectorMXBeans().stream().map(this::toGarbageCollector).toList();
    }

    /**
     * 转换、解析或规范化数据（{@code toGarbageCollector}）。
     */
    private ServiceMonitorRuntimeDiagnosticVO.GarbageCollector toGarbageCollector(GarbageCollectorMXBean bean) {
        return ServiceMonitorRuntimeDiagnosticVO.GarbageCollector.builder()
                .name(bean.getName())
                .collectionCount(Math.max(bean.getCollectionCount(), 0L))
                .collectionTimeMs(Math.max(bean.getCollectionTime(), 0L))
                .build();
    }

    /**
     * 处理内部业务逻辑（{@code threadStates}）。
     */
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

    /**
     * 执行内部处理逻辑（{@code connectionPool}）。
     */
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

    /**
     * 处理内部业务逻辑（{@code invoke}）。
     */
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

    /**
     * 处理内部业务逻辑（{@code integer}）。
     */
    private static Integer integer(Object value) {
        return value instanceof Number number ? number.intValue() : null;
    }

    /**
     * 处理内部业务逻辑（{@code redisDiagnostic}）。
     */
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

    /**
     * 处理内部业务逻辑（{@code slowEndpoints}）。
     */
    private List<ServiceMonitorRuntimeDiagnosticVO.SlowEndpoint> slowEndpoints() {
        return meterRegistry.find("http.server.requests")
                .timers()
                .stream()
                .map(this::toSlowEndpoint)
                .sorted(Comparator.comparingDouble(ServiceMonitorRuntimeDiagnosticVO.SlowEndpoint::getP95ResponseMs).reversed())
                .limit(10)
                .toList();
    }

    /**
     * 转换、解析或规范化数据（{@code toSlowEndpoint}）。
     */
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

    /**
     * 处理内部业务逻辑（{@code tag}）。
     */
    private static String tag(Iterable<io.micrometer.core.instrument.Tag> tags, String key, String fallback) {
        for (var tag : tags)
            if (key.equals(tag.getKey()))
                return tag.getValue();
        return fallback;
    }

    @Override
    @Transactional
    public ServiceMonitorDiagnosticTaskVO createTask(ServiceMonitorDiagnosticFrom from) {
        var type = validateTaskRequest(from);
        taskMapper.acquireAdmissionLock();
        var running = taskMapper.selectCount(new LambdaQueryWrapper<ServiceMonitorDiagnosticTask>()
                .in(ServiceMonitorDiagnosticTask::getStatus, PENDING, RUNNING)
                .isNull(ServiceMonitorDiagnosticTask::getDeleted));
        if (running > 0)
            throw new DataException("当前已有诊断任务执行中，请稍后再试");
        validateStorage(type);
        var now = Instant.now();
        var task = new ServiceMonitorDiagnosticTask();
        task.setTaskType(type.getCode());
        task.setStatus(PENDING);
        task.setInstanceId(instanceId);
        task.setFileName(UUID.randomUUID() + type.getSuffix());
        task.setDisplayName("服务监控-" + type.getLabel() + "-" + now.toEpochMilli() + type.getSuffix());
        task.setRequestedAt(now);
        task.setExpiresAt(now.plus(Duration.ofHours(retentionHours)));
        taskMapper.insert(task);
        dispatchAfterCommit(task.getId());
        return toVO(task);
    }

    private void dispatchAfterCommit(UUID taskId) {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    dispatch(taskId);
                }
            });
            return;
        }
        dispatch(taskId);
    }

    private void dispatch(UUID taskId) {
        try {
            taskExecutor.execute(() -> runTask(taskId));
        } catch (RuntimeException exception) {
            log.warn("服务监控诊断任务未能进入执行队列，等待恢复扫描: taskId={}", taskId, exception);
        }
    }

    private ServiceMonitorDiagnosticType validateTaskRequest(ServiceMonitorDiagnosticFrom from) {
        if (!enabled) {
            throw new DataException("服务监控诊断功能已关闭");
        }
        var type = ServiceMonitorDiagnosticType.fromCode(from.getTaskType());
        if (type == null) {
            throw new DataException("诊断类型无效");
        }
        if (type == ServiceMonitorDiagnosticType.HEAP_DUMP
                && (!heapDumpEnabled || !Boolean.TRUE.equals(from.getConfirm()))) {
            throw new DataException("堆转储默认关闭，启用后必须显式确认风险");
        }
        return type;
    }

    private void validateStorage(ServiceMonitorDiagnosticType type) {
        try {
            Files.createDirectories(diagnosticDirectory);
            long required = type == ServiceMonitorDiagnosticType.HEAP_DUMP ? heapDumpMaxBytes : threadDumpMaxBytes;
            if (Files.getFileStore(diagnosticDirectory).getUsableSpace() < required) {
                throw new DataException("诊断目录可用空间不足");
            }
        } catch (IOException exception) {
            throw new DataException("诊断目录不可用", exception);
        }
    }

    /**
     * 执行内部处理逻辑（{@code runTask}）。
     */
    private void runTask(UUID id) {
        var task = taskMapper.selectById(id);
        if (task == null) {
            return;
        }
        if (!managesInstance(task.getInstanceId())) {
            log.warn("诊断任务不属于当前实例，跳过执行: taskId={}, owner={}", id, task.getInstanceId());
            return;
        }
        var claimedAt = Instant.now();
        var claimed = taskMapper.update(null, new LambdaUpdateWrapper<ServiceMonitorDiagnosticTask>()
                .eq(ServiceMonitorDiagnosticTask::getId, id)
                .eq(ServiceMonitorDiagnosticTask::getStatus, PENDING)
                .isNull(ServiceMonitorDiagnosticTask::getDeleted)
                .and(wrapper -> wrapper.eq(ServiceMonitorDiagnosticTask::getInstanceId, instanceId)
                        .or()
                        .eq(ServiceMonitorDiagnosticTask::getInstanceId, LEGACY_INSTANCE))
                .and(wrapper -> wrapper.isNull(ServiceMonitorDiagnosticTask::getExpiresAt)
                        .or()
                        .gt(ServiceMonitorDiagnosticTask::getExpiresAt, claimedAt))
                .set(ServiceMonitorDiagnosticTask::getStatus, RUNNING)
                .set(ServiceMonitorDiagnosticTask::getInstanceId, instanceId)
                .set(ServiceMonitorDiagnosticTask::getStartedAt, claimedAt));
        if (claimed != 1) {
            return;
        }
        var type = ServiceMonitorDiagnosticType.fromCode(task.getTaskType());
        if (type == null) {
            finishTask(id, FAILED, null, "诊断类型无效");
            return;
        }
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
            finishTask(id, SUCCEEDED, size, null);
        } catch (Exception exception) {
            deleteQuietly(path);
            finishTask(id, FAILED, null,
                    exception instanceof DataException ? exception.getMessage() : "诊断文件生成失败");
            log.warn("服务监控诊断任务失败: taskId={}, type={}", id, type.getCode(), exception);
        }
    }

    private void finishTask(UUID id, String status, Long fileSize, String errorMessage) {
        taskMapper.update(null, new LambdaUpdateWrapper<ServiceMonitorDiagnosticTask>()
                .eq(ServiceMonitorDiagnosticTask::getId, id)
                .eq(ServiceMonitorDiagnosticTask::getStatus, RUNNING)
                .and(wrapper -> wrapper.eq(ServiceMonitorDiagnosticTask::getInstanceId, instanceId)
                        .or()
                        .eq(ServiceMonitorDiagnosticTask::getInstanceId, LEGACY_INSTANCE))
                .isNull(ServiceMonitorDiagnosticTask::getDeleted)
                .set(ServiceMonitorDiagnosticTask::getStatus, status)
                .set(ServiceMonitorDiagnosticTask::getFileSize, fileSize)
                .set(ServiceMonitorDiagnosticTask::getErrorMessage, errorMessage)
                .set(ServiceMonitorDiagnosticTask::getCompletedAt, Instant.now()));
    }

    /**
     * 恢复因提交前派发、执行器拒绝或实例中断而遗留的诊断任务。
     */
    public void recoverPendingTasks() {
        var cutoff = Instant.now().minusSeconds(30);
        var pending = taskMapper.selectList(new LambdaQueryWrapper<ServiceMonitorDiagnosticTask>()
                .eq(ServiceMonitorDiagnosticTask::getStatus, PENDING)
                .and(wrapper -> wrapper.eq(ServiceMonitorDiagnosticTask::getInstanceId, instanceId)
                        .or()
                        .eq(ServiceMonitorDiagnosticTask::getInstanceId, LEGACY_INSTANCE))
                .isNull(ServiceMonitorDiagnosticTask::getDeleted)
                .le(ServiceMonitorDiagnosticTask::getRequestedAt, cutoff)
                .orderByAsc(ServiceMonitorDiagnosticTask::getRequestedAt)
                .last("LIMIT 10"));
        pending.forEach(task -> dispatch(task.getId()));

        var runningCutoff = Instant.now().minus(Duration.ofHours(1));
        taskMapper.update(null, new LambdaUpdateWrapper<ServiceMonitorDiagnosticTask>()
                .eq(ServiceMonitorDiagnosticTask::getStatus, RUNNING)
                .and(wrapper -> wrapper.eq(ServiceMonitorDiagnosticTask::getInstanceId, instanceId)
                        .or()
                        .eq(ServiceMonitorDiagnosticTask::getInstanceId, LEGACY_INSTANCE))
                .isNull(ServiceMonitorDiagnosticTask::getDeleted)
                .le(ServiceMonitorDiagnosticTask::getStartedAt, runningCutoff)
                .set(ServiceMonitorDiagnosticTask::getStatus, FAILED)
                .set(ServiceMonitorDiagnosticTask::getErrorMessage, "诊断任务执行中断")
                .set(ServiceMonitorDiagnosticTask::getCompletedAt, Instant.now()));
    }

    /**
     * 执行内部处理逻辑（{@code writeThreadDump}）。
     */
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

    /**
     * 执行内部处理逻辑（{@code writeHeapDump}）。
     */
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
        if (!managesInstance(task.getInstanceId()))
            throw new DataException("诊断文件仅可在生成实例下载");
        if (task.getExpiresAt() == null || !task.getExpiresAt().isAfter(Instant.now()))
            throw new DataException("诊断文件已过期");
        var path = safePath(task.getFileName());
        if (!Files.isRegularFile(path))
            throw new DataException("诊断文件不存在");
        return new DiagnosticDownload(path, task.getDisplayName());
    }

    /**
     * 执行内部处理逻辑（{@code cleanupExpiredTasks}）。
     */
    public void cleanupExpiredTasks() {
        var now = Instant.now();
        var tasks = taskMapper.selectList(new LambdaQueryWrapper<ServiceMonitorDiagnosticTask>()
                .isNull(ServiceMonitorDiagnosticTask::getDeleted)
                .le(ServiceMonitorDiagnosticTask::getExpiresAt, now));
        for (var task : tasks) {
            if (!managesInstance(task.getInstanceId())) {
                continue;
            }
            if (task.getFileName() == null || task.getFileName().isBlank()) {
                taskMapper.deleteById(task.getId());
                continue;
            }
            var path = safePath(task.getFileName());
            if (!Files.isRegularFile(path)) {
                if (!LEGACY_INSTANCE.equals(task.getInstanceId()) || !SUCCEEDED.equals(task.getStatus())) {
                    taskMapper.deleteById(task.getId());
                }
                continue;
            }
            if (deleteQuietly(path)) {
                taskMapper.deleteById(task.getId());
            } else {
                taskMapper.update(null, new LambdaUpdateWrapper<ServiceMonitorDiagnosticTask>()
                        .eq(ServiceMonitorDiagnosticTask::getId, task.getId())
                        .set(ServiceMonitorDiagnosticTask::getErrorMessage, "诊断文件清理失败"));
            }
        }
    }

    /**
     * 判断诊断任务是否可在当前实例管理；历史任务由实际持有文件的节点处理。
     */
    private boolean managesInstance(String owner) {
        return owner == null || owner.isBlank() || LEGACY_INSTANCE.equals(owner) || instanceId.equals(owner);
    }

    /**
     * 处理内部业务逻辑（{@code safePath}）。
     */
    private Path safePath(String fileName) {
        if (fileName == null || fileName.isBlank() || fileName.contains("\\") || fileName.contains("/")) {
            throw new DataException("诊断文件路径无效");
        }
        var path = diagnosticDirectory.resolve(fileName).normalize();
        if (!path.startsWith(diagnosticDirectory))
            throw new DataException("诊断文件路径无效");
        return path;
    }

    /**
     * 更新或推进目标状态（{@code deleteQuietly}）。
     */
    private boolean deleteQuietly(Path path) {
        try {
            if (path != null)
                Files.deleteIfExists(path);
            return true;
        } catch (IOException exception) {
            log.warn("服务监控诊断文件清理失败");
            return false;
        }
    }

    /**
     * 转换、解析或规范化数据（{@code toVO}）。
     */
    private ServiceMonitorDiagnosticTaskVO toVO(ServiceMonitorDiagnosticTask task) {
        return ServiceMonitorDiagnosticTaskVO.builder()
                .id(task.getId())
                .taskType(task.getTaskType())
                .status(task.getStatus())
                .displayName(task.getDisplayName())
                .fileSize(task.getFileSize())
                .errorMessage(task.getErrorMessage())
                .requestedAt(timeMapper.toOffsetDateTime(task.getRequestedAt()))
                .startedAt(timeMapper.toOffsetDateTime(task.getStartedAt()))
                .completedAt(timeMapper.toOffsetDateTime(task.getCompletedAt()))
                .expiresAt(timeMapper.toOffsetDateTime(task.getExpiresAt()))
                .build();
    }

    /**
     * 处理内部业务逻辑（{@code elapsedMillis}）。
     */
    private static long elapsedMillis(long start) {
        return TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
    }
}
