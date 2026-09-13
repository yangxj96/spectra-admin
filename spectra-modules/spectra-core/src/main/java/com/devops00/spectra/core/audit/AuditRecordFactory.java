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

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

/**
 * 创建显式安全操作的统一审计记录。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Component
@RequiredArgsConstructor
public final class AuditRecordFactory {

    private final AuditSanitizer auditSanitizer;

    /**
     * 创建带有 SECURITY 分类的统一审计记录。
     */
    @SuppressWarnings("PMD.ExcessiveParameterList")
    public AuditRecord create(UUID eventId,
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
                              AuditRecord.Result result,
                              String correlationId) {
        String sanitizedReason = reason;
        if (reason != null) {
            Object sanitized = auditSanitizer.sanitize(Map.of("reason", reason)).get("reason");
            sanitizedReason = sanitized instanceof String value ? value : null;
        }
        return new AuditRecord(eventId, AuditCategory.SECURITY, eventType, targetId, result, occurredAt,
                new AuditContext(operatorId, null, correlationId, client, ip, userAgent),
                auditSanitizer.sanitize(before), auditSanitizer.sanitize(after), sanitizedReason);
    }
}
