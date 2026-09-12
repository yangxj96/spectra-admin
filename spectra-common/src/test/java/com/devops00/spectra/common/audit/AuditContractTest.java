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

package com.devops00.spectra.common.audit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.Instant;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class AuditContractTest {

    @Test
    void auditRecordMustCarryStructuredHttpAndFailureDetails() {
        var componentNames = Arrays.stream(AuditRecord.class.getRecordComponents())
                .map(java.lang.reflect.RecordComponent::getName)
                .toList();

        assertTrue(componentNames.contains("httpSummary"));
        assertTrue(componentNames.contains("failure"));

        var record = new AuditRecord(null, AuditCategory.SECURITY, "LOGIN.FAILED", null,
                AuditRecord.Result.FAILED, Instant.now(), AuditContext.empty(), Map.of(), Map.of(),
                "business reason", new AuditRecord.HttpSummary("POST", "/login?token=secret", 401, 12L),
                new AuditRecord.Failure("AUTH_DENIED", "AccessDeniedException", "权限不足"));

        assertEquals("/login", record.httpSummary().url());
        assertEquals(401, record.httpSummary().status());
        assertEquals("AUTH_DENIED", record.failure().code());
        assertEquals("AccessDeniedException", record.failure().type());
        assertEquals("权限不足", record.failure().reason());
    }

    @Test
    void auditCategoryMustExposeOperationAndSecurity() {
        assertEquals(
                List.of(AuditCategory.OPERATION, AuditCategory.SECURITY),
                List.of(AuditCategory.values()));
    }

    @Test
    void auditRecordMustBeImmutableAndKeepUnifiedContext() {
        UUID operatorId = UUID.randomUUID();
        UUID targetId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-08-31T00:00:00Z");
        Map<String, Object> nested = new LinkedHashMap<>();
        nested.put("name", "Alice");
        Map<String, Object> before = new LinkedHashMap<>();
        before.put("profile", nested);

        AuditRecord record = new AuditRecord(
                null,
                AuditCategory.OPERATION,
                "USER_UPDATED",
                targetId,
                AuditRecord.Result.SUCCEEDED,
                occurredAt,
                new AuditContext(operatorId, "request-1", "correlation-1", "WEB", "127.0.0.1", "agent"),
                before,
                Map.of("enabled", true),
                "profile update");

        nested.put("name", "Mutated");
        before.put("new-field", "must not leak");

        assertNotNull(record.eventId());
        assertEquals(AuditCategory.OPERATION, record.category());
        assertEquals("USER_UPDATED", record.eventType());
        assertEquals(targetId, record.targetId());
        assertEquals(AuditRecord.Result.SUCCEEDED, record.result());
        assertEquals(occurredAt, record.occurredAt());
        assertEquals(operatorId, record.context().operatorId());
        assertEquals("request-1", record.context().requestId());
        assertEquals("correlation-1", record.context().correlationId());
        assertEquals("Alice", ((Map<?, ?>) record.before().get("profile")).get("name"));
        assertTrue(record.before().get("new-field") == null);
        assertThrows(UnsupportedOperationException.class, () -> record.before().put("x", "y"));
        assertThrows(UnsupportedOperationException.class,
                () -> ((Map<String, Object>) record.before().get("profile")).put("x", "y"));
    }

    @Test
    void auditRecordMustRejectMissingBusinessIdentity() {
        AuditContext context = AuditContext.empty();

        assertThrows(IllegalArgumentException.class, () -> new AuditRecord(
                null, AuditCategory.OPERATION, " ", null, AuditRecord.Result.SUCCEEDED,
                Instant.now(), context, Map.of(), Map.of(), null));
        assertThrows(NullPointerException.class, () -> new AuditRecord(
                null, null, "USER_UPDATED", null, AuditRecord.Result.SUCCEEDED,
                Instant.now(), context, Map.of(), Map.of(), null));
        assertThrows(NullPointerException.class, () -> new AuditRecord(
                null, AuditCategory.OPERATION, "USER_UPDATED", null, null,
                Instant.now(), context, Map.of(), Map.of(), null));
    }

    @Test
    void auditServiceMustExposeOnlyRecordEntryPointWithExplicitFailureType() throws NoSuchMethodException {
        var method = AuditService.class.getMethod("record", AuditRecord.class);

        assertEquals(void.class, method.getReturnType());
        assertTrue(AuditService.class.getMethod("assertAvailable").isDefault());
        assertEquals(1, Arrays.stream(AuditService.class.getDeclaredMethods())
                .filter(declaredMethod -> !declaredMethod.isDefault())
                .count());
        assertInstanceOf(Class.class, AuditService.AuditRecordingException.class);
        assertTrue(RuntimeException.class.isAssignableFrom(AuditService.AuditRecordingException.class));
    }

    @Test
    void auditSanitizerMustDefineOneSharedSnapshotContract() throws NoSuchMethodException {
        var method = AuditSanitizer.class.getMethod("sanitize", Map.class);

        assertEquals(Map.class, method.getReturnType());
        assertEquals("***", AuditSanitizer.REDACTED_VALUE);
        assertEquals(1, AuditSanitizer.class.getDeclaredMethods().length);
    }
}
