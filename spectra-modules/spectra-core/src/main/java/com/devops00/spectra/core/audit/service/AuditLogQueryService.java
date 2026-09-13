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

package com.devops00.spectra.core.audit.service;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.audit.javabean.domain.AuditLogQueryCriteria;
import com.devops00.spectra.core.audit.javabean.domain.AuditLogQueryRow;
import com.devops00.spectra.core.audit.javabean.from.AuditLogQueryFrom;
import com.devops00.spectra.core.audit.javabean.vo.AuditLogPageVO;
import com.devops00.spectra.core.audit.javabean.vo.AuditLogVO;
import com.devops00.spectra.core.audit.mapper.AuditLogQueryMapper;
import com.devops00.spectra.core.audit.observability.AuditLogMetrics;
import com.devops00.spectra.core.audit.policy.AuditVisibilityPolicy;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * 定义审计日志查询相关的应用服务契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
@RequiredArgsConstructor
public class AuditLogQueryService {

    private static final int MAX_PAGE_SIZE = 100;

    private static final int MAX_EXPORT_ROWS = 5000;

    private final AuditLogQueryMapper mapper;
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
        AuditLogQueryCriteria criteria = buildCriteria(viewer, query);
        Long total = mapper.countPage(criteria);
        long offset = Math.max(0L, (pageNum - 1L) * pageSize);
        List<AuditLogVO> records = mapper.selectPage(criteria, pageSize, offset)
                .stream()
                .map(this::toView)
                .toList();
        recordMetrics("PAGE");
        return new AuditLogPageVO(records, total == null ? 0L : total, pageNum, pageSize);
    }

    public AuditLogVO detail(Authentication viewer, UUID eventId, Instant occurredAt) {
        requireAuthenticated(viewer);
        if (eventId == null || occurredAt == null) {
            throw new DataNotExistException("审计事件不存在");
        }
        AuditLogQueryRow row = mapper.selectDetail(buildCriteria(viewer, null), eventId, occurredAt);
        if (row == null) {
            throw new DataNotExistException("审计事件不存在或当前主体不可见");
        }
        recordMetrics("DETAIL");
        return toView(row);
    }

    public String export(Authentication viewer, AuditLogQueryFrom query) {
        requireAuthenticated(viewer);
        List<AuditLogVO> records = mapper.selectExport(buildCriteria(viewer, query), MAX_EXPORT_ROWS)
                .stream()
                .map(this::toView)
                .toList();
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

    /**
     * 构建筛选条件。
     */
    private AuditLogQueryCriteria buildCriteria(Authentication viewer, AuditLogQueryFrom query) {
        AuditLogQueryCriteria criteria = new AuditLogQueryCriteria();
        criteria.setCanViewHighRisk(visibilityPolicy.canViewHighRisk(viewer));
        criteria.setCanViewAllNonHighRisk(visibilityPolicy.canViewAllNonHighRisk(viewer));
        criteria.setViewerId(visibilityPolicy.viewerId(viewer));
        AuditLogQueryFrom filters = query == null ? new AuditLogQueryFrom() : query;
        if (filters.getCategory() != null) {
            criteria.setCategory(filters.getCategory().name());
        }
        if (filters.getEventType() != null && !filters.getEventType().isBlank()) {
            criteria.setEventTypePattern("%" + filters.getEventType().trim() + "%");
        }
        if (filters.getOperator() != null && !filters.getOperator().isBlank()) {
            String operator = filters.getOperator().trim();
            criteria.setOperatorId(parseUuid(operator));
            criteria.setOperatorPattern("%" + operator + "%");
        }
        criteria.setTargetId(filters.getTargetId());
        if (filters.getResult() != null) {
            criteria.setResult(filters.getResult().name());
        }
        if (filters.getFrom() != null && !filters.getFrom().isBlank()) {
            criteria.setFrom(timeMapper.toInstant(filters.getFrom()));
        }
        if (filters.getTo() != null && !filters.getTo().isBlank()) {
            criteria.setTo(timeMapper.toInstant(filters.getTo()));
        }
        return criteria;
    }

    /**
     * 转换视图。
     */
    private AuditLogVO toView(AuditLogQueryRow row) {
        return new AuditLogVO(
                row.getEventId(),
                row.getOccurredAt() == null ? Instant.EPOCH : row.getOccurredAt(),
                AuditCategory.valueOf(row.getCategory()),
                row.getEventType(),
                row.getOperatorId(),
                row.getOperatorName(),
                row.getTargetId(),
                row.getClient(),
                row.getIp(),
                row.getUserAgent(),
                row.getHttpMethod(),
                row.getRequestUrl(),
                row.getHttpStatus(),
                row.getDurationMs(),
                parseSnapshot(row.getBeforeSnapshot()),
                parseSnapshot(row.getAfterSnapshot()),
                sanitizeText("reason", row.getReason()),
                AuditRecord.Result.valueOf(row.getResult()),
                row.getFailureCode(),
                row.getFailureType(),
                sanitizeText("failureReason", row.getFailureReason()),
                row.getCorrelationId());
    }

    /**
     * 解析快照。
     */
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

    /**
     * 对审计日志查询执行脱敏处理。
     */
    private String sanitizeText(String key, String value) {
        if (value == null) {
            return null;
        }
        Object sanitized = auditSanitizer.sanitize(Map.of(key, value)).get(key);
        return sanitized instanceof String text ? text : null;
    }

    /**
     * 转换JSON。
     */
    private String toJson(Object value) {
        try {
            return objectMapper.writeValueAsString(value);
        } catch (Exception ignored) {
            return "{\"_redacted\":\"serialization_failed\"}";
        }
    }

    /**
     * 解析UUID。
     */
    private static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException ignored) {
            return null;
        }
    }

    /**
     * 处理审计日志查询相关数据。
     */
    private static void appendCell(StringBuilder csv, Object value) {
        if (csv.charAt(csv.length() - 1) != '\n') {
            csv.append(',');
        }
        String text = value == null ? "" : String.valueOf(value);
        csv.append('"').append(text.replace("\"", "\"\"").replace("\r", " ").replace("\n", " ")).append('"');
    }

    /**
     * 校验已认证状态。
     */
    private static void requireAuthenticated(Authentication viewer) {
        if (viewer == null || !viewer.isAuthenticated()) {
            throw new AccessDeniedException("需要登录后查询审计日志");
        }
    }

    /**
     * 记录指标。
     */
    private void recordMetrics(String operation) {
        metrics.recordQuery(operation, AuditRecord.Result.SUCCEEDED.name());
    }
}
