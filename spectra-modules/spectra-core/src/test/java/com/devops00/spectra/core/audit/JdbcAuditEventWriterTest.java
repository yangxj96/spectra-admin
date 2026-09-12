/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.core.audit.repository.JdbcAuditEventWriter;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCreator;
import tools.jackson.databind.ObjectMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class JdbcAuditEventWriterTest {

    @Test
    void operationAndSecurityMustUseTheSameInsertWithExplicitCommonFields() throws Exception {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        Connection connection = mock(Connection.class);
        PreparedStatement statement = mock(PreparedStatement.class);
        when(connection.prepareStatement(anyString())).thenReturn(statement);
        when(jdbcTemplate.update(any(PreparedStatementCreator.class))).thenAnswer(invocation -> {
            ((PreparedStatementCreator) invocation.getArgument(0)).createPreparedStatement(connection);
            return 1;
        });
        var writer = new JdbcAuditEventWriter(jdbcTemplate, new ObjectMapper());

        writer.append(record(AuditCategory.OPERATION));
        writer.append(record(AuditCategory.SECURITY));

        var sqlCaptor = org.mockito.ArgumentCaptor.forClass(String.class);
        verify(connection, times(2)).prepareStatement(sqlCaptor.capture());
        List<String> statements = sqlCaptor.getAllValues();
        assertEquals(statements.getFirst(), statements.get(1));
        assertTrue(statements.getFirst().contains("INSERT INTO spectra_core.sys_audit_event"));
        verify(statement).setString(3, "OPERATION");
        verify(statement).setString(3, "SECURITY");
        verify(statement, times(2)).setString(4, "TEST.EVENT");
        verify(statement, times(2)).setString(20, "E_FAILURE");
        verify(statement, times(2)).setString(21, "IllegalStateException");
        verify(statement, times(2)).setString(22, "操作执行失败");
    }

    @Test
    void writerFailureMustBeWrappedInAuditRecordingException() {
        JdbcTemplate jdbcTemplate = mock(JdbcTemplate.class);
        when(jdbcTemplate.update(any(PreparedStatementCreator.class)))
                .thenThrow(new IllegalStateException("database unavailable"));
        var writer = new JdbcAuditEventWriter(jdbcTemplate, new ObjectMapper());

        assertThrows(AuditService.AuditRecordingException.class,
                () -> writer.append(record(AuditCategory.SECURITY)));
    }

    private static AuditRecord record(AuditCategory category) {
        return new AuditRecord(UUID.randomUUID(), category, "TEST.EVENT", null,
                AuditRecord.Result.FAILED, Instant.parse("2026-09-13T00:00:00Z"), AuditContext.empty(),
                Map.of("before", true), Map.of("after", true), "business reason",
                new AuditRecord.HttpSummary("POST", "/api/test?token=secret", 500, 12L),
                new AuditRecord.Failure("E_FAILURE", "IllegalStateException", "操作执行失败"));
    }
}
