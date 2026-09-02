/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.outbox;

import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditUnavailableException;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;

class SecurityChangeOutboxProducerTest {

    private static final Instant NOW = Instant.parse("2026-08-31T04:05:06Z");

    @Test
    void publishesOnlySuccessfulConfigurationChangesWithRestorableSanitizedEvent() throws Exception {
        var repository = mock(SecurityChangeOutboxRepository.class);
        var producer = new SecurityChangeOutboxProducer(repository, new ObjectMapper());
        var event = event("USER_STATUS_CHANGED", AuditResult.SUCCEEDED);

        producer.publish(event);

        var payload = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(repository).enqueue(eq(event.eventId()), any(String.class), eq(event.eventType()), eq("USER"),
                eq(event.targetId()), payload.capture(), eq(event.correlationId()), any(Instant.class), eq(event.operatorId()));
        SecurityAuditEvent restored = new ObjectMapper().readValue(payload.getValue(), SecurityAuditEvent.class);
        assertEquals(event.eventId(), restored.eventId());
        assertEquals(event.targetId(), restored.targetId());
        assertEquals(event.correlationId(), restored.correlationId());
        assertFalse(payload.getValue().contains("raw-password"));
        assertFalse(payload.getValue().contains("access-token"));
    }

    @Test
    void ignoresAuthenticationAndUnsuccessfulAuditFacts() {
        var repository = mock(SecurityChangeOutboxRepository.class);
        var producer = new SecurityChangeOutboxProducer(repository, new ObjectMapper());

        producer.publish(event("AUTH_LOGIN_SUCCEEDED", AuditResult.SUCCEEDED));
        producer.publish(event("USER_STATUS_CHANGED", AuditResult.FAILED));
        producer.publish(event("SECURITY_AUDIT_VIEWED", AuditResult.SUCCEEDED));

        verify(repository, never()).enqueue(any(), any(), any(), any(), any(), any(), any(), any(), any());
        assertFalse(SecurityChangeOutboxProducer.isDispatchable("MFA_FACTOR_ENROLLED"));
        assertFalse(SecurityChangeOutboxProducer.isDispatchable("AUTH_LOGIN_SUCCEEDED"));
    }

    @Test
    void outboxFailureFailsClosedForConfigurationMutation() {
        var repository = mock(SecurityChangeOutboxRepository.class);
        doThrow(new IllegalStateException("database unavailable")).when(repository)
                .enqueue(
                        any(), any(), any(), any(), any(), any(), any(), any(), any());
        var producer = new SecurityChangeOutboxProducer(repository, new ObjectMapper());

        assertThrows(SecurityAuditUnavailableException.class,
                () -> producer.publish(event("ROLE_PERMISSION_CHANGED", AuditResult.SUCCEEDED)));
    }

    private static SecurityAuditEvent event(String type, AuditResult result) {
        return new SecurityAuditEvent(UUID.randomUUID(), type, UUID.randomUUID(), UUID.randomUUID(), "WEB",
                "127.0.0.1", "agent", Map.of("password", "raw-password"),
                Map.of("accessToken", "access-token"), "test", NOW, result, "corr-123");
    }
}
