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

package com.devops00.spectra.core.audit.javabean.vo;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 封装审计日志相关的响应数据。
 *
 * @param eventId       审计事件标识
 * @param occurredAt    审计事件发生时间
 * @param category      业务类别
 * @param eventType     事件类型
 * @param operatorId    操作人标识
 * @param operatorName  执行操作的用户名称
 * @param targetId      目标对象标识
 * @param client        发起请求的客户端信息
 * @param ip            发起请求的客户端 IP 地址
 * @param userAgent     发起请求的客户端 User-Agent
 * @param httpMethod    HTTP方法
 * @param requestUrl    请求URL
 * @param httpStatus    HTTP状态
 * @param durationMs    审计操作耗时（毫秒）
 * @param before        变更前的配置值
 * @param after         变更后的配置值
 * @param reason        本次操作或审计事件对应的原因
 * @param result        审计事件记录的操作结果
 * @param failureCode   失败编码
 * @param failureType   失败类型
 * @param failureReason 失败原因
 * @param correlationId 关联标识
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record AuditLogVO(UUID eventId,
                         Instant occurredAt,
                         AuditCategory category,
                         String eventType,
                         UUID operatorId,
                         String operatorName,
                         UUID targetId,
                         String client,
                         String ip,
                         String userAgent,
                         String httpMethod,
                         String requestUrl,
                         Integer httpStatus,
                         Long durationMs,
                         Map<String, Object> before,
                         Map<String, Object> after,
                         String reason,
                         AuditRecord.Result result,
                         String failureCode,
                         String failureType,
                         String failureReason,
                         String correlationId) {

    public AuditLogVO {
        before = immutableMap(before);
        after = immutableMap(after);
    }

    @Override
    public Map<String, Object> before() {
        return immutableMap(before);
    }

    @Override
    public Map<String, Object> after() {
        return immutableMap(after);
    }

    /**
     * 处理审计日志相关数据。
     */
    private static Map<String, Object> immutableMap(Map<String, Object> source) {
        return source == null || source.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
