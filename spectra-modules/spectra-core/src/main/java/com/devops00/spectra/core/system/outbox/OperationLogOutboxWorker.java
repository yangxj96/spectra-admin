/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.system.outbox;

import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledLoopContext;
import com.devops00.spectra.common.scheduler.ScheduledLoopCycleResult;
import com.devops00.spectra.common.scheduler.ScheduledLoopHandler;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.core.system.service.OperationLogService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 普通操作日志 outbox worker。
 *
 * <p>批量领取和状态推进使用数据库事务；每个事件的 {@code sys_log} 写入和成功确认使用
 * 独立事务，避免一个坏事件回滚整批。当前 worker 由统一单体调度内核作为 SINGLETON/LOOP
 * 处理器驱动。</p>
 */
@Slf4j
@Component
public class OperationLogOutboxWorker implements ScheduledLoopHandler {

    private static final int DEFAULT_BATCH_SIZE = 100;
    private static final int MAX_ATTEMPTS = 10;
    private static final Duration LEASE_DURATION = Duration.ofSeconds(30);
    private static final long MAX_BACKOFF_SECONDS = 300;

    private static final ScheduledJobDescriptor DESCRIPTOR = ScheduledJobDescriptor.builder()
            .jobKey("system.operation-log.outbox")
            .handlerKey("system.operation-log.outbox")
            .name("普通操作日志 Outbox")
            .module("system")
            .jobType(ScheduledJobType.LOOP)
            .runScope(ScheduledRunScope.SINGLETON)
            .scheduleKind(ScheduledScheduleKind.FIXED_DELAY)
            .effectType(ScheduledEffectType.DB_ONLY)
            .parameterSchema(Map.of())
            .supportedActions(Set.of("VIEW", "START", "DRAIN_STOP"))
            .executionPolicy(Map.of(
                    "heartbeatIntervalMs", 3000L,
                    "leaseDurationMs", LEASE_DURATION.toMillis(),
                    "errorLogIntervalMs", 60000L,
                    "batchSize", DEFAULT_BATCH_SIZE,
                    "maxAttempts", MAX_ATTEMPTS))
            .build();

    private final OperationLogOutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final OperationLogService operationLogService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final String defaultOwner;
    private final AtomicLong pendingGauge = new AtomicLong();
    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Counter deadLetterCounter;

    @Autowired
    public OperationLogOutboxWorker(OperationLogOutboxRepository repository,
                                    ObjectMapper objectMapper,
                                    OperationLogService operationLogService,
                                    PlatformTransactionManager transactionManager,
                                    MeterRegistry meterRegistry,
                                    Clock clock) {
        this(repository, objectMapper, operationLogService, new TransactionTemplate(transactionManager),
                meterRegistry, clock, "operation-log-outbox");
    }

    OperationLogOutboxWorker(OperationLogOutboxRepository repository,
                             ObjectMapper objectMapper,
                             OperationLogService operationLogService,
                             TransactionTemplate transactionTemplate,
                             MeterRegistry meterRegistry,
                             Clock clock,
                             String defaultOwner) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.operationLogService = operationLogService;
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
        this.defaultOwner = defaultOwner;
        Gauge.builder("operation_log_outbox_pending", pendingGauge, AtomicLong::doubleValue)
                .description("尚未成功处理的普通操作日志 outbox 数量")
                .register(meterRegistry);
        this.processedCounter = Counter.builder("operation_log_outbox_processed_total")
                .description("成功写入 sys_log 的普通操作日志 outbox 事件数")
                .register(meterRegistry);
        this.failedCounter = Counter.builder("operation_log_outbox_failed_total")
                .description("普通操作日志 outbox 处理失败次数")
                .register(meterRegistry);
        this.deadLetterCounter = Counter.builder("operation_log_outbox_dead_letter_total")
                .description("进入人工处置的普通操作日志 outbox 事件数")
                .register(meterRegistry);
    }

    @Override
    public ScheduledJobDescriptor descriptor() {
        return DESCRIPTOR;
    }

    /**
     * 直接执行一批消费，供测试和运维调用；生产调度使用 {@link #runCycle(ScheduledLoopContext)}。
     */
    public BatchResult processBatch() {
        return processBatch(defaultOwner, DEFAULT_BATCH_SIZE);
    }

    @Override
    public ScheduledLoopCycleResult runCycle(ScheduledLoopContext context) {
        String owner = owner(context);
        BatchResult result = processBatch(owner, DEFAULT_BATCH_SIZE);
        boolean failed = result.failed() > 0;
        return ScheduledLoopCycleResult.builder()
                .processed(result.processed())
                .failed(result.failed())
                .errorCode(failed ? "OPERATION_LOG_OUTBOX_FAILURE" : null)
                .sanitizedMessage(failed ? "普通操作日志 outbox 存在处理失败事件" : null)
                .context(Map.of(
                        "claimed", result.claimed(),
                        "pending", result.pending(),
                        "deadLetter", result.deadLetter()))
                .build();
    }

    BatchResult processBatch(String owner, int batchSize) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("outbox worker owner 不能为空");
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("outbox worker batchSize 必须大于 0");
        }

        Instant now = clock.instant();
        var events = transactionTemplate.execute(status -> repository.claimBatch(
                owner, now, now.plus(LEASE_DURATION), batchSize, MAX_ATTEMPTS));
        int claimed = events == null ? 0 : events.size();
        int processed = 0;
        int failed = 0;
        int deadLetter = 0;
        if (events != null) {
            for (var event : events) {
                try {
                    processOne(event, owner);
                    processed++;
                    processedCounter.increment();
                } catch (RuntimeException exception) {
                    failed++;
                    failedCounter.increment();
                    if (recordFailure(event, owner, exception)) {
                        deadLetter++;
                        deadLetterCounter.increment();
                    }
                }
            }
        }
        long pending = repository.pendingCount();
        pendingGauge.set(pending);
        return new BatchResult(claimed, processed, failed, deadLetter, pending);
    }

    private void processOne(OperationLogOutboxRepository.OperationLogOutboxEvent event, String owner) {
        AuditRecord record;
        try {
            record = objectMapper.readValue(event.payload(), AuditRecord.class);
        } catch (RuntimeException exception) {
            throw new OutboxPayloadException("普通操作日志 outbox payload 无法解析", exception);
        }
        if (!event.eventId().equals(record.eventId())) {
            throw new OutboxPayloadException("普通操作日志 outbox event_id 与 payload 不一致");
        }
        transactionTemplate.executeWithoutResult(status -> {
            operationLogService.persist(record);
            if (repository.markProcessed(event.eventId(), owner, clock.instant()) != 1) {
                throw new DataAccessException("普通操作日志 outbox 租约已失效，未确认成功") {
                };
            }
        });
    }

    /**
     * 失败事件只做状态推进，不删除 payload；达到上限后转为明确的 DEAD_LETTER。
     */
    private boolean recordFailure(OperationLogOutboxRepository.OperationLogOutboxEvent event,
                                  String owner,
                                  RuntimeException exception) {
        Instant now = clock.instant();
        String error = errorMessage(exception);
        try {
            if (event.attempts() >= MAX_ATTEMPTS) {
                if (transactionTemplate.execute(status -> repository.markDeadLetter(
                        event.eventId(), owner, now, error)) != 1) {
                    log.warn("普通操作日志 outbox DEAD_LETTER 状态推进失败: eventId={}", event.eventId());
                }
                return true;
            }
            Instant nextAvailableAt = now.plusSeconds(backoffSeconds(event.attempts()));
            if (transactionTemplate.execute(status -> repository.markRetry(
                    event.eventId(), owner, now, nextAvailableAt, error)) != 1) {
                log.warn("普通操作日志 outbox 重试状态推进失败: eventId={}", event.eventId());
            }
        } catch (RuntimeException markException) {
            // 状态推进失败时保留已过期租约，事件会在 lease_until 到期后重新可领取。
            log.error("普通操作日志 outbox 失败状态无法落库: eventId={}", event.eventId(), markException);
        }
        return false;
    }

    private static long backoffSeconds(int attempts) {
        int exponent = Math.max(0, Math.min(attempts - 1, 8));
        return Math.min(MAX_BACKOFF_SECONDS, 1L << exponent);
    }

    private static String errorMessage(Throwable exception) {
        String type = exception.getClass().getSimpleName();
        String message = exception.getMessage();
        if (message == null || message.isBlank()) {
            return type;
        }
        String normalized = message.replaceAll("[\\r\\n\\t]+", " ").trim();
        String combined = type + ": " + normalized;
        return combined.length() <= 2000 ? combined : combined.substring(0, 2000);
    }

    private static String owner(ScheduledLoopContext context) {
        if (context == null || context.instanceId() == null || context.runtimeId() == null) {
            return "operation-log-outbox";
        }
        return context.instanceId() + ":" + context.runtimeId();
    }

    /** 一批 outbox 的处理统计。 */
    public record BatchResult(int claimed, int processed, int failed, int deadLetter, long pending) {

        public BatchResult {
            if (claimed < 0 || processed < 0 || failed < 0 || deadLetter < 0 || pending < 0) {
                throw new IllegalArgumentException("outbox 统计不能为负数");
            }
        }
    }

    private static final class OutboxPayloadException extends RuntimeException {

        private OutboxPayloadException(String message) {
            super(message);
        }

        private OutboxPayloadException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
