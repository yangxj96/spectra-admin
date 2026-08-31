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

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.core.system.javabean.entity.OperationLog;
import com.devops00.spectra.core.system.outbox.OperationLogOutboxWriter;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class OperationLogServiceImplTest {

    @Test
    void mapsUnifiedOperationRecordToStableOperationLogRecord() {
        var service = new CapturingOperationLogService();
        var eventId = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c1234");
        var operatorId = UUID.fromString("018f0a6f-3b6f-7b2f-8e4f-4e6a7d9c5678");
        var occurredAt = Instant.parse("2026-08-31T04:05:06.789Z");
        var record = new AuditRecord(eventId, AuditCategory.OPERATION, "USER.UPDATE", UUID.randomUUID(),
                AuditRecord.Result.FAILED, occurredAt,
                new AuditContext(operatorId, "request-123", "correlation-456", "WEB", "127.0.0.1", "agent"),
                Map.of("method", "PATCH", "url", "/api/users/1", "arguments", Map.of("name", "Ada")),
                Map.of("status", 422, "durationMs", 37L, "response", Map.of("code", "INVALID")),
                "validation failed");

        service.persist(record);

        assertEquals(eventId, service.captured.getId());
        assertEquals(operatorId, service.captured.getCreatedBy());
        assertEquals(occurredAt, service.captured.getCreatedAt());
        assertEquals(operatorId, service.captured.getUpdatedBy());
        assertEquals(occurredAt, service.captured.getUpdatedAt());
        assertEquals("validation failed", service.captured.getExplain());
        assertEquals((short) 422, service.captured.getStatus());
        assertEquals("127.0.0.1", service.captured.getIp());
        assertEquals("PATCH", service.captured.getMethod());
        assertEquals("/api/users/1", service.captured.getUrl());
        assertEquals(37L, service.captured.getTimeCost());
        assertEquals(eventId.toString(), auditMetadata(service.captured.getArgs()).get("eventId"));
        assertEquals("USER.UPDATE", auditMetadata(service.captured.getResult()).get("eventType"));
        assertEquals("request-123", auditMetadata(service.captured.getResult()).get("requestId"));
        assertEquals("correlation-456", auditMetadata(service.captured.getResult()).get("correlationId"));
    }

    @Test
    void recordWritesToOutboxInsteadOfWritingSysLogDirectly() {
        var writer = mock(OperationLogOutboxWriter.class);
        var service = new OperationLogServiceImpl(writer);
        var record = new AuditRecord(UUID.randomUUID(), AuditCategory.OPERATION, "USER.UPDATE", null,
                AuditRecord.Result.SUCCEEDED, Instant.now(),
                new AuditContext(null, null, null, null, null, null), Map.of(), Map.of(), null);

        service.record(record);

        verify(writer).write(record);
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> auditMetadata(Map<String, Object> snapshot) {
        return (Map<String, Object>) snapshot.get("_audit");
    }

    private static final class CapturingOperationLogService extends OperationLogServiceImpl {

        private OperationLog captured;

        private CapturingOperationLogService() {
            super(mock(OperationLogOutboxWriter.class));
        }

        @Override
        protected boolean saveIdempotently(OperationLog entity) {
            captured = entity;
            return true;
        }
    }
}
