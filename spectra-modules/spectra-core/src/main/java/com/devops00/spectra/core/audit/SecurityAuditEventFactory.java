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

package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.core.security.audit.AuditResult;
import com.devops00.spectra.core.security.audit.SecurityAuditEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * Core 安全审计事件工厂。
 *
 * <p>所有 Core 生产事件都通过该工厂先清洗原始快照，再创建不可变安全审计事实，避免业务服务
 * 自行选择脱敏策略或直接绕过统一 Bean。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
@Component
@RequiredArgsConstructor
public final class SecurityAuditEventFactory {

    private final AuditSanitizer auditSanitizer;

    /**
     * 使用注入的脱敏器创建安全审计事件。
     *
     * @param eventId       事件 ID；为 null 时由安全审计事件生成新的 UUID
     * @param eventType     稳定的安全审计事件类型；不能为空
     * @param operatorId    执行操作的安全主体 ID；允许为 null 表示无法关联主体
     * @param targetId      被操作的目标主体 ID；没有目标主体时为 null
     * @param client        发起操作的客户端类型；无法识别时为 null
     * @param ip            发起操作的客户端 IP；无法取得时为 null
     * @param userAgent     发起操作的客户端标识；无法取得时为 null
     * @param before        原始变更前快照；为空时保存为空快照
     * @param after         原始变更后快照；为空时保存为空快照
     * @param reason        操作原因或业务说明；没有说明时为 null
     * @param occurredAt    事件发生时间；为 null 时由安全审计事件使用当前时间
     * @param result        事件当前结果；不能为空
     * @param correlationId 关联请求或事务 ID；没有关联上下文时为 null
     * @return 已使用统一策略清洗 before/after 快照、可安全写入事实表的不可变事件
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public SecurityAuditEvent create(UUID eventId,
                                     String eventType,
                                     UUID operatorId,
                                     UUID targetId,
                                     String client,
                                     String ip,
                                     String userAgent,
                                     Map<String, ?> before,
                                     Map<String, ?> after,
                                     String reason,
                                     Instant occurredAt,
                                     AuditResult result,
                                     String correlationId) {
        Map<String, Object> sanitizedBefore = auditSanitizer.sanitize(before);
        Map<String, Object> sanitizedAfter = auditSanitizer.sanitize(after);
        return new SecurityAuditEvent(eventId, eventType, operatorId, targetId, client, ip, userAgent,
                sanitizedBefore, sanitizedAfter, reason, occurredAt, result, correlationId);
    }
}
