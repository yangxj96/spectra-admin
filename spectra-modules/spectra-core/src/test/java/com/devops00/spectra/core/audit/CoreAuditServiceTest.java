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

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.core.system.service.OperationLogService;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditUnavailableException;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.SimpleTransactionStatus;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Instant;
import java.util.ArrayList;
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
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

class CoreAuditServiceTest {

    private static final UUID EVENT_ID = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c1234");
    private static final UUID OPERATOR_ID = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c5678");
    private static final Instant OCCURRED_AT = Instant.parse("2026-08-31T04:05:06.789Z");

    @Test
    void operationCategoryRoutesToOperationSinkWithUnifiedContextAndSanitizedSnapshots() {
        var securitySink = new RecordingSecurityAuditWriter();
        var operationSink = mock(OperationLogService.class);
        var service = new CoreAuditService(securitySink, operationSink, this::sanitize);

        service.record(operationRecord());

        var captor = org.mockito.ArgumentCaptor.forClass(AuditRecord.class);
        verify(operationSink).record(captor.capture());
        var accepted = captor.getValue();
        assertEquals(EVENT_ID, accepted.eventId());
        assertEquals(OCCURRED_AT, accepted.occurredAt());
        assertEquals("***", accepted.before().get("password"));
        assertEquals("***", accepted.after().get("token"));
        assertEquals("request-123", accepted.context().requestId());
        assertEquals("correlation-456", accepted.context().correlationId());
        assertEquals(0, securitySink.events.size());
    }

    @Test
    void securityCategoryRoutesToSynchronousSecuritySinkAndPreservesDeniedResult() {
        var securitySink = new RecordingSecurityAuditWriter();
        var operationSink = mock(OperationLogService.class);
        var service = new CoreAuditService(securitySink, operationSink, this::sanitize);

        service.record(securityRecord(AuditRecord.Result.DENIED));

        assertEquals(1, securitySink.events.size());
        var event = securitySink.events.getFirst();
        assertEquals(EVENT_ID, event.eventId());
        assertEquals(AuditResult.DENIED, event.result());
        assertEquals("correlation-456", event.correlationId());
        assertEquals("request-123", auditMetadata(event.after()).get("requestId"));
        assertEquals("correlation-456", auditMetadata(event.after()).get("correlationId"));
        verifyNoInteractions(operationSink);
    }

    @Test
    void securitySinkFailureIsFailClosedAndOperationSinkFailureIsPropagated() {
        var securitySink = new RecordingSecurityAuditWriter();
        securitySink.failure = true;
        var operationSink = mock(OperationLogService.class);
        var service = new CoreAuditService(securitySink, operationSink, this::sanitize);

        assertThrows(SecurityAuditUnavailableException.class, () -> service.record(securityRecord(AuditRecord.Result.FAILED)));
        verifyNoInteractions(operationSink);

        securitySink.failure = false;
        doThrow(new AuditService.AuditRecordingException("operation sink unavailable"))
                .when(operationSink)
                .record(any(AuditRecord.class));
        assertThrows(AuditService.AuditRecordingException.class, () -> service.record(operationRecord()));
    }

    @Test
    void transactionBoundaryIsDeclaredAndRepeatedCallsKeepStableEventId() throws NoSuchMethodException {
        var annotation = CoreAuditService.class.getMethod("record", AuditRecord.class).getAnnotation(Transactional.class);
        assertNotNull(annotation);

        var securitySink = new RecordingSecurityAuditWriter();
        var operationSink = mock(OperationLogService.class);
        var service = new CoreAuditService(securitySink, operationSink, this::sanitize);
        var record = operationRecord();

        service.record(record);
        service.record(record);

        var captor = org.mockito.ArgumentCaptor.forClass(AuditRecord.class);
        verify(operationSink, org.mockito.Mockito.times(2)).record(captor.capture());
        assertEquals(List.of(EVENT_ID, EVENT_ID), captor.getAllValues().stream().map(AuditRecord::eventId).toList());
    }

    @Test
    void operationSinkFailureRollsBackTheOwningTransaction() {
        var transactionManager = new RecordingTransactionManager();
        var securitySink = new RecordingSecurityAuditWriter();
        var operationSink = mock(OperationLogService.class);
        doThrow(new AuditService.AuditRecordingException("operation sink unavailable"))
                .when(operationSink)
                .record(any(AuditRecord.class));
        var service = new CoreAuditService(securitySink, operationSink, this::sanitize);

        var template = new TransactionTemplate(transactionManager);
        assertThrows(AuditService.AuditRecordingException.class,
                () -> template.executeWithoutResult(status -> service.record(operationRecord())));

        assertEquals(0, transactionManager.commits);
        assertEquals(1, transactionManager.rollbacks);
    }

    private AuditRecord operationRecord() {
        return new AuditRecord(EVENT_ID, AuditCategory.OPERATION, "USER.UPDATE", null, AuditRecord.Result.SUCCEEDED,
                OCCURRED_AT, context(), Map.of("password", "plain"), Map.of("token", "plain"), "user updated");
    }

    private AuditRecord securityRecord(AuditRecord.Result result) {
        return new AuditRecord(EVENT_ID, AuditCategory.SECURITY, "USER.PASSWORD_RESET", null, result,
                OCCURRED_AT, context(), Map.of("password", "plain"), Map.of("token", "plain"), "security decision");
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

    private static final class RecordingSecurityAuditWriter implements SecurityAuditWriter {

        private final List<SecurityAuditEvent> events = new ArrayList<>();
        private boolean failure;

        @Override
        public void assertAvailable() {
            if (failure) {
                throw new SecurityAuditUnavailableException("security audit unavailable");
            }
        }

        @Override
        public void append(SecurityAuditEvent event) {
            assertAvailable();
            events.add(event);
        }
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
