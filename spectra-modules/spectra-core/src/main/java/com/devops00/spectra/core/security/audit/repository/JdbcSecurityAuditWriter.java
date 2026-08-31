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

package com.devops00.spectra.core.security.audit.repository;

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditUnavailableException;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import tools.jackson.databind.ObjectMapper;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Map;
import java.util.UUID;

/**
 * PostgreSQL Security Audit 追加写入器。
 * <p>
 * 表和最小数据库权限由目标 Flyway schema 提供；没有表、连接或写权限时直接 fail-closed。
 * 该写入器只负责同步写入不可变的安全审计事实表，不负责投递安全变更 outbox，
 * 也不把安全事实降级为普通操作日志；需要外部动作的成功安全变更由 Core 业务事务显式调用
 * {@code SecurityChangeOutboxProducer}，从而保证事实表与 outbox 同事务提交。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Repository
@RequiredArgsConstructor
public class JdbcSecurityAuditWriter implements SecurityAuditWriter {

    private static final String TABLE = "spectra_security.sec_security_audit_event";

    private static final String INSERT_SQL = "INSERT INTO " + TABLE
            + " (event_id, event_type, operator_id, target_id, client, ip, user_agent, before_snapshot, after_snapshot, reason, occurred_at, result, correlation_id, created_by, created_at, updated_by, updated_at, version)"
            + " VALUES (?, ?, ?, ?, ?, ?, ?, ?::jsonb, ?::jsonb, ?, ?, ?, ?, ?, ?, ?, ?, 0)";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final ObjectProvider<AuditSanitizer> auditSanitizer;

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
            String before = objectMapper.writeValueAsString(sanitize(event.before()));
            String after = objectMapper.writeValueAsString(sanitize(event.after()));
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
                // PostgreSQL's driver does not accept java.time.Instant directly for
                // this column. Timestamp.from preserves the instant and is compatible
                // with the TIMESTAMP WITH TIME ZONE audit column.
                Timestamp occurredAt = Timestamp.from(event.occurredAt());
                statement.setTimestamp(11, occurredAt);
                statement.setString(12, event.result().name());
                statement.setString(13, event.correlationId());
                setUuid(statement, 14, event.operatorId());
                statement.setTimestamp(15, occurredAt);
                setUuid(statement, 16, event.operatorId());
                statement.setTimestamp(17, occurredAt);
                return statement;
            });
        } catch (RuntimeException exception) {
            throw unavailable("Security Audit 写入失败", exception);
        }
    }

    /**
     * 更新或推进目标状态（{@code setUuid}）。
     */
    private static void setUuid(PreparedStatement statement, int index, UUID value) throws SQLException {
        if (value == null) {
            statement.setNull(index, Types.OTHER);
        } else {
            statement.setObject(index, value);
        }
    }

    /**
     * 处理内部业务逻辑（{@code unavailable}）。
     */
    private static SecurityAuditUnavailableException unavailable(String message, Throwable cause) {
        return cause == null ? new SecurityAuditUnavailableException(message) : new SecurityAuditUnavailableException(message, cause);
    }

    /**
     * 兼容直接使用旧安全写入端口的调用方，同时让进入数据库的快照统一经过 Core 约定的脱敏器。
     * 没有 Starter 脱敏 Bean 的独立安全上下文沿用 {@link SecurityAuditEvent} 已完成的安全快照。
     */
    private Map<String, Object> sanitize(Map<String, Object> snapshot) {
        var sanitizer = auditSanitizer.getIfAvailable();
        return sanitizer == null ? snapshot : sanitizer.sanitize(snapshot);
    }
}
