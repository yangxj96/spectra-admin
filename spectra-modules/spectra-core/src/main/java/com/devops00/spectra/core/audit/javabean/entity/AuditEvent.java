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
 * 按事件发生时间分区的只追加审计事件实体，直接映射 {@code sys_audit_event} 主表。
 * 表以事件 ID 和发生时间组成复合主键；持久化写入由 {@code JdbcAuditEventWriter} 负责。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName(value = "sys_audit_event", schema = "spectra_core", autoResultMap = true)
public class AuditEvent {

    /**
     * 审计事件唯一标识；与发生时间共同组成分区表主键。
     */
    @TableField(value = "event_id")
    private UUID eventId;

    /**
     * 审计事件发生时间，也是分区路由和时间范围查询的依据。
     */
    @TableField(value = "occurred_at")
    private Instant occurredAt;

    /**
     * 审计事件所属分类，用于区分操作、安全等审计场景。
     */
    @TableField(value = "category")
    private String category;

    /**
     * 审计事件的具体类型，用于识别记录的业务动作。
     */
    @TableField(value = "event_type")
    private String eventType;

    /**
     * 触发审计事件的用户 ID；系统自动产生的事件可为空。
     */
    @TableField(value = "operator_id")
    private UUID operatorId;

    /**
     * 审计事件所作用的目标对象 ID；没有关联目标时可为空。
     */
    @TableField(value = "target_id")
    private UUID targetId;

    /**
     * 本次操作或安全事件的处理结果。
     */
    @TableField(value = "result")
    private String result;

    /**
     * 发起请求的客户端类型或来源标识。
     */
    @TableField(value = "client")
    private String client;

    /**
     * 发起请求的客户端 IP 地址。
     */
    @TableField(value = "ip")
    private String ip;

    /**
     * 请求携带的客户端 User-Agent 信息。
     */
    @TableField(value = "user_agent")
    private String userAgent;

    /**
     * 标识当前请求的请求 ID，用于关联请求日志。
     */
    @TableField(value = "request_id")
    private String requestId;

    /**
     * 跨组件或服务关联同一业务调用的追踪 ID。
     */
    @TableField(value = "correlation_id")
    private String correlationId;

    /**
     * 触发事件的 HTTP 请求方法。
     */
    @TableField(value = "http_method")
    private String httpMethod;

    /**
     * 触发事件的请求路径。
     */
    @TableField(value = "request_url")
    private String requestUrl;

    /**
     * 请求处理后返回的 HTTP 状态码。
     */
    @TableField(value = "http_status")
    private Short httpStatus;

    /**
     * 请求或操作从开始到结束的耗时，单位为毫秒。
     */
    @TableField(value = "duration_ms")
    private Long durationMs;

    /**
     * 操作前的脱敏状态快照，以 JSONB 格式保存。
     */
    @TableField(value = "before_snapshot", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> beforeSnapshot;

    /**
     * 操作后的脱敏状态快照，以 JSONB 格式保存。
     */
    @TableField(value = "after_snapshot", typeHandler = PgJsonbTypeHandler.class)
    private Map<String, Object> afterSnapshot;

    /**
     * 与本次审计事件关联的业务原因说明。
     */
    @TableField(value = "reason")
    private String reason;

    /**
     * 失败时记录的稳定错误代码。
     */
    @TableField(value = "failure_code")
    private String failureCode;

    /**
     * 失败时记录的异常或失败类别。
     */
    @TableField(value = "failure_type")
    private String failureType;

    /**
     * 失败原因的安全摘要，不保存原始异常详情。
     */
    @TableField(value = "failure_reason")
    private String failureReason;
}
