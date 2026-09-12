/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.query;

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.authentication.TestingAuthenticationToken;
import tools.jackson.databind.ObjectMapper;

import java.lang.reflect.InvocationTargetException;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

class AuditLogQueryServiceTest {

    @Test
    void ordinaryReaderCountAndPageSqlHideOnlyHighRiskSecurityRowsAndLimitSecurityRowsToSelf() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), any(Object[].class), eq(Long.class))).thenReturn(2L);
        doReturn(List.of()).when(jdbc).query(anyString(), any(Object[].class), any(RowMapper.class));
        var service = service(jdbc, mock(ObjectMapper.class), mock(AuditSanitizer.class));
        UUID viewerId = UUID.randomUUID();

        var page = service.page(authentication(viewerId, "ROLE_USER"), null, new AuditLogQueryFrom());

        assertEquals(2L, page.total());
        var countSql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbc).queryForObject(countSql.capture(), any(Object[].class), eq(Long.class));
        assertTrue(countSql.getValue().contains("category <> 'SECURITY' OR NOT"));
        assertTrue(countSql.getValue().contains("category <> 'SECURITY' OR operator_id = ? OR target_id = ?"));
    }

    @Test
    void snapshotUsesInjectedSanitizerAndInvalidJsonFailsClosed() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        AuditSanitizer sanitizer = mock(AuditSanitizer.class);
        when(objectMapper.readValue("{\"token\":\"plain\"}", Map.class)).thenReturn(Map.of("token", "plain"));
        when(sanitizer.sanitize(any())).thenReturn(Map.of("token", "***"));
        var service = service(mock(JdbcTemplate.class), objectMapper, sanitizer);
        var method = AuditLogQueryService.class.getDeclaredMethod("parseSnapshot", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> snapshot = (Map<String, Object>) invoke(method, service, "{\"token\":\"plain\"}");

        assertEquals("***", snapshot.get("token"));
        verify(sanitizer).sanitize(any());

        when(objectMapper.readValue("invalid", Map.class)).thenThrow(new IllegalArgumentException("invalid json"));
        @SuppressWarnings("unchecked")
        Map<String, Object> invalid = (Map<String, Object>) invoke(method, service, "invalid");
        assertEquals("invalid_snapshot", invalid.get("_redacted"));
    }

    @Test
    void detailRequiresThePartitionTimestampAlongsideEventId() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        doReturn(List.of()).when(jdbc).query(anyString(), any(Object[].class), any(RowMapper.class));
        var service = service(jdbc, mock(ObjectMapper.class), mock(AuditSanitizer.class));
        var viewer = authentication(UUID.randomUUID(), "ROLE_DEV_OPS");

        assertThrows(com.devops00.spectra.common.exception.DataNotExistException.class,
                () -> service.detail(viewer, UUID.randomUUID(), java.time.Instant.parse("2026-09-13T00:00:00Z")));
        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(jdbc).query(sql.capture(), any(Object[].class), any(RowMapper.class));
        assertTrue(sql.getValue().contains("event_id = ? AND occurred_at = ?"));
    }

    @Test
    void pagePreservesTheExactPartitionTimestampIncludingFractionalSeconds() throws Exception {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), any(Object[].class), eq(Long.class))).thenReturn(1L);

        Instant occurredAt = Instant.parse("2026-09-13T03:53:16.442Z");
        UUID eventId = UUID.randomUUID();
        ResultSet resultSet = mock(ResultSet.class);
        when(resultSet.getObject("event_id", UUID.class)).thenReturn(eventId);
        when(resultSet.getTimestamp("occurred_at")).thenReturn(Timestamp.from(occurredAt));
        when(resultSet.getString("category")).thenReturn("OPERATION");
        when(resultSet.getString("event_type")).thenReturn("AUDIT_LOG_PAGE_VIEWED");
        when(resultSet.getString("operator_name")).thenReturn("张三");
        when(resultSet.getString("result")).thenReturn("SUCCEEDED");
        doAnswer(invocation -> {
            @SuppressWarnings("unchecked")
            RowMapper<AuditLogVO> rowMapper = (RowMapper<AuditLogVO>) invocation.getArgument(2);
            return List.of(rowMapper.mapRow(resultSet, 0));
        }).when(jdbc).query(anyString(), any(Object[].class), any(RowMapper.class));

        var service = service(jdbc, mock(ObjectMapper.class), mock(AuditSanitizer.class));
        var page = service.page(authentication(UUID.randomUUID(), "ROLE_DEV_OPS"), null, new AuditLogQueryFrom());

        assertEquals(occurredAt, page.records().getFirst().occurredAt());
        assertEquals("张三", page.records().getFirst().operatorName());
    }

    @Test
    void operatorAndEventTypeSearchesMatchTheirIdsNamesAndAuditDescriptions() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), any(Object[].class), eq(Long.class))).thenReturn(1L);
        doReturn(List.of()).when(jdbc).query(anyString(), any(Object[].class), any(RowMapper.class));
        var service = service(jdbc, mock(ObjectMapper.class), mock(AuditSanitizer.class));
        UUID operatorId = UUID.randomUUID();
        var query = new AuditLogQueryFrom();
        query.setOperator(operatorId.toString());
        query.setEventType("用户详情");

        service.page(authentication(UUID.randomUUID(), "ROLE_DEV_OPS"), null, query);

        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        var parameters = org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).queryForObject(sql.capture(), parameters.capture(), eq(Long.class));
        assertTrue(sql.getValue().contains("event_type ILIKE ? OR reason ILIKE ?"));
        assertTrue(sql.getValue().contains("operator_id = ? OR EXISTS"));
        assertTrue(sql.getValue().contains("audit_operator.real_name ILIKE ?"));
        assertEquals(List.of("%用户详情%", "%用户详情%", operatorId, "%" + operatorId + "%"),
                java.util.Arrays.asList(parameters.getValue()));
    }

    @Test
    void operatorNameSearchSupportsPartialNamesWithoutParsingThemAsIds() {
        JdbcTemplate jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForObject(anyString(), any(Object[].class), eq(Long.class))).thenReturn(0L);
        doReturn(List.of()).when(jdbc).query(anyString(), any(Object[].class), any(RowMapper.class));
        var service = service(jdbc, mock(ObjectMapper.class), mock(AuditSanitizer.class));
        var query = new AuditLogQueryFrom();
        query.setOperator("张三");

        service.page(authentication(UUID.randomUUID(), "ROLE_DEV_OPS"), null, query);

        var sql = org.mockito.ArgumentCaptor.forClass(String.class);
        var parameters = org.mockito.ArgumentCaptor.forClass(Object[].class);
        verify(jdbc).queryForObject(sql.capture(), parameters.capture(), eq(Long.class));
        assertTrue(sql.getValue().contains("audit_operator.real_name ILIKE ?"));
        assertEquals(List.of("%张三%"), java.util.Arrays.asList(parameters.getValue()));
    }

    @Test
    void failedSnapshotParsingDoesNotCallTheSanitizer() throws Exception {
        ObjectMapper objectMapper = mock(ObjectMapper.class);
        AuditSanitizer sanitizer = mock(AuditSanitizer.class);
        when(objectMapper.readValue("invalid", Map.class)).thenThrow(new IllegalArgumentException("invalid json"));
        var service = service(mock(JdbcTemplate.class), objectMapper, sanitizer);
        var method = AuditLogQueryService.class.getDeclaredMethod("parseSnapshot", String.class);
        method.setAccessible(true);

        @SuppressWarnings("unchecked")
        Map<String, Object> snapshot = (Map<String, Object>) invoke(method, service, "invalid");

        assertEquals("invalid_snapshot", snapshot.get("_redacted"));
        verifyNoInteractions(sanitizer);
    }

    private static AuditLogQueryService service(JdbcTemplate jdbc, ObjectMapper mapper, AuditSanitizer sanitizer) {
        return new AuditLogQueryService(jdbc, mapper, new DefaultAuditVisibilityPolicy(),
                mock(AuditLogMetrics.class), mock(TimeMapper.class), sanitizer);
    }

    private static TestingAuthenticationToken authentication(UUID principal, String... authorities) {
        var authentication = new TestingAuthenticationToken(principal, null, authorities);
        authentication.setAuthenticated(true);
        return authentication;
    }

    private static Object invoke(java.lang.reflect.Method method, Object target, String json)
            throws InvocationTargetException, IllegalAccessException {
        return method.invoke(target, json);
    }
}
