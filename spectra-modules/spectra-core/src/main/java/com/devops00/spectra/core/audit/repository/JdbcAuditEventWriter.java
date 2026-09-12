/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.audit.repository;

import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.UUID;

/**
 * 统一审计事实表的同步 JDBC 写入器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Repository
@RequiredArgsConstructor
public class JdbcAuditEventWriter {

    private static final String TABLE = "spectra_core.sys_audit_event";

    private static final String INSERT_SQL = "INSERT INTO " + TABLE
            + " (event_id, occurred_at, category, event_type, operator_id, target_id, result, client, ip, user_agent,"
            + " request_id, correlation_id, http_method, request_url, http_status, duration_ms, before_snapshot,"
            + " after_snapshot, reason, failure_code, failure_type, failure_reason)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 同步追加一条操作或安全审计事实。
     *
     * @param record 脱敏后的统一审计记录
     */
    public void append(AuditRecord record) {
        if (record == null) {
            throw new AuditService.AuditRecordingException("统一审计记录不能为空");
        }
        try {
            String before = objectMapper.writeValueAsString(record.before());
            String after = objectMapper.writeValueAsString(record.after());
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL);
                AuditRecord.HttpSummary http = record.httpSummary();
                AuditRecord.Failure failure = record.failure();
                setUuid(statement, 1, record.eventId());
                statement.setTimestamp(2, Timestamp.from(record.occurredAt()));
                statement.setString(3, record.category().name());
                statement.setString(4, record.eventType());
                setUuid(statement, 5, record.context().operatorId());
                setUuid(statement, 6, record.targetId());
                statement.setString(7, record.result().name());
                statement.setString(8, record.context().client());
                statement.setString(9, record.context().ip());
                statement.setString(10, record.context().userAgent());
                statement.setString(11, record.context().requestId());
                statement.setString(12, record.context().correlationId());
                statement.setString(13, http.method());
                statement.setString(14, http.url());
                setInteger(statement, 15, http.status());
                setLong(statement, 16, http.durationMs());
                statement.setString(17, before);
                statement.setString(18, after);
                statement.setString(19, record.reason());
                statement.setString(20, failure == null ? null : failure.code());
                statement.setString(21, failure == null ? null : failure.type());
                statement.setString(22, failure == null ? null : failure.reason());
                return statement;
            });
        } catch (RuntimeException exception) {
            if (exception instanceof AuditService.AuditRecordingException recordingException) {
                throw recordingException;
            }
            throw new AuditService.AuditRecordingException("统一审计记录写入失败", exception);
        }
    }

    /**
     * 检查统一表可查询且当前数据库角色拥有 INSERT 权限。
     */
    public void assertAvailable() {
        try {
            jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM " + TABLE + " WHERE false)", Boolean.class);
            Boolean insertAllowed = jdbcTemplate.queryForObject(
                    "SELECT has_table_privilege(current_user, '" + TABLE + "', 'INSERT')", Boolean.class);
            if (!Boolean.TRUE.equals(insertAllowed)) {
                throw new AuditService.AuditRecordingException("统一审计存储不可写");
            }
        } catch (RuntimeException exception) {
            if (exception instanceof AuditService.AuditRecordingException recordingException) {
                throw recordingException;
            }
            throw new AuditService.AuditRecordingException("统一审计存储不可用", exception);
        }
    }

    private static void setUuid(PreparedStatement statement, int index, UUID value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.OTHER);
        } else {
            statement.setObject(index, value);
        }
    }

    private static void setInteger(PreparedStatement statement, int index, Integer value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.SMALLINT);
        } else {
            statement.setInt(index, value);
        }
    }

    private static void setLong(PreparedStatement statement, int index, Long value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.BIGINT);
        } else {
            statement.setLong(index, value);
        }
    }
}
