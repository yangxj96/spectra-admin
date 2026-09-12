/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.query;

import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** Query, detail and export facade over the one partitioned audit table. */
@Service
@RequiredArgsConstructor
public class AuditLogQueryService {

    private static final String TABLE = "spectra_core.sys_audit_event";

    private static final String USER_TABLE = "spectra_core.sys_user";

    private static final int MAX_PAGE_SIZE = 100;

    private static final int MAX_EXPORT_ROWS = 5000;

    private static final String RISKY_SECURITY_EVENT = "(category <> 'SECURITY' OR NOT ("
            + "UPPER(event_type) LIKE '%ROOT%' OR UPPER(event_type) LIKE '%BREAK_GLASS%' "
            + "OR UPPER(event_type) LIKE '%SECURITY%' OR UPPER(event_type) LIKE '%SESSION%' "
            + "OR UPPER(event_type) LIKE '%PASSWORD%' OR UPPER(event_type) LIKE '%AUDIT%'))";

    private static final String PROJECTION = "event_id, occurred_at, category, event_type, operator_id, "
            + "(SELECT NULLIF(BTRIM(audit_operator.real_name), '') FROM " + USER_TABLE
            + " audit_operator WHERE audit_operator.id = operator_id) AS operator_name, target_id, "
            + "client, ip, user_agent, http_method, request_url, http_status, duration_ms, "
            + "before_snapshot::text AS before_snapshot, after_snapshot::text AS after_snapshot, reason, result, "
            + "failure_code, failure_type, failure_reason, correlation_id";

    private final JdbcTemplate jdbcTemplate;
    private final ObjectMapper objectMapper;
    private final AuditVisibilityPolicy visibilityPolicy;
    private final AuditLogMetrics metrics;
    private final TimeMapper timeMapper;
    private final AuditSanitizer auditSanitizer;

    public AuditLogPageVO page(Authentication viewer, PageFrom page, AuditLogQueryFrom query) {
        requireAuthenticated(viewer);
        PageFrom safePage = page == null ? new PageFrom() : page;
        long pageNum = safePage.getPageNum() == null ? 1L : Math.max(1L, safePage.getPageNum());
        long pageSize = safePage.getPageSize() == null
                ? 15L
                : Math.min(MAX_PAGE_SIZE, Math.max(1L, safePage.getPageSize()));
        QueryPlan plan = buildPlan(viewer, query);
        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + TABLE + plan.whereSql(),
                plan.arguments().toArray(), Long.class);
        long offset = Math.max(0L, (pageNum - 1L) * pageSize);
        var arguments = new ArrayList<>(plan.arguments());
        arguments.add(pageSize);
        arguments.add(offset);
        List<AuditLogVO> records = jdbcTemplate.query("SELECT " + PROJECTION + " FROM " + TABLE + plan.whereSql()
                + " ORDER BY occurred_at DESC, event_id DESC LIMIT ? OFFSET ?",
                arguments.toArray(), this::mapVisibleRow);
        recordMetrics("PAGE");
        return new AuditLogPageVO(records, total == null ? 0L : total, pageNum, pageSize);
    }

    public AuditLogVO detail(Authentication viewer, UUID eventId, Instant occurredAt) {
        requireAuthenticated(viewer);
        if (eventId == null || occurredAt == null) {
            throw new DataNotExistException("审计事件不存在");
        }
        QueryPlan plan = buildPlan(viewer, null);
        String where = plan.whereSql().isBlank()
                ? " WHERE event_id = ? AND occurred_at = ?"
                : plan.whereSql() + " AND event_id = ? AND occurred_at = ?";
        var arguments = new ArrayList<>(plan.arguments());
        arguments.add(eventId);
        arguments.add(Timestamp.from(occurredAt));
        List<AuditLogVO> records = jdbcTemplate.query("SELECT " + PROJECTION + " FROM " + TABLE + where,
                arguments.toArray(), this::mapVisibleRow);
        if (records.isEmpty()) {
            throw new DataNotExistException("审计事件不存在或当前主体不可见");
        }
        recordMetrics("DETAIL");
        return records.getFirst();
    }

    public String export(Authentication viewer, AuditLogQueryFrom query) {
        requireAuthenticated(viewer);
        QueryPlan plan = buildPlan(viewer, query);
        var arguments = new ArrayList<>(plan.arguments());
        arguments.add(MAX_EXPORT_ROWS);
        List<AuditLogVO> records = jdbcTemplate.query("SELECT " + PROJECTION + " FROM " + TABLE + plan.whereSql()
                + " ORDER BY occurred_at DESC, event_id DESC LIMIT ?",
                arguments.toArray(), this::mapVisibleRow);
        var csv = new StringBuilder("event_id,occurred_at,category,event_type,operator_id,target_id,client,ip,"
                + "user_agent,http_method,request_url,http_status,duration_ms,before,after,reason,result,"
                + "failure_code,failure_type,failure_reason,correlation_id\n");
        for (AuditLogVO record : records) {
            appendCell(csv, record.eventId());
            appendCell(csv, record.occurredAt());
            appendCell(csv, record.category());
            appendCell(csv, record.eventType());
            appendCell(csv, record.operatorId());
            appendCell(csv, record.targetId());
            appendCell(csv, record.client());
            appendCell(csv, record.ip());
            appendCell(csv, record.userAgent());
            appendCell(csv, record.httpMethod());
            appendCell(csv, record.requestUrl());
            appendCell(csv, record.httpStatus());
            appendCell(csv, record.durationMs());
            appendCell(csv, toJson(record.before()));
            appendCell(csv, toJson(record.after()));
            appendCell(csv, record.reason());
            appendCell(csv, record.result());
            appendCell(csv, record.failureCode());
            appendCell(csv, record.failureType());
            appendCell(csv, record.failureReason());
            appendCell(csv, record.correlationId());
            csv.append('\n');
        }
        recordMetrics("EXPORT");
        return csv.toString();
    }

    private QueryPlan buildPlan(Authentication viewer, AuditLogQueryFrom query) {
        var conditions = new ArrayList<String>();
        var arguments = new ArrayList<>();
        AuditLogQueryFrom filters = query == null ? new AuditLogQueryFrom() : query;
        if (!visibilityPolicy.canViewHighRisk(viewer)) {
            conditions.add(RISKY_SECURITY_EVENT);
            if (!visibilityPolicy.canViewAllNonHighRisk(viewer)) {
                UUID viewerId = visibilityPolicy.viewerId(viewer);
                if (viewerId == null) {
                    conditions.add("category <> 'SECURITY'");
                } else {
                    conditions.add("(category <> 'SECURITY' OR operator_id = ? OR target_id = ?)");
                    arguments.add(viewerId);
                    arguments.add(viewerId);
                }
            }
        }
        if (filters.getCategory() != null) {
            conditions.add("category = ?");
            arguments.add(filters.getCategory().name());
        }
        if (filters.getEventType() != null && !filters.getEventType().isBlank()) {
            String eventType = filters.getEventType().trim();
            conditions.add("(event_type ILIKE ? OR reason ILIKE ?)");
            arguments.add("%" + eventType + "%");
            arguments.add("%" + eventType + "%");
        }
        if (filters.getOperator() != null && !filters.getOperator().isBlank()) {
            String operator = filters.getOperator().trim();
            UUID operatorId = parseUuid(operator);
            String operatorNameCondition = "EXISTS (SELECT 1 FROM " + USER_TABLE
                    + " audit_operator WHERE audit_operator.id = operator_id"
                    + " AND audit_operator.real_name ILIKE ?)";
            if (operatorId == null) {
                conditions.add(operatorNameCondition);
            } else {
                conditions.add("(operator_id = ? OR " + operatorNameCondition + ")");
                arguments.add(operatorId);
            }
            arguments.add("%" + operator + "%");
        }
        if (filters.getTargetId() != null) {
            conditions.add("target_id = ?");
            arguments.add(filters.getTargetId());
        }
        if (filters.getResult() != null) {
            conditions.add("result = ?");
            arguments.add(filters.getResult().name());
        }
        if (filters.getFrom() != null && !filters.getFrom().isBlank()) {
            conditions.add("occurred_at >= ?");
            arguments.add(timeMapper.toInstant(filters.getFrom()));
        }
        if (filters.getTo() != null && !filters.getTo().isBlank()) {
            conditions.add("occurred_at < ?");
            arguments.add(timeMapper.toInstant(filters.getTo()));
        }
        return new QueryPlan(conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions), arguments);
    }

    private AuditLogVO mapVisibleRow(ResultSet resultSet, int ignored) throws SQLException {
        String reason = resultSet.getString("reason");
        String failureReason = resultSet.getString("failure_reason");
        return new AuditLogVO(
                resultSet.getObject("event_id", UUID.class),
                toInstant(resultSet.getTimestamp("occurred_at")),
                com.devops00.spectra.common.audit.AuditCategory.valueOf(resultSet.getString("category")),
                resultSet.getString("event_type"),
                resultSet.getObject("operator_id", UUID.class),
                resultSet.getString("operator_name"),
                resultSet.getObject("target_id", UUID.class),
                resultSet.getString("client"),
                resultSet.getString("ip"),
                resultSet.getString("user_agent"),
                resultSet.getString("http_method"),
                resultSet.getString("request_url"),
                resultSet.getObject("http_status", Integer.class),
                resultSet.getObject("duration_ms", Long.class),
                parseSnapshot(resultSet.getString("before_snapshot")),
                parseSnapshot(resultSet.getString("after_snapshot")),
                sanitizeText("reason", reason),
                AuditRecord.Result.valueOf(resultSet.getString("result")),
                resultSet.getString("failure_code"),
                resultSet.getString("failure_type"),
                sanitizeText("failureReason", failureReason),
                resultSet.getString("correlation_id"));
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> parseSnapshot(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Map<?, ?> parsed = objectMapper.readValue(json, Map.class);
            var normalized = new LinkedHashMap<String, Object>();
            parsed.forEach((key, value) -> normalized.put(String.valueOf(key), value));
            return auditSanitizer.sanitize(normalized);
        } catch (Exception ignored) {
            return Map.of("_redacted", "invalid_snapshot");
        }
    }

    private String sanitizeText(String key, String value) {
        if (value == null) {
            return null;
        }
        Object sanitized = auditSanitizer.sanitize(Map.of(key, value)).get(key);
        return sanitized instanceof String text ? text : null;
    }

    private AuditRecord toRecord(AuditLogVO value) {
        return new AuditRecord(value.eventId(), value.category(), value.eventType(), value.targetId(), value.result(),
                value.occurredAt(),
                new com.devops00.spectra.common.audit.AuditContext(value.operatorId(), null, value.correlationId(),
                        value.client(), value.ip(), value.userAgent()),
                value.before(), value.after(), value.reason(),
                new AuditRecord.HttpSummary(value.httpMethod(), value.requestUrl(), value.httpStatus(), value.durationMs()),
                new AuditRecord.Failure(value.failureCode(), value.failureType(), value.failureReason()));
    }

    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return "{\"_redacted\":\"serialization_failed\"}";
        }
    }

    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }

    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    private static void appendCell(StringBuilder csv, Object value) {
        if (csv.charAt(csv.length() - 1) != '\n') {
            csv.append(',');
        }
        String text = value == null ? "" : String.valueOf(value);
        csv.append('"').append(text.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ")).append('"');
    }

    private static void requireAuthenticated(Authentication viewer) {
        if (viewer == null || !viewer.isAuthenticated()) {
            throw new AccessDeniedException("需要登录后查询审计日志");
        }
    }

    private void recordMetrics(String operation) {
        metrics.recordQuery(operation, AuditRecord.Result.SUCCEEDED.name());
    }

    private record QueryPlan(String whereSql, List<Object> arguments) {
    }
}
