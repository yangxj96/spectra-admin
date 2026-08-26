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

package com.devops00.spectra.core.scheduler.service;

import com.devops00.spectra.common.scheduler.ScheduledJobContext;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobResult;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.common.scheduler.ScheduledTriggerType;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerExecutionEntity;
import com.devops00.spectra.core.scheduler.javabean.entity.SchedulerJobEntity;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerExecutionStatus;
import com.devops00.spectra.core.scheduler.javabean.enums.SchedulerResolutionStatus;
import com.devops00.spectra.core.scheduler.javabean.domain.ExecutionResolution;
import com.devops00.spectra.core.scheduler.mapper.SchedulerExecutionMapper;
import com.devops00.spectra.core.scheduler.mapper.SchedulerJobMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.math.BigDecimal;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.support.CronExpression;

/** 离散调度执行的入队、领取、处理器调用和结果回写。 */
@Slf4j
@Service
public class SchedulerExecutionService {

    private static final String HANDLER_VERSION = "1.0.0";
    private static final Duration DEFAULT_TIMEOUT = Duration.ofMinutes(5);
    private static final Duration DEFAULT_RETRY_DELAY = Duration.ofSeconds(5);
    private static final Pattern ERROR_CODE_PATTERN = Pattern.compile("[A-Z0-9_.:-]{1,100}");

    private final SchedulerJobMapper jobMapper;
    private final SchedulerExecutionMapper executionMapper;
    private final ScheduledJobRegistry registry;
    private final SchedulerTimeZoneResolver timeZoneResolver;
    private final ExecutionLeaseService leaseService;
    private final ExecutionStateMachine stateMachine;
    private final IdempotencyService idempotencyService;
    private final Clock clock;

    @Autowired
    public SchedulerExecutionService(SchedulerJobMapper jobMapper,
                                     SchedulerExecutionMapper executionMapper,
                                     ScheduledJobRegistry registry,
                                     SchedulerTimeZoneResolver timeZoneResolver,
                                     ExecutionLeaseService leaseService,
                                     ExecutionStateMachine stateMachine,
                                     IdempotencyService idempotencyService) {
        this(jobMapper, executionMapper, registry, timeZoneResolver, leaseService, stateMachine,
                idempotencyService, Clock.systemUTC());
    }

    SchedulerExecutionService(SchedulerJobMapper jobMapper,
                              SchedulerExecutionMapper executionMapper,
                              ScheduledJobRegistry registry,
                              SchedulerTimeZoneResolver timeZoneResolver,
                              ExecutionLeaseService leaseService,
                              ExecutionStateMachine stateMachine,
                              IdempotencyService idempotencyService,
                              Clock clock) {
        this.jobMapper = jobMapper;
        this.executionMapper = executionMapper;
        this.registry = registry;
        this.timeZoneResolver = timeZoneResolver;
        this.leaseService = leaseService;
        this.stateMachine = stateMachine;
        this.idempotencyService = idempotencyService;
        this.clock = clock;
    }

    /**
     * 在一个短事务内生成 due 执行并推进任务计划。
     * <p>处理器调用不在这个事务中发生。</p>
     */
    @Transactional
    public int dispatchDueJobs(Instant now, int limit) {
        if (now == null || limit <= 0) {
            throw new IllegalArgumentException("due 派发参数不合法");
        }
        ZoneId zone = timeZoneResolver.resolve();
        int inserted = 0;
        for (var job : jobMapper.selectDueJobs(now, limit)) {
            if (job.getJobType() == ScheduledJobType.LOOP
                    || job.getDefinitionStatus() == null
                    || job.getNextFireAt() == null) {
                continue;
            }
            var descriptor = registry.find(job.getJobKey()).orElse(null);
            if (descriptor == null) {
                continue;
            }
            var execution = newExecution(job, descriptor, now);
            int created = executionMapper.insertIfAbsent(execution);
            if (created == 1) {
                inserted++;
            }
            var nextFireAt = nextFireAt(job, job.getNextFireAt(), now, zone);
            if (jobMapper.advanceNextFire(job.getId(), job.getVersion(), nextFireAt) != 1) {
                throw new IllegalStateException("推进调度任务下一次计划失败: " + job.getJobKey());
            }
        }
        return inserted;
    }

    /** 领取并在事务外调用到期执行处理器。 */
    public int executeClaimable(Instant now, int limit, String instanceId) {
        if (now == null || limit <= 0 || instanceId == null || instanceId.isBlank()) {
            throw new IllegalArgumentException("执行领取参数不合法");
        }
        executionMapper.markExpiredAsUnknown(now, "WORKER_LEASE_EXPIRED", "执行租约已过期，结果无法确认");
        int executed = 0;
        for (var execution : executionMapper.selectClaimable(now, limit)) {
            long expectedVersion = execution.getVersion();
            if (execution.getStatus() == SchedulerExecutionStatus.RETRY_WAIT) {
                if (executionMapper.requeueExecution(execution.getId(), expectedVersion, now) != 1) {
                    continue;
                }
                expectedVersion++;
                execution.setVersion(expectedVersion);
                execution.setStatus(SchedulerExecutionStatus.QUEUED);
            }
            var lockedAt = clock.instant();
            var leaseExpiresAt = ExecutionLeaseService.leaseExpiresAt(lockedAt, leaseDuration(execution, lockedAt));
            if (!leaseService.claim(new ExecutionLeaseService.SchedulerExecutionLease(
                    execution.getId(), expectedVersion, instanceId, lockedAt, leaseExpiresAt))) {
                continue;
            }
            execution.setVersion(expectedVersion + 1);
            execution.setStatus(SchedulerExecutionStatus.RUNNING);
            executeOne(execution, instanceId);
            executed++;
        }
        return executed;
    }

    /** 人工确认 UNKNOWN 结果；原始执行 status 保持 UNKNOWN。 */
    public boolean resolveUnknown(UUID executionId, long expectedVersion, ExecutionResolution resolution) {
        if (executionId == null
                || expectedVersion < 0
                || resolution == null
                || resolution.status() == null
                || resolution.reason() == null
                || resolution.reason().isBlank()) {
            throw new IllegalArgumentException("UNKNOWN 解决参数不合法");
        }
        stateMachine.resolveUnknown(SchedulerExecutionStatus.UNKNOWN, resolution.status());
        var resolvedAt = clock.instant();
        return executionMapper.resolveUnknown(executionId, expectedVersion, resolution.status(),
                safeMessage(resolution.reason()), resolution.resolvedBy(), resolvedAt) == 1;
    }

    /** 创建一条人工触发执行；人工触发不推进周期任务的 next_fire_at。 */
    @Transactional
    public SchedulerExecutionEntity triggerManual(UUID jobId, Map<String, Object> parameters,
                                                  String idempotencyKey, Instant requestedAt) {
        if (jobId == null || idempotencyKey == null || idempotencyKey.isBlank()) {
            throw new IllegalArgumentException("手工触发缺少任务或幂等键");
        }
        var job = jobMapper.selectById(jobId);
        var descriptor = job == null ? null : registry.find(job.getJobKey()).orElse(null);
        if (job == null || descriptor == null || job.getJobType() == ScheduledJobType.LOOP) {
            throw new IllegalStateException("手工触发目标任务不可用或不支持离散执行");
        }
        if (job.getDefinitionStatus() != com.devops00.spectra.core.scheduler.javabean.enums.SchedulerDefinitionStatus.REGISTERED) {
            throw new IllegalStateException("任务定义未注册，禁止手工触发");
        }
        validateParameters(descriptor, parameters);
        var now = requestedAt == null ? clock.instant() : requestedAt;
        var fireKey = idempotencyService.manualKey(jobId, idempotencyKey);
        var existing = executionMapper.selectByFireKey(fireKey);
        if (existing != null) {
            return existing;
        }
        var execution = newExecution(job, descriptor, now, now);
        execution.setFireKey(fireKey);
        execution.setTriggerType(ScheduledTriggerType.MANUAL);
        execution.setParametersSnapshot(parameters == null ? Map.of() : safeParameters(parameters));
        if (executionMapper.insertIfAbsent(execution) == 1) {
            return execution;
        }
        existing = executionMapper.selectByFireKey(fireKey);
        if (existing == null) {
            throw new IllegalStateException("手工触发幂等插入失败且无法读取原执行");
        }
        return existing;
    }

    /** 手工重试创建新执行；原执行记录和 UNKNOWN 原状态不被覆盖。 */
    @Transactional
    public SchedulerExecutionEntity retryExecution(UUID originalExecutionId, UUID resolvedBy,
                                                   String reason, String idempotencyKey) {
        if (originalExecutionId == null
                || idempotencyKey == null
                || idempotencyKey.isBlank()
                || reason == null
                || reason.isBlank()) {
            throw new IllegalArgumentException("手工重试缺少执行或幂等键");
        }
        var original = executionMapper.selectById(originalExecutionId);
        if (original == null
                || (original.getStatus() != SchedulerExecutionStatus.UNKNOWN
                        && original.getStatus() != SchedulerExecutionStatus.FAILED)) {
            throw new IllegalStateException("只有 FAILED 或 UNKNOWN 执行可以手工重试");
        }
        if (original.getStatus() == SchedulerExecutionStatus.UNKNOWN
                && original.getResolutionStatus() != SchedulerResolutionStatus.UNRESOLVED) {
            throw new IllegalStateException("UNKNOWN 执行已经解决，不能重复重试");
        }
        var job = jobMapper.selectById(original.getJobId());
        var descriptor = job == null ? null : registry.find(job.getJobKey()).orElse(null);
        if (job == null || descriptor == null || job.getJobType() == ScheduledJobType.LOOP) {
            throw new IllegalStateException("重试目标任务处理器不可用或不支持离散执行");
        }
        var fireKey = idempotencyService.retryKey(originalExecutionId, idempotencyKey);
        var existing = executionMapper.selectByFireKey(fireKey);
        if (existing != null) {
            return existing;
        }
        var now = clock.instant();
        var retry = newExecution(job, descriptor, now, now);
        retry.setFireKey(fireKey);
        retry.setTriggerType(ScheduledTriggerType.RETRY);
        retry.setJobRevision(original.getJobRevision());
        retry.setHandlerVersion(original.getHandlerVersion());
        retry.setScheduleKindSnapshot(original.getScheduleKindSnapshot());
        retry.setScheduleExpressionSnapshot(original.getScheduleExpressionSnapshot());
        retry.setParametersSnapshot(original.getParametersSnapshot() == null
                ? Map.of()
                : safeParameters(original.getParametersSnapshot()));
        retry.setEffectType(original.getEffectType());
        retry.setMaxAttempts(original.getMaxAttempts());
        retry.setOriginalExecutionId(originalExecutionId);
        if (executionMapper.insertIfAbsent(retry) != 1) {
            existing = executionMapper.selectByFireKey(fireKey);
            if (existing != null) {
                return existing;
            }
            throw new IllegalStateException("创建手工重试执行失败");
        }
        if (original.getStatus() == SchedulerExecutionStatus.UNKNOWN
                && !resolveUnknown(originalExecutionId, original.getVersion(),
                        new ExecutionResolution(SchedulerResolutionStatus.RETRIED, reason, resolvedBy))) {
            throw new IllegalStateException("登记 UNKNOWN 重试解决状态失败");
        }
        return retry;
    }

    /** 仅取消尚未开始的离散执行。 */
    public boolean cancel(UUID executionId, long expectedVersion, String reason, Instant cancelledAt) {
        if (executionId == null || expectedVersion < 0 || reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("取消执行参数不合法");
        }
        return executionMapper.cancelExecution(executionId, expectedVersion, safeMessage(reason),
                cancelledAt == null ? clock.instant() : cancelledAt) == 1;
    }

    /**
     * 为 UNKNOWN 执行创建独立的新重试记录，并在原记录上登记 RETRIED。
     * <p>原执行状态和历史不会被覆盖。</p>
     */
    @Transactional
    public SchedulerExecutionEntity retryUnknown(UUID originalExecutionId, UUID resolvedBy, String reason) {
        var original = executionMapper.selectById(originalExecutionId);
        if (original == null
                || original.getStatus() != SchedulerExecutionStatus.UNKNOWN
                || original.getResolutionStatus() != SchedulerResolutionStatus.UNRESOLVED) {
            throw new IllegalStateException("只有未解决的 UNKNOWN 执行可以重试");
        }
        var job = jobMapper.selectById(original.getJobId());
        var descriptor = job == null ? null : registry.find(job.getJobKey()).orElse(null);
        if (job == null || descriptor == null) {
            throw new IllegalStateException("UNKNOWN 执行对应的任务处理器不可用");
        }
        var retry = newExecution(job, descriptor, clock.instant());
        retry.setId(UUID.randomUUID());
        retry.setFireKey(idempotencyService.retryKey(originalExecutionId, retry.getId()));
        retry.setTriggerType(ScheduledTriggerType.RETRY);
        var retryAt = clock.instant();
        retry.setJobRevision(original.getJobRevision());
        retry.setHandlerVersion(original.getHandlerVersion());
        retry.setScheduleKindSnapshot(original.getScheduleKindSnapshot());
        retry.setScheduleExpressionSnapshot(original.getScheduleExpressionSnapshot());
        retry.setParametersSnapshot(original.getParametersSnapshot() == null
                ? Map.of()
                : Map.copyOf(original.getParametersSnapshot()));
        retry.setEffectType(original.getEffectType());
        retry.setScheduledAt(retryAt);
        retry.setQueuedAt(retryAt);
        retry.setDeadlineAt(retryAt.plus(timeout(descriptor, job)));
        retry.setAttemptNo(1);
        retry.setMaxAttempts(original.getMaxAttempts());
        retry.setCreatedAt(retryAt);
        retry.setUpdatedAt(retryAt);
        retry.setOriginalExecutionId(originalExecutionId);
        if (executionMapper.insertIfAbsent(retry) != 1) {
            throw new IllegalStateException("创建 UNKNOWN 重试执行失败");
        }
        if (!resolveUnknown(originalExecutionId, original.getVersion(),
                new ExecutionResolution(SchedulerResolutionStatus.RETRIED, reason, resolvedBy))) {
            throw new IllegalStateException("登记 UNKNOWN 重试解决状态失败");
        }
        return retry;
    }

    private void executeOne(SchedulerExecutionEntity execution, String instanceId) {
        var job = jobMapper.selectById(execution.getJobId());
        var descriptor = job == null ? null : registry.find(job.getJobKey()).orElse(null);
        var handler = job == null ? null : registry.findJobHandler(job.getJobKey()).orElse(null);
        ScheduledJobResult result;
        if (job == null || descriptor == null || handler == null) {
            result = ScheduledJobResult.builder()
                    .status(ScheduledJobResult.Status.UNKNOWN)
                    .errorCode("HANDLER_UNAVAILABLE")
                    .sanitizedMessage("调度处理器不可用，执行结果无法确认")
                    .build();
        } else {
            var context = ScheduledJobContext.builder()
                    .executionId(execution.getId())
                    .jobKey(job.getJobKey())
                    .handlerKey(job.getHandlerKey())
                    .jobRevision(execution.getJobRevision())
                    .handlerVersion(execution.getHandlerVersion())
                    .fireKey(execution.getFireKey())
                    .instanceId(instanceId)
                    .scheduledAt(execution.getScheduledAt())
                    .deadline(execution.getDeadlineAt())
                    .parameters(execution.getParametersSnapshot())
                    .actorType("SYSTEM_JOB")
                    .actorId(job.getJobKey())
                    .build();
            try {
                result = handler.execute(context);
                if (result == null) {
                    result = ScheduledJobResult.builder()
                            .status(ScheduledJobResult.Status.UNKNOWN)
                            .errorCode("NULL_HANDLER_RESULT")
                            .sanitizedMessage("处理器未返回可确认结果")
                            .build();
                }
            } catch (RuntimeException exception) {
                result = ScheduledJobResult.builder()
                        .status(ScheduledJobResult.Status.UNKNOWN)
                        .errorCode("HANDLER_EXCEPTION")
                        .sanitizedMessage("处理器异常，外部副作用无法确认")
                        .build();
                log.error("调度处理器执行异常: jobKey={}", job.getJobKey(), exception);
            }
        }
        persistResult(execution, instanceId, clock.instant(), result);
    }

    private void persistResult(SchedulerExecutionEntity execution,
                               String instanceId,
                               Instant finishedAt,
                               ScheduledJobResult result) {
        var targetStatus = targetStatus(execution, result);
        stateMachine.transition(SchedulerExecutionStatus.RUNNING, targetStatus);
        var updated = executionMapper.finishExecution(
                execution.getId(),
                execution.getVersion(),
                instanceId,
                targetStatus.name(),
                safeErrorCode(result.errorCode()),
                safeMessage(result.sanitizedMessage()),
                safeSummary(result.resultSummary()),
                retryAt(execution, result, targetStatus, finishedAt),
                targetStatus == SchedulerExecutionStatus.RETRY_WAIT ? null : finishedAt);
        if (updated != 1) {
            log.warn("调度执行完成 CAS 失败，忽略迟到结果: executionId={}", execution.getId());
        }
    }

    private SchedulerExecutionStatus targetStatus(SchedulerExecutionEntity execution, ScheduledJobResult result) {
        return switch (result.status()) {
            case SUCCEEDED -> SchedulerExecutionStatus.SUCCEEDED;
            case FAILED -> SchedulerExecutionStatus.FAILED;
            case UNKNOWN -> SchedulerExecutionStatus.UNKNOWN;
            case RETRYABLE -> isSafeRetry(execution) && execution.getAttemptNo() < execution.getMaxAttempts()
                    ? SchedulerExecutionStatus.RETRY_WAIT
                    : SchedulerExecutionStatus.FAILED;
        };
    }

    private Instant retryAt(SchedulerExecutionEntity execution,
                            ScheduledJobResult result,
                            SchedulerExecutionStatus targetStatus,
                            Instant now) {
        if (targetStatus != SchedulerExecutionStatus.RETRY_WAIT) {
            return null;
        }
        return result.retryAt() == null ? now.plus(DEFAULT_RETRY_DELAY) : result.retryAt();
    }

    private boolean isSafeRetry(SchedulerExecutionEntity execution) {
        return execution.getEffectType() != com.devops00.spectra.common.scheduler.ScheduledEffectType.EXTERNAL_UNKNOWN;
    }

    private SchedulerExecutionEntity newExecution(SchedulerJobEntity job,
                                                  ScheduledJobDescriptor descriptor,
                                                  Instant now) {
        var scheduledAt = job.getNextFireAt() == null ? now : job.getNextFireAt();
        return newExecution(job, descriptor, now, scheduledAt);
    }

    private SchedulerExecutionEntity newExecution(SchedulerJobEntity job,
                                                  ScheduledJobDescriptor descriptor,
                                                  Instant now,
                                                  Instant scheduledAt) {
        var execution = new SchedulerExecutionEntity();
        var id = UUID.randomUUID();
        execution.setId(id);
        execution.setJobId(job.getId());
        execution.setFireKey(idempotencyService.fireKey(job, scheduledAt));
        execution.setTriggerType(ScheduledTriggerType.SCHEDULE);
        execution.setStatus(SchedulerExecutionStatus.QUEUED);
        execution.setJobRevision(job.getRevision());
        execution.setHandlerVersion(handlerVersion(descriptor));
        execution.setScheduleKindSnapshot(job.getScheduleKind());
        execution.setScheduleExpressionSnapshot(scheduleExpression(job));
        execution.setParametersSnapshot(job.getParameters() == null ? Map.of() : job.getParameters());
        execution.setEffectType(descriptor.effectType());
        execution.setScheduledAt(scheduledAt);
        execution.setQueuedAt(now);
        execution.setDeadlineAt(now.plus(timeout(descriptor, job)));
        execution.setAttemptNo(1);
        execution.setMaxAttempts(maxAttempts(descriptor, job));
        execution.setResultSummary(Map.of());
        execution.setResolutionStatus(SchedulerResolutionStatus.UNRESOLVED);
        execution.setCreatedAt(now);
        execution.setUpdatedAt(now);
        execution.setVersion(0L);
        return execution;
    }

    private static void validateParameters(ScheduledJobDescriptor descriptor, Map<String, Object> parameters) {
        var actual = parameters == null ? Map.<String, Object>of() : parameters;
        var schema = descriptor.parameterSchema();
        if (actual.keySet().stream().anyMatch(key -> key == null || !schema.containsKey(key))) {
            throw new IllegalArgumentException("手工触发参数包含未注册字段");
        }
        schema.forEach((key, definition) -> {
            if (definition instanceof Map<?, ?> map
                    && Boolean.TRUE.equals(map.get("required"))
                    && !actual.containsKey(key)) {
                throw new IllegalArgumentException("缺少必填任务参数: " + key);
            }
        });
    }

    private static Map<String, Object> safeParameters(Map<String, Object> parameters) {
        var copy = new LinkedHashMap<String, Object>();
        parameters.forEach((key, value) -> {
            if (key == null || key.isBlank() || key.length() > 100) {
                throw new IllegalArgumentException("任务参数名称不合法");
            }
            if (value == null
                    || value instanceof String
                    || value instanceof Number
                    || value instanceof Boolean
                    || value instanceof Map<?, ?>
                    || value instanceof List<?>) {
                copy.put(key, value);
            } else {
                throw new IllegalArgumentException("任务参数值类型不受支持: " + key);
            }
        });
        return Collections.unmodifiableMap(copy);
    }

    private static String handlerVersion(ScheduledJobDescriptor descriptor) {
        Object value = descriptor.executionPolicy().get("handlerVersion");
        return value == null || String.valueOf(value).isBlank() ? HANDLER_VERSION : String.valueOf(value).trim();
    }

    private static Duration timeout(ScheduledJobDescriptor descriptor, SchedulerJobEntity job) {
        return durationPolicy(descriptor, job, "timeoutMs", DEFAULT_TIMEOUT);
    }

    private static Duration leaseDuration(SchedulerExecutionEntity execution, Instant now) {
        if (execution.getDeadlineAt() == null) {
            return DEFAULT_TIMEOUT;
        }
        var remaining = Duration.between(now, execution.getDeadlineAt());
        return !remaining.isZero() && !remaining.isNegative() ? remaining : Duration.ofSeconds(30);
    }

    private static int maxAttempts(ScheduledJobDescriptor descriptor, SchedulerJobEntity job) {
        var value = policyValue(descriptor, job, "maxAttempts");
        if (value == null) {
            return 1;
        }
        int attempts = positiveInt(value, "maxAttempts");
        if (attempts <= 0) {
            throw new IllegalStateException("maxAttempts 必须大于 0");
        }
        return attempts;
    }

    private static Duration durationPolicy(ScheduledJobDescriptor descriptor,
                                           SchedulerJobEntity job,
                                           String key,
                                           Duration fallback) {
        var value = policyValue(descriptor, job, key);
        if (value == null) {
            return fallback;
        }
        long millis = positiveLong(value, key);
        if (millis <= 0) {
            throw new IllegalStateException(key + " 必须大于 0");
        }
        return Duration.ofMillis(millis);
    }

    private static Object policyValue(ScheduledJobDescriptor descriptor, SchedulerJobEntity job, String key) {
        Object value = descriptor.executionPolicy().get(key);
        if (value != null) {
            return value;
        }
        return job.getExecutionPolicy() == null ? null : job.getExecutionPolicy().get(key);
    }

    private static int positiveInt(Object value, String key) {
        try {
            return new BigDecimal(String.valueOf(value)).intValueExact();
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new IllegalStateException(key + " 必须是正整数", exception);
        }
    }

    private static long positiveLong(Object value, String key) {
        try {
            return new BigDecimal(String.valueOf(value)).longValueExact();
        } catch (NumberFormatException | ArithmeticException exception) {
            throw new IllegalStateException(key + " 必须是正整数毫秒数", exception);
        }
    }

    private static String scheduleExpression(SchedulerJobEntity job) {
        return job.getScheduleKind() == ScheduledScheduleKind.CRON
                ? job.getCronExpression()
                : job.getFixedDelayMs() == null ? null : String.valueOf(job.getFixedDelayMs());
    }

    private static Instant nextFireAt(SchedulerJobEntity job, Instant scheduledAt, Instant now, ZoneId zone) {
        Instant next = switch (job.getScheduleKind()) {
            case CRON -> {
                var cron = CronExpression.parse(job.getCronExpression());
                var current = ZonedDateTime.ofInstant(scheduledAt, zone);
                var nextCron = cron.next(current);
                yield nextCron == null ? null : nextCron.toInstant();
            }
            case FIXED_DELAY -> scheduledAt.plusMillis(job.getFixedDelayMs());
            case MANUAL -> null;
        };
        if (next == null || next.isAfter(now)) {
            return next;
        }
        return switch (job.getScheduleKind()) {
            case CRON -> {
                var nextCron = CronExpression.parse(job.getCronExpression())
                        .next(ZonedDateTime.ofInstant(now, zone));
                yield nextCron == null ? null : nextCron.toInstant();
            }
            case FIXED_DELAY -> now.plusMillis(job.getFixedDelayMs());
            case MANUAL -> null;
        };
    }

    private static String safeErrorCode(String errorCode) {
        if (errorCode == null || !ERROR_CODE_PATTERN.matcher(errorCode.trim()).matches()) {
            return errorCode == null ? null : "SCHEDULER_HANDLER_ERROR";
        }
        return errorCode.trim();
    }

    private static String safeMessage(String message) {
        if (message == null || message.isBlank()) {
            return null;
        }
        var value = message.replaceAll("[\\r\\n\\t]+", " ").trim();
        return value.length() <= 500 ? value : value.substring(0, 500);
    }

    private static Map<String, Object> safeSummary(Map<String, Object> summary) {
        if (summary == null || summary.isEmpty()) {
            return Map.of();
        }
        var sanitized = new LinkedHashMap<String, Object>();
        summary.entrySet().stream().limit(20).forEach(entry -> {
            var key = entry.getKey();
            if (key != null && key.matches("[A-Za-z0-9_.:-]{1,100}")) {
                Object value = entry.getValue();
                if (value == null || value instanceof Number || value instanceof Boolean) {
                    sanitized.put(key, value);
                } else {
                    sanitized.put(key, safeMessage(String.valueOf(value)));
                }
            }
        });
        return Map.copyOf(sanitized);
    }
}
