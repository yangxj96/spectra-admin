/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.service;

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.core.audit.javabean.domain.AuditLogQueryCriteria;
import com.devops00.spectra.core.audit.javabean.domain.AuditLogQueryRow;
import com.devops00.spectra.core.audit.javabean.from.AuditLogQueryFrom;
import com.devops00.spectra.core.audit.mapper.AuditLogQueryMapper;
import com.devops00.spectra.core.audit.observability.AuditLogMetrics;
import com.devops00.spectra.core.audit.policy.DefaultAuditVisibilityPolicy;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuditLogQueryServiceTest {

    @Test
    void ordinaryReaderCriteriaHideHighRiskRowsAndLimitSecurityRowsToSelf() {
        AuditLogQueryMapper mapper = mock(AuditLogQueryMapper.class);
        when(mapper.countPage(any(AuditLogQueryCriteria.class))).thenReturn(2L);
        doReturn(List.of()).when(mapper).selectPage(any(), anyLong(), anyLong());
        var service = service(mapper, mock(ObjectMapper.class), mock(AuditSanitizer.class));
        UUID viewerId = UUID.randomUUID();

        var page = service.page(authentication(viewerId, "ROLE_USER"), null, new AuditLogQueryFrom());

        assertEquals(2L, page.total());
        var criteria = org.mockito.ArgumentCaptor.forClass(AuditLogQueryCriteria.class);
        verify(mapper).countPage(criteria.capture());
        assertEquals(false, criteria.getValue().isCanViewHighRisk());
        assertEquals(false, criteria.getValue().isCanViewAllNonHighRisk());
        assertEquals(viewerId, criteria.getValue().getViewerId());
    }

    @Test
    void snapshotUsesInjectedSanitizerAndInvalidJsonFailsClosed() throws Exception {
        AuditLogQueryMapper mapper = mock(AuditLogQueryMapper.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        AuditSanitizer sanitizer = mock(AuditSanitizer.class);
        when(objectMapper.readValue("{\"token\":\"plain\"}", Map.class)).thenReturn(Map.of("token", "plain"));
        when(sanitizer.sanitize(any())).thenReturn(Map.of("token", "***"));
        var service = service(mapper, objectMapper, sanitizer);
        var method = AuditLogQueryService.class.getDeclaredMethod("parseSnapshot", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> snapshot = (Map<String, Object>) method.invoke(service, "{\"token\":\"plain\"}");

        assertEquals("***", snapshot.get("token"));
        verify(sanitizer).sanitize(any());

        when(objectMapper.readValue("invalid", Map.class)).thenThrow(new IllegalArgumentException("invalid json"));
        @SuppressWarnings("unchecked")
        Map<String, Object> invalid = (Map<String, Object>) method.invoke(service, "invalid");
        assertEquals("invalid_snapshot", invalid.get("_redacted"));
    }

    @Test
    void detailRequiresThePartitionTimestampAlongsideEventId() {
        AuditLogQueryMapper mapper = mock(AuditLogQueryMapper.class);
        UUID eventId = UUID.randomUUID();
        Instant occurredAt = Instant.parse("2026-09-13T00:00:00Z");
        when(mapper.selectDetail(any(), eq(eventId), eq(occurredAt))).thenReturn(null);
        var service = service(mapper, mock(ObjectMapper.class), mock(AuditSanitizer.class));
        var viewer = authentication(UUID.randomUUID(), "ROLE_DEV_OPS");

        assertThrows(com.devops00.spectra.common.exception.DataNotExistException.class,
                () -> service.detail(viewer, eventId, occurredAt));
        var criteria = org.mockito.ArgumentCaptor.forClass(AuditLogQueryCriteria.class);
        verify(mapper).selectDetail(criteria.capture(), eq(eventId), eq(occurredAt));
        assertTrue(criteria.getValue().isCanViewHighRisk());
    }

    @Test
    void pagePreservesTheExactPartitionTimestampIncludingFractionalSeconds() {
        AuditLogQueryMapper mapper = mock(AuditLogQueryMapper.class);
        Instant occurredAt = Instant.parse("2026-09-13T03:53:16.442Z");
        UUID eventId = UUID.randomUUID();
        AuditLogQueryRow row = new AuditLogQueryRow();
        row.setEventId(eventId);
        row.setOccurredAt(occurredAt);
        row.setCategory("OPERATION");
        row.setEventType("AUDIT_LOG_PAGE_VIEWED");
        row.setOperatorName("张三");
        row.setResult("SUCCEEDED");
        when(mapper.countPage(any())).thenReturn(1L);
        when(mapper.selectPage(any(), anyLong(), anyLong())).thenReturn(List.of(row));

        var service = service(mapper, mock(ObjectMapper.class), mock(AuditSanitizer.class));
        var page = service.page(authentication(UUID.randomUUID(), "ROLE_DEV_OPS"), null, new AuditLogQueryFrom());

        assertEquals(occurredAt, page.records().getFirst().occurredAt());
        assertEquals("张三", page.records().getFirst().operatorName());
        assertEquals(eventId, page.records().getFirst().eventId());
    }

    @Test
    void operatorAndEventTypeSearchesBecomeTypedMapperCriteria() {
        AuditLogQueryMapper mapper = mock(AuditLogQueryMapper.class);
        when(mapper.countPage(any())).thenReturn(1L);
        doReturn(List.of()).when(mapper).selectPage(any(), anyLong(), anyLong());
        var service = service(mapper, mock(ObjectMapper.class), mock(AuditSanitizer.class));
        UUID operatorId = UUID.randomUUID();
        var query = new AuditLogQueryFrom();
        query.setOperator(operatorId.toString());
        query.setEventType("用户详情");

        service.page(authentication(UUID.randomUUID(), "ROLE_DEV_OPS"), null, query);

        var criteria = org.mockito.ArgumentCaptor.forClass(AuditLogQueryCriteria.class);
        verify(mapper).countPage(criteria.capture());
        assertEquals("%用户详情%", criteria.getValue().getEventTypePattern());
        assertEquals(operatorId, criteria.getValue().getOperatorId());
        assertEquals("%" + operatorId + "%", criteria.getValue().getOperatorPattern());
    }

    @Test
    void operatorNameSearchSupportsPartialNamesWithoutParsingThemAsIds() {
        AuditLogQueryMapper mapper = mock(AuditLogQueryMapper.class);
        when(mapper.countPage(any())).thenReturn(0L);
        doReturn(List.of()).when(mapper).selectPage(any(), anyLong(), anyLong());
        var service = service(mapper, mock(ObjectMapper.class), mock(AuditSanitizer.class));
        var query = new AuditLogQueryFrom();
        query.setOperator("张三");

        service.page(authentication(UUID.randomUUID(), "ROLE_DEV_OPS"), null, query);

        var criteria = org.mockito.ArgumentCaptor.forClass(AuditLogQueryCriteria.class);
        verify(mapper).countPage(criteria.capture());
        assertEquals(null, criteria.getValue().getOperatorId());
        assertEquals("%张三%", criteria.getValue().getOperatorPattern());
    }

    @Test
    void failedSnapshotParsingDoesNotCallTheSanitizer() throws Exception {
        AuditLogQueryMapper mapper = mock(AuditLogQueryMapper.class);
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        AuditSanitizer sanitizer = mock(AuditSanitizer.class);
        when(objectMapper.readValue("invalid", Map.class)).thenThrow(new IllegalArgumentException("invalid json"));
        var service = service(mapper, objectMapper, sanitizer);
        var method = AuditLogQueryService.class.getDeclaredMethod("parseSnapshot", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> snapshot = (Map<String, Object>) method.invoke(service, "invalid");

        assertEquals("invalid_snapshot", snapshot.get("_redacted"));
        verifyNoInteractions(sanitizer);
    }

    private static AuditLogQueryService service(AuditLogQueryMapper mapper, ObjectMapper objectMapper,
                                                AuditSanitizer sanitizer) {
        return new AuditLogQueryService(mapper, objectMapper, new DefaultAuditVisibilityPolicy(),
                mock(AuditLogMetrics.class), mock(TimeMapper.class), sanitizer);
    }

    private static TestingAuthenticationToken authentication(UUID principal, String... authorities) {
        var authentication = new TestingAuthenticationToken(principal, null, authorities);
        authentication.setAuthenticated(true);
        return authentication;
    }
}
