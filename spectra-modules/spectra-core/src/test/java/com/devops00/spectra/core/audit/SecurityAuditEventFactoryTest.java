/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.core.security.audit.AuditResult;
import com.devops00.spectra.core.security.audit.SecurityAuditEvent;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class SecurityAuditEventFactoryTest {

    @Test
    void createSanitizesBothSnapshotsThroughInjectedStrategy() {
        AuditSanitizer sanitizer = mock(AuditSanitizer.class);
        when(sanitizer.sanitize(any())).thenReturn(Map.of("password", "***"));
        var factory = new SecurityAuditEventFactory(sanitizer);
        UUID eventId = UUID.randomUUID();

        SecurityAuditEvent event = factory.create(eventId, "USER_UPDATED", UUID.randomUUID(), null, "WEB", null,
                null, Map.of("password", "plain"), Map.of("password", "plain"), "updated", Instant.EPOCH,
                AuditResult.SUCCEEDED, "correlation-1");

        assertEquals(eventId, event.eventId());
        assertEquals("***", event.before().get("password"));
        assertEquals("***", event.after().get("password"));
        verify(sanitizer, times(2)).sanitize(any());
    }
}
