/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.outbox;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityChangeOutboxWorkerTest {

    private static final Instant NOW = Instant.parse("2026-08-31T04:05:06Z");
    private static final UUID EVENT_ID = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c1234");
    private static final UUID TARGET_ID = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c5678");

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @AfterEach
    void closeMeterRegistry() {
        meterRegistry.close();
    }

    @Test
    void dispatchesEventAndConfirmsOnlyAfterHandlerSucceeds() throws Exception {
        var repository = mock(SecurityChangeOutboxRepository.class);
        var handler = mock(SecurityChangeOutboxHandler.class);
        when(handler.supports("USER_STATUS_CHANGED")).thenReturn(true);
        doAnswer(invocation -> {
            assertEquals("corr-123", RequestCorrelationContext.current().correlationId());
            assertEquals(null, RequestCorrelationContext.current().requestId());
            return null;
        }).when(handler).handle(any(), any());
        var auditEvent = event();
        var outboxEvent = outboxEvent(auditEvent, 1);
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(30), 100, 10)).thenReturn(List.of(outboxEvent));
        when(repository.markProcessed(EVENT_ID, "worker-a", NOW)).thenReturn(1);
        when(repository.pendingCount()).thenReturn(0L);

        var worker = worker(repository, List.of(handler), "worker-a");
        var result = worker.processBatch();

        assertEquals(new SecurityChangeOutboxWorker.BatchResult(1, 1, 0, 0, 0), result);
        verify(handler).handle(eq(outboxEvent), eq(auditEvent));
        verify(repository).markProcessed(EVENT_ID, "worker-a", NOW);
    }

    @Test
    void retainsEventForRetryWhenDownstreamHandlerFails() throws Exception {
        var repository = mock(SecurityChangeOutboxRepository.class);
        var handler = mock(SecurityChangeOutboxHandler.class);
        when(handler.supports("USER_STATUS_CHANGED")).thenReturn(true);
        doThrow(new IllegalStateException("downstream unavailable"))
                .when(handler)
                .handle(any(), any());
        var outboxEvent = outboxEvent(event(), 1);
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(30), 100, 10)).thenReturn(List.of(outboxEvent));
        when(repository.markRetry(EVENT_ID, "worker-a", NOW, NOW.plusSeconds(1),
                "IllegalStateException: downstream unavailable")).thenReturn(1);
        when(repository.pendingCount()).thenReturn(1L);

        var worker = worker(repository, List.of(handler), "worker-a");
        var result = worker.processBatch();

        assertEquals(new SecurityChangeOutboxWorker.BatchResult(1, 0, 1, 0, 1), result);
        verify(repository).markRetry(EVENT_ID, "worker-a", NOW, NOW.plusSeconds(1),
                "IllegalStateException: downstream unavailable");
        verify(repository, never()).markProcessed(any(), any(), any());
    }

    @Test
    void acceptsDurableHandoffWhenNoOptionalConsumerIsInstalled() throws Exception {
        var repository = mock(SecurityChangeOutboxRepository.class);
        var outboxEvent = outboxEvent(event(), 1);
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(30), 100, 10)).thenReturn(List.of(outboxEvent));
        when(repository.markProcessed(EVENT_ID, "worker-a", NOW)).thenReturn(1);
        when(repository.pendingCount()).thenReturn(0L);

        var worker = worker(repository, List.of(), "worker-a");

        assertEquals(1, worker.processBatch().processed());
        verify(repository).markProcessed(EVENT_ID, "worker-a", NOW);
    }

    @Test
    void unsupportedEventWithRegisteredConsumersIsRetried() throws Exception {
        var repository = mock(SecurityChangeOutboxRepository.class);
        var handler = mock(SecurityChangeOutboxHandler.class);
        when(handler.supports("USER_STATUS_CHANGED")).thenReturn(false);
        var outboxEvent = outboxEvent(event(), 1);
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(30), 100, 10)).thenReturn(List.of(outboxEvent));
        when(repository.markRetry(EVENT_ID, "worker-a", NOW, NOW.plusSeconds(1),
                "OutboxPayloadException: 安全变更 outbox 没有匹配的下游处理器")).thenReturn(1);
        when(repository.pendingCount()).thenReturn(1L);

        var worker = worker(repository, List.of(handler), "worker-a");

        assertEquals(1, worker.processBatch().failed());
        verify(repository).markRetry(EVENT_ID, "worker-a", NOW, NOW.plusSeconds(1),
                "OutboxPayloadException: 安全变更 outbox 没有匹配的下游处理器");
    }

    private SecurityChangeOutboxWorker worker(SecurityChangeOutboxRepository repository,
                                              List<SecurityChangeOutboxHandler> handlers,
                                              String owner) {
        return new SecurityChangeOutboxWorker(repository, new ObjectMapper(), handlers,
                transactionTemplate(), meterRegistry, Clock.fixed(NOW, ZoneOffset.UTC), owner);
    }

    private static TransactionTemplate transactionTemplate() {
        TransactionTemplate template = mock(TransactionTemplate.class);
        when(template.execute(any(TransactionCallback.class))).thenAnswer(invocation -> {
            TransactionCallback<?> callback = invocation.getArgument(0);
            return callback.doInTransaction(new SimpleTransactionStatus());
        });
        doAnswer(invocation -> {
            Consumer<?> action = invocation.getArgument(0);
            @SuppressWarnings("unchecked")
            Consumer<org.springframework.transaction.TransactionStatus> transactionAction = (Consumer<org.springframework.transaction.TransactionStatus>) action;
            transactionAction.accept(new SimpleTransactionStatus());
            return null;
        }).when(template).executeWithoutResult(any());
        return template;
    }

    private static SecurityChangeOutboxRepository.SecurityChangeOutboxEvent outboxEvent(
                                                                                        SecurityAuditEvent event, int attempts)
            throws Exception {
        return new SecurityChangeOutboxRepository.SecurityChangeOutboxEvent(
                event.eventId(), "idempotency", event.eventType(), "USER", event.targetId(),
                new ObjectMapper().writeValueAsString(event), event.correlationId(), attempts);
    }

    private static SecurityAuditEvent event() {
        return new SecurityAuditEvent(EVENT_ID, "USER_STATUS_CHANGED", UUID.randomUUID(), TARGET_ID, "WEB",
                "127.0.0.1", "agent", Map.of("status", "ACTIVE"), Map.of("status", "DISABLED"),
                "test", NOW, AuditResult.SUCCEEDED, "corr-123");
    }
}
