/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.system.outbox;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.core.system.service.OperationLogService;
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
import static org.mockito.ArgumentCaptor.forClass;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OperationLogOutboxWorkerTest {

    private static final Instant NOW = Instant.parse("2026-08-31T04:05:06Z");
    private static final UUID EVENT_ID = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c1234");
    private static final UUID RUNTIME_ID = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c9999");

    private final SimpleMeterRegistry meterRegistry = new SimpleMeterRegistry();

    @AfterEach
    void closeMeterRegistry() {
        meterRegistry.close();
    }

    @Test
    void commitsSysLogAndOutboxConfirmationInTheSameEventTransaction() throws Exception {
        var repository = mock(OperationLogOutboxRepository.class);
        var operationLogService = mock(OperationLogService.class);
        var transactionTemplate = transactionTemplate();
        var record = record(EVENT_ID);
        var event = event(record, 1);
        doAnswer(invocation -> {
            assertEquals("correlation-456", RequestCorrelationContext.current().correlationId());
            assertEquals("request-123", RequestCorrelationContext.current().requestId());
            return null;
        }).when(operationLogService).persist(any(AuditRecord.class));
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(30), 100, 10)).thenReturn(List.of(event));
        when(repository.markProcessed(EVENT_ID, "worker-a", NOW)).thenReturn(1);
        when(repository.pendingCount()).thenReturn(0L);

        var worker = worker(repository, operationLogService, transactionTemplate, "worker-a");
        var result = worker.processBatch();

        assertEquals(new OperationLogOutboxWorker.BatchResult(1, 1, 0, 0, 0), result);
        var recordCaptor = forClass(AuditRecord.class);
        verify(operationLogService).persist(recordCaptor.capture());
        assertEquals(EVENT_ID, recordCaptor.getValue().eventId());
        verify(repository).markProcessed(EVENT_ID, "worker-a", NOW);
        assertEquals(1.0, meterRegistry.get("operation_log_outbox_processed_total").counter().count());
        assertEquals(0.0, meterRegistry.get("operation_log_outbox_pending").gauge().value());
    }

    @Test
    void keepsEventForRetryWhenSysLogTargetIsTemporarilyUnavailable() throws Exception {
        var repository = mock(OperationLogOutboxRepository.class);
        var operationLogService = mock(OperationLogService.class);
        var record = record(EVENT_ID);
        var event = event(record, 1);
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(30), 100, 10)).thenReturn(List.of(event));
        when(repository.markRetry(EVENT_ID, "worker-a", NOW, NOW.plusSeconds(1),
                "AuditRecordingException: sys_log unavailable")).thenReturn(1);
        when(repository.pendingCount()).thenReturn(1L);
        doThrow(new AuditService.AuditRecordingException("sys_log unavailable"))
                .when(operationLogService)
                .persist(any(AuditRecord.class));

        var worker = worker(repository, operationLogService, transactionTemplate(), "worker-a");
        var result = worker.processBatch();

        assertEquals(new OperationLogOutboxWorker.BatchResult(1, 0, 1, 0, 1), result);
        verify(repository).markRetry(EVENT_ID, "worker-a", NOW, NOW.plusSeconds(1),
                "AuditRecordingException: sys_log unavailable");
        assertEquals(1.0, meterRegistry.get("operation_log_outbox_failed_total").counter().count());
        assertEquals(0.0, meterRegistry.get("operation_log_outbox_dead_letter_total").counter().count());
    }

    @Test
    void movesPermanentFailureToDeadLetterWithoutDeletingPayload() throws Exception {
        var repository = mock(OperationLogOutboxRepository.class);
        var operationLogService = mock(OperationLogService.class);
        var record = record(EVENT_ID);
        var event = event(record, 10);
        when(repository.claimBatch("worker-a", NOW, NOW.plusSeconds(30), 100, 10)).thenReturn(List.of(event));
        when(repository.markDeadLetter(EVENT_ID, "worker-a", NOW,
                "AuditRecordingException: permanent failure")).thenReturn(1);
        when(repository.pendingCount()).thenReturn(1L);
        doThrow(new AuditService.AuditRecordingException("permanent failure"))
                .when(operationLogService)
                .persist(any(AuditRecord.class));

        var worker = worker(repository, operationLogService, transactionTemplate(), "worker-a");
        var result = worker.processBatch();

        assertEquals(new OperationLogOutboxWorker.BatchResult(1, 0, 1, 1, 1), result);
        verify(repository).markDeadLetter(EVENT_ID, "worker-a", NOW,
                "AuditRecordingException: permanent failure");
        assertEquals(1.0, meterRegistry.get("operation_log_outbox_dead_letter_total").counter().count());
    }

    @Test
    void schedulerCycleExposesFailureAndBacklogWithoutLeakingPayload() throws Exception {
        var repository = mock(OperationLogOutboxRepository.class);
        var operationLogService = mock(OperationLogService.class);
        var record = record(EVENT_ID);
        var event = event(record, 1);
        when(repository.claimBatch("instance-a:" + RUNTIME_ID, NOW, NOW.plusSeconds(30), 100, 10))
                .thenReturn(List.of(event));
        when(repository.markRetry(eq(EVENT_ID), eq("instance-a:" + RUNTIME_ID), eq(NOW),
                eq(NOW.plusSeconds(1)), any(String.class))).thenReturn(1);
        when(repository.pendingCount()).thenReturn(1L);
        doThrow(new IllegalStateException("database unavailable"))
                .when(operationLogService)
                .persist(record);

        var worker = worker(repository, operationLogService, transactionTemplate(), "unused");
        var cycle = worker.runCycle(com.devops00.spectra.common.scheduler.ScheduledLoopContext.builder()
                .runtimeId(RUNTIME_ID)
                .jobKey("system.operation-log.outbox")
                .handlerKey("system.operation-log.outbox")
                .sessionKey("session-a")
                .instanceId("instance-a")
                .startedAt(NOW)
                .build());

        assertEquals(0, cycle.processed());
        assertEquals(1, cycle.failed());
        assertEquals("OPERATION_LOG_OUTBOX_FAILURE", cycle.errorCode());
        assertEquals(1, cycle.context().get("claimed"));
        assertEquals(1L, cycle.context().get("pending"));
        assertEquals(0, cycle.context().get("deadLetter"));
    }

    private OperationLogOutboxWorker worker(OperationLogOutboxRepository repository,
                                            OperationLogService operationLogService,
                                            TransactionTemplate transactionTemplate,
                                            String owner) {
        return new OperationLogOutboxWorker(repository, new ObjectMapper(), operationLogService,
                transactionTemplate, meterRegistry, Clock.fixed(NOW, ZoneOffset.UTC), owner);
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

    private static OperationLogOutboxRepository.OperationLogOutboxEvent event(AuditRecord record, int attempts)
            throws Exception {
        return new OperationLogOutboxRepository.OperationLogOutboxEvent(
                record.eventId(), record.eventId().toString(), new ObjectMapper().writeValueAsString(record), attempts);
    }

    private static AuditRecord record(UUID eventId) {
        return new AuditRecord(eventId, AuditCategory.OPERATION, "USER.UPDATE", null,
                AuditRecord.Result.SUCCEEDED, NOW,
                new AuditContext(null, "request-123", "correlation-456", "WEB", "127.0.0.1", "agent"),
                Map.of("method", "PATCH"), Map.of("status", 200, "durationMs", 12L), "updated");
    }
}
