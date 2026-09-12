/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.core.audit.repository.JdbcAuditEventWriter;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class CoreAuditServiceTest {

    private static final UUID EVENT_ID = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c1234");
    private static final UUID OPERATOR_ID = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c5678");
    private static final Instant OCCURRED_AT = Instant.parse("2026-08-31T04:05:06.789Z");

    @Test
    void operationAndSecurityCategoriesMustUseTheSameWriterAndSanitizedContext() {
        var writer = mock(JdbcAuditEventWriter.class);
        var service = new CoreAuditService(writer, this::sanitize);

        service.record(operationRecord());
        service.record(securityRecord(AuditRecord.Result.DENIED));

        var captor = org.mockito.ArgumentCaptor.forClass(AuditRecord.class);
        verify(writer, times(2)).append(captor.capture());
        List<AuditRecord> accepted = captor.getAllValues();
        assertEquals(List.of(AuditCategory.OPERATION, AuditCategory.SECURITY),
                accepted.stream().map(AuditRecord::category).toList());
        assertEquals(EVENT_ID, accepted.getFirst().eventId());
        assertEquals(OCCURRED_AT, accepted.getFirst().occurredAt());
        assertEquals("***", accepted.getFirst().before().get("password"));
        assertEquals("***", accepted.getFirst().after().get("token"));
        assertEquals("request-123", auditMetadata(accepted.getFirst().after()).get("requestId"));
        assertEquals(AuditRecord.Result.DENIED, accepted.get(1).result());
    }

    @Test
    void writerFailureMustPropagateAndAvailabilityMustUseTheSameWriter() {
        var writer = mock(JdbcAuditEventWriter.class);
        doThrow(new AuditService.AuditRecordingException("audit unavailable")).when(writer).append(any());
        var service = new CoreAuditService(writer, this::sanitize);

        assertThrows(AuditService.AuditRecordingException.class, () -> service.record(operationRecord()));
        service.assertAvailable();
        verify(writer).assertAvailable();
    }

    @Test
    void transactionBoundaryIsDeclaredAndWriterFailureRollsBackTheOwningTransaction()
            throws NoSuchMethodException {
        var annotation = CoreAuditService.class.getMethod("record", AuditRecord.class).getAnnotation(Transactional.class);
        assertNotNull(annotation);

        var transactionManager = new RecordingTransactionManager();
        var writer = mock(JdbcAuditEventWriter.class);
        doThrow(new AuditService.AuditRecordingException("audit unavailable")).when(writer).append(any());
        var service = new CoreAuditService(writer, this::sanitize);

        var template = new TransactionTemplate(transactionManager);
        assertThrows(AuditService.AuditRecordingException.class,
                () -> template.executeWithoutResult(status -> service.record(operationRecord())));

        assertEquals(0, transactionManager.commits);
        assertEquals(1, transactionManager.rollbacks);
    }

    private AuditRecord operationRecord() {
        return new AuditRecord(EVENT_ID, AuditCategory.OPERATION, "USER.UPDATE", null,
                AuditRecord.Result.SUCCEEDED, OCCURRED_AT, context(),
                Map.of("password", "plain"), Map.of("token", "plain"), "user updated");
    }

    private AuditRecord securityRecord(AuditRecord.Result result) {
        return new AuditRecord(EVENT_ID, AuditCategory.SECURITY, "USER.PASSWORD_RESET", null,
                result, OCCURRED_AT, context(),
                Map.of("password", "plain"), Map.of("token", "plain"), "security decision");
    }

    private AuditContext context() {
        return new AuditContext(OPERATOR_ID, "request-123", "correlation-456", "WEB", "127.0.0.1", "agent");
    }

    private Map<String, Object> sanitize(Map<String, ?> source) {
        var sanitized = new LinkedHashMap<String, Object>();
        source.forEach((key, value) -> sanitized.put(key, value));
        if (sanitized.containsKey("password")) {
            sanitized.put("password", "***");
        }
        if (sanitized.containsKey("token")) {
            sanitized.put("token", "***");
        }
        return sanitized;
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> auditMetadata(Map<String, Object> snapshot) {
        return (Map<String, Object>) snapshot.get("_audit");
    }

    private static final class RecordingTransactionManager implements PlatformTransactionManager {

        private int commits;
        private int rollbacks;

        @Override
        public TransactionStatus getTransaction(TransactionDefinition definition) {
            return new SimpleTransactionStatus();
        }

        @Override
        public void commit(TransactionStatus status) {
            commits++;
        }

        @Override
        public void rollback(TransactionStatus status) {
            rollbacks++;
        }
    }
}
