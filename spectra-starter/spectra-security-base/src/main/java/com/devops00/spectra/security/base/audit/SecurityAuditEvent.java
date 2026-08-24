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

package com.devops00.spectra.security.base.audit;

import java.time.Instant;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 不可变的安全审计事实。
 * <p>
 * 该对象只允许保存脱敏后的 before/after 快照。Access Token、Refresh Token、密码、
 * Provider Secret 和私钥等凭据不得进入快照。
 *
 * @param eventId       事件 ID
 * @param eventType     稳定事件类型
 * @param operatorId    操作者
 * @param targetId      被操作主体
 * @param client        客户端类型
 * @param ip            客户端 IP
 * @param userAgent     客户端标识
 * @param before        变更前脱敏快照
 * @param after         变更后脱敏快照
 * @param reason        操作原因
 * @param occurredAt    事件时间
 * @param result        操作结果
 * @param correlationId 关联请求/事务 ID
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public record SecurityAuditEvent(UUID eventId,
                                 String eventType,
                                 UUID operatorId,
                                 UUID targetId,
                                 String client,
                                 String ip,
                                 String userAgent,
                                 Map<String, Object> before,
                                 Map<String, Object> after,
                                 String reason,
                                 Instant occurredAt,
                                 AuditResult result,
                                 String correlationId) {

    public SecurityAuditEvent {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (eventType == null || eventType.isBlank()) {
            throw new IllegalArgumentException("安全审计事件类型不能为空");
        }
        if (occurredAt == null) {
            occurredAt = Instant.now();
        }
        if (result == null) {
            throw new IllegalArgumentException("安全审计事件结果不能为空");
        }
        before = immutableCopy(SecurityAuditSnapshotSanitizer.sanitize(before));
        after = immutableCopy(SecurityAuditSnapshotSanitizer.sanitize(after));
    }

    @Override
    public Map<String, Object> before() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(before));
    }

    @Override
    public Map<String, Object> after() {
        return Collections.unmodifiableMap(new LinkedHashMap<>(after));
    }

    /**
     * 创建高风险事务的审计预写入事件。
     */
    public SecurityAuditEvent started() {
        return withResult(AuditResult.STARTED);
    }

    /**
     * 使用新的结果创建同一事件的副本。
     */
    public SecurityAuditEvent withResult(AuditResult nextResult) {
        return new SecurityAuditEvent(null, eventType, operatorId, targetId, client, ip, userAgent, before, after,
                reason, occurredAt, nextResult, correlationId);
    }

    /**
     * 转换、解析或规范化数据（{@code immutableCopy}）。
     */
    private static Map<String, Object> immutableCopy(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
