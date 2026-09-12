/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
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

class AuditRecordFactoryTest {

    @Test
    void createsSecurityCategoryAndSanitizesSnapshotsAndReason() {
        AuditSanitizer sanitizer = mock(AuditSanitizer.class);
        when(sanitizer.sanitize(any())).thenReturn(Map.of("password", "***", "reason", "safe"));
        var factory = new AuditRecordFactory(sanitizer);
        UUID eventId = UUID.randomUUID();

        AuditRecord record = factory.create(eventId, "USER_UPDATED", UUID.randomUUID(), null, "WEB", null,
                null, Map.of("password", "plain"), Map.of("password", "plain"), "unsafe reason", Instant.EPOCH,
                AuditRecord.Result.SUCCEEDED, "correlation-1");

        assertEquals(eventId, record.eventId());
        assertEquals(AuditCategory.SECURITY, record.category());
        assertEquals("***", record.before().get("password"));
        assertEquals("***", record.after().get("password"));
        assertEquals("safe", record.reason());
        verify(sanitizer, times(3)).sanitize(any());
    }
}
