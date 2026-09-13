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

package com.devops00.spectra.core.audit.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.mybatis.PgJsonbTypeHandler;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Append-only audit fact mapped to the partitioned {@code sys_audit_event} parent table.
 *
 * <p>The table has a composite key and does not use {@code BaseEntity}; writes remain owned by
 * {@code JdbcAuditEventWriter}.</p>
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_audit_event", schema = "spectra_core", autoResultMap = true)
public class AuditEvent {

    @TableField(value = "event_id")
    private UUID eventId;

    @TableField(value = "occurred_at")
    private Instant occurredAt;

    @TableField(value = "category")
    private String category;

    @TableField(value = "event_type")
    private String eventType;

    @TableField(value = "operator_id")
    private UUID operatorId;

    @TableField(value = "target_id")
    private UUID targetId;

    @TableField(value = "result")
    private String result;

    @TableField(value = "client")
    private String client;

    @TableField(value = "ip")
    private String ip;

    @TableField(value = "user_agent")
    private String userAgent;

    @TableField(value = "request_id")
    private String requestId;

    @TableField(value = "correlation_id")
    private String correlationId;

    @TableField(value = "http_method")
    private String httpMethod;

    @TableField(value = "request_url")
    private String requestUrl;

    @TableField(value = "http_status")
    private Short httpStatus;

    @TableField(value = "duration_ms")
    private Long durationMs;

    @TableField(value = "before_snapshot", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> beforeSnapshot;

    @TableField(value = "after_snapshot", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> afterSnapshot;

    @TableField(value = "reason")
    private String reason;

    @TableField(value = "failure_code")
    private String failureCode;

    @TableField(value = "failure_type")
    private String failureType;

    @TableField(value = "failure_reason")
    private String failureReason;
}
