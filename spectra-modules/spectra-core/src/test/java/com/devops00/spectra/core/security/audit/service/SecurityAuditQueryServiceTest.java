/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.core.security.audit.service;

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.core.audit.SecurityAuditEventFactory;
import com.devops00.spectra.core.security.audit.AuditVisibilityPolicy;
import com.devops00.spectra.core.security.audit.SecurityAuditWriter;
import com.devops00.spectra.core.security.audit.observability.SecurityAuditMetrics;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class SecurityAuditQueryServiceTest {

    @Test
    void parseSnapshotUsesInjectedSanitizer() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        AuditSanitizer sanitizer = mock(AuditSanitizer.class);
        when(objectMapper.readValue("{\"token\":\"plain\"}", Map.class))
                .thenReturn(Map.of("token", "plain"));
        when(sanitizer.sanitize(any())).thenReturn(Map.of("token", "***"));
        var service = new SecurityAuditQueryService(mock(org.springframework.jdbc.core.JdbcTemplate.class), objectMapper,
                mock(AuditVisibilityPolicy.class), mock(SecurityAuditWriter.class), mock(SecurityAuditMetrics.class),
                mock(TimeMapper.class), sanitizer, mock(SecurityAuditEventFactory.class));

        var method = SecurityAuditQueryService.class.getDeclaredMethod("parseSnapshot", String.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> snapshot = (Map<String, Object>) invoke(method, service, "{\"token\":\"plain\"}");

        assertEquals("***", snapshot.get("token"));
        verify(sanitizer).sanitize(any());
    }

    @Test
    void parseSnapshotRedactsInvalidJsonWithoutCallingSanitizer() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        AuditSanitizer sanitizer = mock(AuditSanitizer.class);
        when(objectMapper.readValue("invalid", Map.class)).thenThrow(new IllegalArgumentException("invalid json"));
        var service = new SecurityAuditQueryService(mock(org.springframework.jdbc.core.JdbcTemplate.class), objectMapper,
                mock(AuditVisibilityPolicy.class), mock(SecurityAuditWriter.class), mock(SecurityAuditMetrics.class),
                mock(TimeMapper.class), sanitizer, mock(SecurityAuditEventFactory.class));

        var method = SecurityAuditQueryService.class.getDeclaredMethod("parseSnapshot", String.class);
        method.setAccessible(true);
        @SuppressWarnings("unchecked")
        Map<String, Object> snapshot = (Map<String, Object>) invoke(method, service, "invalid");

        assertEquals("invalid_snapshot", snapshot.get("_redacted"));
        verifyNoInteractions(sanitizer);
    }

    private static Object invoke(java.lang.reflect.Method method, Object target, String json)
            throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, json);
    }
}
