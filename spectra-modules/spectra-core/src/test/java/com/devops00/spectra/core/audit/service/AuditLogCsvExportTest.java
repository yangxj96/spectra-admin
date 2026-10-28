/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.core.audit.service;

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.core.audit.javabean.domain.AuditLogQueryRow;
import com.devops00.spectra.core.audit.mapper.AuditLogQueryMapper;
import com.devops00.spectra.core.audit.observability.AuditLogMetrics;
import com.devops00.spectra.core.audit.policy.AuditVisibilityPolicy;
import com.devops00.spectra.core.audit.service.impl.AuditLogQueryServiceImpl;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class AuditLogCsvExportTest {

    @Test
    void exportEscapesSpreadsheetFormulaPrefixes() {
        var mapper = mock(AuditLogQueryMapper.class);
        var objectMapper = mock(ObjectMapper.class);
        var visibilityPolicy = mock(AuditVisibilityPolicy.class);
        var metrics = mock(AuditLogMetrics.class);
        var timeMapper = mock(TimeMapper.class);
        var sanitizer = mock(AuditSanitizer.class);
        var viewer = mock(Authentication.class);
        var row = new AuditLogQueryRow();
        row.setEventId(UUID.randomUUID());
        row.setOccurredAt(Instant.EPOCH);
        row.setCategory("SECURITY");
        row.setEventType("=cmd|' /C calc'!A0");
        row.setResult("SUCCEEDED");
        row.setReason("+SUM(1+1)");
        row.setFailureReason("@SUM(1+1)");
        when(viewer.isAuthenticated()).thenReturn(true);
        when(visibilityPolicy.canViewHighRisk(viewer)).thenReturn(false);
        when(visibilityPolicy.canViewAllNonHighRisk(viewer)).thenReturn(false);
        when(visibilityPolicy.viewerId(viewer)).thenReturn(UUID.randomUUID());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(sanitizer.sanitize(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.selectExport(any(), anyInt())).thenReturn(List.of(row));

        var csv = new AuditLogQueryServiceImpl(mapper, objectMapper, visibilityPolicy, metrics, timeMapper, sanitizer)
                .export(viewer, null);

        assertTrue(csv.contains("\"'=cmd|' /C calc'!A0\""));
        assertTrue(csv.contains("\"'+SUM(1+1)\""));
        assertTrue(csv.contains("\"'@SUM(1+1)\""));
        assertFalse(csv.contains("\"=cmd"));
        assertFalse(csv.contains("\"+SUM"));
        assertFalse(csv.contains("\"@SUM"));
    }

    @Test
    void numericCellsAreNotPrefixedAsFormulas() {
        var mapper = mock(AuditLogQueryMapper.class);
        var objectMapper = mock(ObjectMapper.class);
        var visibilityPolicy = mock(AuditVisibilityPolicy.class);
        var metrics = mock(AuditLogMetrics.class);
        var timeMapper = mock(TimeMapper.class);
        var sanitizer = mock(AuditSanitizer.class);
        var viewer = mock(Authentication.class);
        var row = new AuditLogQueryRow();
        row.setEventId(UUID.randomUUID());
        row.setOccurredAt(Instant.EPOCH);
        row.setCategory("SECURITY");
        row.setEventType("TEST");
        row.setResult("SUCCEEDED");
        row.setDurationMs(-1L);
        when(viewer.isAuthenticated()).thenReturn(true);
        when(visibilityPolicy.canViewHighRisk(viewer)).thenReturn(false);
        when(visibilityPolicy.canViewAllNonHighRisk(viewer)).thenReturn(false);
        when(visibilityPolicy.viewerId(viewer)).thenReturn(UUID.randomUUID());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");
        when(sanitizer.sanitize(any())).thenAnswer(invocation -> invocation.getArgument(0));
        when(mapper.selectExport(any(), anyInt())).thenReturn(List.of(row));

        var csv = new AuditLogQueryServiceImpl(mapper, objectMapper, visibilityPolicy, metrics, timeMapper, sanitizer)
                .export(viewer, null);

        assertTrue(csv.contains("\"-1\""));
    }
}
