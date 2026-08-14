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

package com.devops00.spectra.core.security.audit;

import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditUnavailableException;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.Types;

/**
 * PostgreSQL Security Audit 追加写入器。
 * <p>
 * 表和最小数据库权限由目标 Flyway schema 提供；没有表、连接或写权限时直接 fail-closed。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Repository
@RequiredArgsConstructor
public class JdbcSecurityAuditWriter implements SecurityAuditWriter {

    private static final String TABLE = "spectra_security.security_audit_event";

    private static final String INSERT_SQL = "INSERT INTO " + TABLE
            + " (event_id, event_type, operator_id, target_id, client, ip, user_agent, before_snapshot, after_snapshot, reason, occurred_at, result, correlation_id)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?)";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void assertAvailable() {
        try {
            jdbcTemplate.queryForObject("SELECT EXISTS (SELECT 1 FROM " + TABLE + ")", Boolean.class);
        } catch (RuntimeException exception) {
            throw unavailable("Security Audit 存储不可用", exception);
        }
    }

    @Override
    public void append(SecurityAuditEvent event) {
        if (event == null) {
            throw unavailable("Security Audit 事件不能为空", null);
        }
        try {
            String before = objectMapper.writeValueAsString(event.before());
            String after = objectMapper.writeValueAsString(event.after());
            jdbcTemplate.update(connection -> {
                PreparedStatement statement = connection.prepareStatement(INSERT_SQL);
                setUuid(statement, 1, event.eventId());
                statement.setString(2, event.eventType());
                setUuid(statement, 3, event.operatorId());
                setUuid(statement, 4, event.targetId());
                statement.setString(5, event.client());
                statement.setString(6, event.ip());
                statement.setString(7, event.userAgent());
                statement.setString(8, before);
                statement.setString(9, after);
                statement.setString(10, event.reason());
                statement.setObject(11, event.occurredAt());
                statement.setString(12, event.result().name());
                statement.setString(13, event.correlationId());
                return statement;
            });
        } catch (RuntimeException exception) {
            throw unavailable("Security Audit 写入失败", exception);
        }
    }

    private static void setUuid(PreparedStatement statement, int index, java.util.UUID value) throws java.sql.SQLException {
        if (value == null) {
            statement.setNull(index, Types.OTHER);
        } else {
            statement.setObject(index, value);
        }
    }

    private static SecurityAuditUnavailableException unavailable(String message, Throwable cause) {
        return cause == null ? new SecurityAuditUnavailableException(message) : new SecurityAuditUnavailableException(message, cause);
    }
}
