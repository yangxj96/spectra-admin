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

package com.devops00.spectra.core.security.audit.service;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.audit.DefaultAuditSanitizer;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.security.audit.javabean.from.SecurityAuditQueryFrom;
import com.devops00.spectra.core.security.audit.javabean.vo.SecurityAuditPageVO;
import com.devops00.spectra.core.security.audit.javabean.vo.SecurityAuditRetentionVO;
import com.devops00.spectra.core.security.audit.javabean.vo.SecurityAuditVO;
import com.devops00.spectra.core.security.audit.observability.SecurityAuditMetrics;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.AuditVisibilityPolicy;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
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

/**
 * Security Audit 查询应用服务。
 * <p>
 * 查询 SQL 先按 viewer 做数据库级可见性过滤，结果映射后再经过
 * {@link AuditVisibilityPolicy} 和快照脱敏器校验，避免分页或历史脏数据绕过策略。
 * 该服务只有 SELECT 能力，任何 UPDATE/DELETE/清理操作均不提供应用入口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Service
@RequiredArgsConstructor
public class SecurityAuditQueryService {

    private static final String TABLE = "spectra_security.sec_security_audit_event";

    private static final String RETENTION_TABLE = "spectra_security.sec_security_audit_retention_policy";

    private static final int MAX_PAGE_SIZE = 100;

    private static final int MAX_EXPORT_ROWS = 5000;

    private final JdbcTemplate jdbcTemplate;

    private final ObjectMapper objectMapper;

    private final AuditVisibilityPolicy visibilityPolicy;

    private final SecurityAuditWriter securityAuditWriter;

    private final SecurityAuditMetrics metrics;

    private final TimeMapper timeMapper;

    /**
     * 分页查询审计事件。
     */
    public SecurityAuditPageVO page(Authentication viewer, PageFrom page, SecurityAuditQueryFrom query) {
        requireAuthenticated(viewer);
        PageFrom safePage = page == null ? new PageFrom() : page;
        long pageNum = safePage.getPageNum() == null ? 1L : Math.max(1L, safePage.getPageNum());
        long pageSize = safePage.getPageSize() == null ? 15L : Math.min(MAX_PAGE_SIZE, Math.max(1L, safePage.getPageSize()));
        QueryPlan plan = buildPlan(viewer, query);

        Long total = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM " + TABLE + plan.whereSql(),
                plan.arguments().toArray(), Long.class);
        long offset = Math.max(0L, (pageNum - 1L) * pageSize);
        String sql = "SELECT event_id, event_type, operator_id, target_id, client, ip, user_agent, "
                + "before_snapshot::text AS before_snapshot, after_snapshot::text AS after_snapshot, reason, "
                + "occurred_at, result, correlation_id FROM " + TABLE + plan.whereSql()
                + " ORDER BY occurred_at DESC, event_id DESC LIMIT ? OFFSET ?";
        var arguments = new ArrayList<>(plan.arguments());
        arguments.add(pageSize);
        arguments.add(offset);
        List<SecurityAuditVO> records = jdbcTemplate.query(sql, arguments.toArray(), this::mapVisibleRow)
                .stream()
                .filter(event -> visibilityPolicy.canView(viewer, toEvent(event)))
                .toList();
        recordOperation(viewer, "SECURITY_AUDIT_VIEWED", "PAGE");
        return new SecurityAuditPageVO(records, total == null ? 0L : total, pageNum, pageSize);
    }

    /**
     * 查询单条审计详情。event_id 虽与 occurred_at 共同构成数据库主键，但生产者保证其稳定唯一。
     */
    public SecurityAuditVO detail(Authentication viewer, UUID eventId) {
        requireAuthenticated(viewer);
        if (eventId == null) {
            throw new DataNotExistException("安全审计事件不存在");
        }
        QueryPlan plan = buildPlan(viewer, null);
        String detailWhere = plan.whereSql().isBlank() ? " WHERE event_id = ?" : plan.whereSql() + " AND event_id = ?";
        String sql = "SELECT event_id, event_type, operator_id, target_id, client, ip, user_agent, "
                + "before_snapshot::text AS before_snapshot, after_snapshot::text AS after_snapshot, reason, "
                + "occurred_at, result, correlation_id FROM " + TABLE + detailWhere
                + " ORDER BY occurred_at DESC LIMIT 1";
        var arguments = new ArrayList<>(plan.arguments());
        arguments.add(eventId);
        List<SecurityAuditVO> records = jdbcTemplate.query(sql, arguments.toArray(), this::mapVisibleRow)
                .stream()
                .filter(event -> visibilityPolicy.canView(viewer, toEvent(event)))
                .toList();
        if (records.isEmpty()) {
            throw new DataNotExistException("安全审计事件不存在或当前主体不可见");
        }
        recordOperation(viewer, "SECURITY_AUDIT_VIEWED", "DETAIL");
        return records.getFirst();
    }

    /**
     * 导出当前主体可见的审计 CSV。导出上限固定，避免将审计表当作无边界数据下载接口。
     */
    public String export(Authentication viewer, SecurityAuditQueryFrom query) {
        requireAuthenticated(viewer);
        QueryPlan plan = buildPlan(viewer, query);
        String sql = "SELECT event_id, event_type, operator_id, target_id, client, ip, user_agent, "
                + "before_snapshot::text AS before_snapshot, after_snapshot::text AS after_snapshot, reason, "
                + "occurred_at, result, correlation_id FROM " + TABLE + plan.whereSql()
                + " ORDER BY occurred_at DESC, event_id DESC LIMIT ?";
        var arguments = new ArrayList<>(plan.arguments());
        arguments.add(MAX_EXPORT_ROWS);
        List<SecurityAuditVO> records = jdbcTemplate.query(sql, arguments.toArray(), this::mapVisibleRow)
                .stream()
                .filter(event -> visibilityPolicy.canView(viewer, toEvent(event)))
                .toList();
        var csv = new StringBuilder(
                "event_id,event_type,operator_id,target_id,client,ip,user_agent,before,after,reason,occurred_at,result,correlation_id\n");
        for (SecurityAuditVO record : records) {
            csv.append(csvCell(record.eventId()))
                    .append(',')
                    .append(csvCell(record.eventType()))
                    .append(',')
                    .append(csvCell(record.operatorId()))
                    .append(',')
                    .append(csvCell(record.targetId()))
                    .append(',')
                    .append(csvCell(record.client()))
                    .append(',')
                    .append(csvCell(record.ip()))
                    .append(',')
                    .append(csvCell(record.userAgent()))
                    .append(',')
                    .append(csvCell(toJson(record.before())))
                    .append(',')
                    .append(csvCell(toJson(record.after())))
                    .append(',')
                    .append(csvCell(record.reason()))
                    .append(',')
                    .append(csvCell(record.occurredAt()))
                    .append(',')
                    .append(csvCell(record.result()))
                    .append(',')
                    .append(csvCell(record.correlationId()))
                    .append('\n');
        }
        recordOperation(viewer, "SECURITY_AUDIT_EXPORTED", "EXPORT");
        return csv.toString();
    }

    /**
     * 返回数据库登记的保留/归档策略。此接口只读，策略变化必须由受审计的迁移/运维流程完成。
     */
    public SecurityAuditRetentionVO retention() {
        return jdbcTemplate.queryForObject("SELECT policy_key, hot_retention_months, total_retention_years, "
                + "archive_backend, state, version FROM " + RETENTION_TABLE + " WHERE policy_key = 'DEFAULT'",
                (resultSet, ignored) -> new SecurityAuditRetentionVO(
                        resultSet.getString("policy_key"),
                        resultSet.getInt("hot_retention_months"),
                        resultSet.getInt("total_retention_years"),
                        resultSet.getString("archive_backend"),
                        resultSet.getString("state"),
                        resultSet.getLong("version")));
    }

    /**
     * 创建或构建目标数据（{@code buildPlan}）。
     */
    private QueryPlan buildPlan(Authentication viewer, SecurityAuditQueryFrom query) {
        var conditions = new ArrayList<String>();
        var arguments = new ArrayList<>();
        SecurityAuditQueryFrom safeQuery = query == null ? new SecurityAuditQueryFrom() : query;
        if (!visibilityPolicy.canViewHighRisk(viewer)) {
            conditions.add("NOT (UPPER(event_type) LIKE '%ROOT%' OR UPPER(event_type) LIKE '%BREAK_GLASS%' "
                    + "OR UPPER(event_type) LIKE '%SECURITY%' OR UPPER(event_type) LIKE '%SESSION%' "
                    + "OR UPPER(event_type) LIKE '%PASSWORD%' "
                    + "OR UPPER(event_type) LIKE '%AUDIT%')");
            if (!visibilityPolicy.canViewAllNonHighRisk(viewer)) {
                UUID viewerId = visibilityPolicy.viewerId(viewer);
                if (viewerId == null) {
                    conditions.add("1 = 0");
                } else {
                    conditions.add("(operator_id = ? OR target_id = ?)");
                    arguments.add(viewerId);
                    arguments.add(viewerId);
                }
            }
        }
        if (safeQuery.getEventType() != null && !safeQuery.getEventType().isBlank()) {
            conditions.add("event_type = ?");
            arguments.add(safeQuery.getEventType().trim());
        }
        if (safeQuery.getOperatorId() != null) {
            conditions.add("operator_id = ?");
            arguments.add(safeQuery.getOperatorId());
        }
        if (safeQuery.getTargetId() != null) {
            conditions.add("target_id = ?");
            arguments.add(safeQuery.getTargetId());
        }
        if (safeQuery.getResult() != null) {
            conditions.add("result = ?");
            arguments.add(safeQuery.getResult().name());
        }
        if (safeQuery.getFrom() != null && !safeQuery.getFrom().isBlank()) {
            conditions.add("occurred_at >= ?");
            arguments.add(timeMapper.toInstant(safeQuery.getFrom()));
        }
        if (safeQuery.getTo() != null && !safeQuery.getTo().isBlank()) {
            conditions.add("occurred_at < ?");
            arguments.add(timeMapper.toInstant(safeQuery.getTo()));
        }
        String where = conditions.isEmpty() ? "" : " WHERE " + String.join(" AND ", conditions);
        return new QueryPlan(where, arguments);
    }

    /**
     * 转换、解析或规范化数据（{@code mapVisibleRow}）。
     */
    private SecurityAuditVO mapVisibleRow(ResultSet resultSet, int ignored) throws SQLException {
        return new SecurityAuditVO(
                resultSet.getObject("event_id", UUID.class),
                resultSet.getString("event_type"),
                resultSet.getObject("operator_id", UUID.class),
                resultSet.getObject("target_id", UUID.class),
                resultSet.getString("client"),
                resultSet.getString("ip"),
                resultSet.getString("user_agent"),
                parseSnapshot(resultSet.getString("before_snapshot")),
                parseSnapshot(resultSet.getString("after_snapshot")),
                resultSet.getString("reason"),
                timeMapper.toLocalDateTime(toInstant(resultSet.getTimestamp("occurred_at"))),
                AuditResult.valueOf(resultSet.getString("result")),
                resultSet.getString("correlation_id"));
    }

    /**
     * 转换、解析或规范化数据（{@code parseSnapshot}）。
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> parseSnapshot(String json) {
        if (json == null || json.isBlank()) {
            return Map.of();
        }
        try {
            Map<?, ?> parsed = objectMapper.readValue(json, Map.class);
            var normalized = new LinkedHashMap<String, Object>();
            parsed.forEach((key, value) -> normalized.put(String.valueOf(key), value));
            return DefaultAuditSanitizer.INSTANCE.sanitize(normalized);
        } catch (Exception ignored) {
            return Map.of("_redacted", "invalid_snapshot");
        }
    }

    /**
     * 转换、解析或规范化数据（{@code toEvent}）。
     */
    private SecurityAuditEvent toEvent(SecurityAuditVO value) {
        return new SecurityAuditEvent(value.eventId(), value.eventType(), value.operatorId(), value.targetId(), value.client(),
                value.ip(), value.userAgent(), value.before(), value.after(), value.reason(), timeMapper.toInstant(value.occurredAt()),
                value.result(),
                value.correlationId());
    }

    /**
     * 转换、解析或规范化数据（{@code toInstant}）。
     */
    private static Instant toInstant(Timestamp timestamp) {
        return timestamp == null ? Instant.EPOCH : timestamp.toInstant();
    }

    /**
     * 转换、解析或规范化数据（{@code toJson}）。
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return "{\"_redacted\":\"serialization_failed\"}";
        }
    }

    /**
     * 处理内部业务逻辑（{@code csvCell}）。
     */
    private static String csvCell(Object value) {
        String text = value == null ? "" : String.valueOf(value);
        return "\"" + text.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ") + "\"";
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireAuthenticated}）。
     */
    private static void requireAuthenticated(Authentication viewer) {
        if (viewer == null || !viewer.isAuthenticated()) {
            throw new AccessDeniedException("需要登录后查询安全审计");
        }
    }

    /**
     * 更新或推进目标状态（{@code recordOperation}）。
     */
    private void recordOperation(Authentication viewer, String eventType, String operation) {
        metrics.recordQuery(operation, AuditResult.SUCCEEDED.name());
        var operatorId = visibilityPolicy.viewerId(viewer);
        securityAuditWriter.append(new SecurityAuditEvent(UUID.randomUUID(), eventType, operatorId, null, null, null, null,
                Map.of("operation", operation), Map.of(), null, null, AuditResult.SUCCEEDED,
                RequestCorrelationContext.current().correlationId()));
    }

    private record QueryPlan(String whereSql, List<Object> arguments) {
    }
}
