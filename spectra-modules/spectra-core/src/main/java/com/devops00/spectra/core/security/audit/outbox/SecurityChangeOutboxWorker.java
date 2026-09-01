/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.outbox;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.scheduler.ScheduledEffectType;
import com.devops00.spectra.common.scheduler.ScheduledJobDescriptor;
import com.devops00.spectra.common.scheduler.ScheduledJobType;
import com.devops00.spectra.common.scheduler.ScheduledLoopContext;
import com.devops00.spectra.common.scheduler.ScheduledLoopCycleResult;
import com.devops00.spectra.common.scheduler.ScheduledLoopHandler;
import com.devops00.spectra.common.scheduler.ScheduledRunScope;
import com.devops00.spectra.common.scheduler.ScheduledScheduleKind;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.Gauge;
import io.micrometer.core.instrument.MeterRegistry;
import jakarta.annotation.PreDestroy;
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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;

/** 安全变更 outbox 的租约消费与下游动作分发器。 */
@Slf4j
@Component
public class SecurityChangeOutboxWorker implements ScheduledLoopHandler {

    private static final int BATCH_SIZE = 100;
    private static final int MAX_ATTEMPTS = 10;
    private static final Duration LEASE_DURATION = Duration.ofSeconds(30);
    private static final Duration LEASE_RENEWAL_INTERVAL = Duration.ofSeconds(10);
    private static final long MAX_BACKOFF_SECONDS = 300;

    private static final ScheduledJobDescriptor DESCRIPTOR = ScheduledJobDescriptor.builder()
            .jobKey("security.security-change.outbox")
            .handlerKey("security.security-change.outbox")
            .name("安全变更 Outbox")
            .module("security")
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
                    "batchSize", BATCH_SIZE,
                    "maxAttempts", MAX_ATTEMPTS))
            .build();

    private final SecurityChangeOutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final List<SecurityChangeOutboxHandler> handlers;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final String defaultOwner;
    private final AtomicLong pendingGauge = new AtomicLong();
    private final AtomicLong oldestPendingAgeSeconds = new AtomicLong();
    private final Counter processedCounter;
    private final Counter failedCounter;
    private final Counter deadLetterCounter;
    private final ScheduledExecutorService leaseRenewalExecutor = Executors.newSingleThreadScheduledExecutor(runnable -> {
        Thread thread = new Thread(runnable, "security-change-outbox-lease-renewer");
        thread.setDaemon(true);
        return thread;
    });

    @Autowired
    public SecurityChangeOutboxWorker(SecurityChangeOutboxRepository repository,
                                      ObjectMapper objectMapper,
                                      List<SecurityChangeOutboxHandler> handlers,
                                      PlatformTransactionManager transactionManager,
                                      MeterRegistry meterRegistry,
                                      Clock clock) {
        this(repository, objectMapper, handlers, new TransactionTemplate(transactionManager), meterRegistry, clock,
                "security-change-outbox");
    }

    SecurityChangeOutboxWorker(SecurityChangeOutboxRepository repository,
                               ObjectMapper objectMapper,
                               List<SecurityChangeOutboxHandler> handlers,
                               TransactionTemplate transactionTemplate,
                               MeterRegistry meterRegistry,
                               Clock clock,
                               String defaultOwner) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.handlers = handlers == null ? List.of() : List.copyOf(handlers);
        this.transactionTemplate = transactionTemplate;
        this.clock = clock;
        this.defaultOwner = defaultOwner;
        Gauge.builder("security_change_outbox_pending", pendingGauge, AtomicLong::doubleValue)
                .description("尚未完成安全变更动作分发的 outbox 数量")
                .register(meterRegistry);
        Gauge.builder("security_change_outbox_oldest_pending_age_seconds", oldestPendingAgeSeconds, AtomicLong::doubleValue)
                .description("安全变更 outbox 最老待处理事件年龄")
                .register(meterRegistry);
        this.processedCounter = Counter.builder("security_change_outbox_processed_total")
                .description("成功分发的安全变更 outbox 事件数")
                .register(meterRegistry);
        this.failedCounter = Counter.builder("security_change_outbox_failed_total")
                .description("安全变更 outbox 处理失败次数")
                .register(meterRegistry);
        this.deadLetterCounter = Counter.builder("security_change_outbox_dead_letter_total")
                .description("进入人工重放的安全变更 outbox 事件数")
                .register(meterRegistry);
    }

    @Override
    public ScheduledJobDescriptor descriptor() {
        return DESCRIPTOR;
    }

    @PreDestroy
    void shutdownLeaseRenewal() {
        leaseRenewalExecutor.shutdownNow();
    }

    /** 直接执行一批消费，供测试和运维调用。 */
    public BatchResult processBatch() {
        return processBatch(defaultOwner, BATCH_SIZE);
    }

    @Override
    public ScheduledLoopCycleResult runCycle(ScheduledLoopContext context) {
        BatchResult result = processBatch(owner(context), BATCH_SIZE);
        boolean failed = result.failed() > 0;
        return ScheduledLoopCycleResult.builder()
                .processed(result.processed())
                .failed(result.failed())
                .errorCode(failed ? "SECURITY_CHANGE_OUTBOX_FAILURE" : null)
                .sanitizedMessage(failed ? "安全变更 outbox 存在处理失败事件" : null)
                .context(Map.of(
                        "claimed", result.claimed(),
                        "pending", result.pending(),
                        "deadLetter", result.deadLetter()))
                .build();
    }

    BatchResult processBatch(String owner, int batchSize) {
        if (owner == null || owner.isBlank()) {
            throw new IllegalArgumentException("安全变更 outbox worker owner 不能为空");
        }
        if (batchSize <= 0) {
            throw new IllegalArgumentException("安全变更 outbox batchSize 必须大于 0");
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
        Instant oldest = repository.oldestPendingAt();
        oldestPendingAgeSeconds.set(oldest == null ? 0L : Math.max(0L, Duration.between(oldest, clock.instant()).toSeconds()));
        return new BatchResult(claimed, processed, failed, deadLetter, pending);
    }

    private void processOne(SecurityChangeOutboxRepository.SecurityChangeOutboxEvent event, String owner) {
        try (var ignored = RequestCorrelationContext.openTask(
                event.correlationId() == null ? event.eventId().toString() : event.correlationId())) {
            ScheduledFuture<?> renewal = leaseRenewalExecutor.scheduleAtFixedRate(
                    () -> renewLeaseWithContext(event, owner),
                    LEASE_RENEWAL_INTERVAL.toMillis(),
                    LEASE_RENEWAL_INTERVAL.toMillis(),
                    java.util.concurrent.TimeUnit.MILLISECONDS);
            try {
                SecurityAuditEvent auditEvent;
                try {
                    auditEvent = objectMapper.readValue(event.payload(), SecurityAuditEvent.class);
                } catch (RuntimeException exception) {
                    throw new OutboxPayloadException("安全变更 outbox payload 无法解析", exception);
                }
                if (!event.eventId().equals(auditEvent.eventId())
                        || !event.eventType().equals(auditEvent.eventType())
                        || (event.correlationId() != null && !event.correlationId().equals(auditEvent.correlationId()))) {
                    throw new OutboxPayloadException("安全变更 outbox 元数据与 payload 不一致");
                }
                transactionTemplate.executeWithoutResult(status -> {
                    var matchingHandlers = handlers.stream()
                            .filter(handler -> handler.supports(event.eventType()))
                            .toList();
                    // Core 在没有可选下游模块时只承担持久化交接职责；一旦已经注册了
                    // handler 却没有消费者接收该事件，必须失败并进入重试/死信，不能静默确认。
                    if (!handlers.isEmpty() && matchingHandlers.isEmpty()) {
                        throw new OutboxPayloadException("安全变更 outbox 没有匹配的下游处理器");
                    }
                    matchingHandlers.forEach(handler -> handler.handle(event, auditEvent));
                    if (repository.markProcessed(event.eventId(), owner, clock.instant()) != 1) {
                        throw new DataAccessException("安全变更 outbox 租约已失效，未确认成功") {
                        };
                    }
                });
                log.debug("安全变更 outbox 已分发: eventId={}, correlationId={}, eventType={}",
                        event.eventId(), event.correlationId(), event.eventType());
            } finally {
                renewal.cancel(false);
            }
        }
    }

    private void renewLeaseWithContext(SecurityChangeOutboxRepository.SecurityChangeOutboxEvent event, String owner) {
        try (var ignored = RequestCorrelationContext.openTask(
                event.correlationId() == null ? event.eventId().toString() : event.correlationId())) {
            renewLease(event, owner);
        }
    }

    private void renewLease(SecurityChangeOutboxRepository.SecurityChangeOutboxEvent event, String owner) {
        try {
            Instant now = clock.instant();
            if (repository.renewLease(event.eventId(), owner, now.plus(LEASE_DURATION), now) != 1) {
                log.warn("安全变更 outbox 租约续期未生效: eventId={}", event.eventId());
            }
        } catch (RuntimeException exception) {
            log.warn("安全变更 outbox 租约续期失败: eventId={}, error={}", event.eventId(),
                    errorMessage(exception));
        }
    }

    private boolean recordFailure(SecurityChangeOutboxRepository.SecurityChangeOutboxEvent event,
                                  String owner,
                                  RuntimeException exception) {
        Instant now = clock.instant();
        String error = errorMessage(exception);
        try {
            if (event.attempts() >= MAX_ATTEMPTS) {
                return transactionTemplate.execute(status -> repository.markDeadLetter(event.eventId(), owner, now, error)) == 1;
            }
            Instant nextAvailableAt = now.plusSeconds(backoffSeconds(event.attempts()));
            transactionTemplate.execute(status -> repository.markRetry(
                    event.eventId(), owner, now, nextAvailableAt, error));
        } catch (RuntimeException markException) {
            log.error("安全变更 outbox 失败状态无法落库: eventId={}", event.eventId(), markException);
        }
        return false;
    }

    /** 人工重放死信，不绕过原有权限控制。 */
    public boolean replay(UUID eventId, UUID operatorId) {
        return transactionTemplate.execute(status -> repository.replay(eventId, operatorId, clock.instant())) == 1;
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
            return "security-change-outbox";
        }
        return context.instanceId() + ":" + context.runtimeId();
    }

    /** 一批安全变更 outbox 的处理统计。 */
    public record BatchResult(int claimed, int processed, int failed, int deadLetter, long pending) {

        public BatchResult {
            if (claimed < 0 || processed < 0 || failed < 0 || deadLetter < 0 || pending < 0) {
                throw new IllegalArgumentException("安全变更 outbox 统计不能为负数");
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
