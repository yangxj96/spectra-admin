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

import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.core.audit.repository.JdbcAuditEventWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 统一操作与安全审计服务。
 *
 * <p>两类事件都经过同一套快照脱敏逻辑，并在当前业务事务中同步写入统一审计表。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Service
@RequiredArgsConstructor
public class CoreAuditService implements AuditService {

    private static final String AUDIT_METADATA = "_audit";

    private final JdbcAuditEventWriter writer;
    private final AuditSanitizer auditSanitizer;

    /**
     * 对任意分类的审计记录执行同一清洗并写入同一关系表。
     *
     * @param record 统一审计事件
     */
    @Override
    @Transactional
    public void record(AuditRecord record) {
        Objects.requireNonNull(record, "统一审计记录不能为空");
        writer.append(sanitize(record));
    }

    /**
     * 在高风险安全变更开始前确认统一审计表可用。
     */
    @Override
    public void assertAvailable() {
        writer.assertAvailable();
    }

    private AuditRecord sanitize(AuditRecord record) {
        Map<String, Object> metadata = metadata(record);
        AuditRecord.Failure failure = record.failure();
        if (failure != null && failure.reason() != null) {
            String reason = stringValue(auditSanitizer.sanitize(Map.of("failureReason", failure.reason()))
                    .get("failureReason"));
            failure = new AuditRecord.Failure(failure.code(), failure.type(), reason);
        }
        return new AuditRecord(
                record.eventId(),
                record.category(),
                record.eventType(),
                record.targetId(),
                record.result(),
                record.occurredAt(),
                record.context(),
                withMetadata(auditSanitizer.sanitize(record.before()), metadata),
                withMetadata(auditSanitizer.sanitize(record.after()), metadata),
                record.reason(),
                record.httpSummary(),
                failure);
    }

    private static Map<String, Object> metadata(AuditRecord record) {
        AuditContext context = record.context();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("eventId", record.eventId().toString());
        metadata.put("eventType", record.eventType());
        metadata.put("category", record.category().name());
        metadata.put("result", record.result().name());
        metadata.put("occurredAt", record.occurredAt().toString());
        putIfPresent(metadata, "targetId", record.targetId());
        putIfPresent(metadata, "operatorId", context.operatorId());
        putIfPresent(metadata, "requestId", context.requestId());
        putIfPresent(metadata, "correlationId", context.correlationId());
        putIfPresent(metadata, "client", context.client());
        putIfPresent(metadata, "ip", context.ip());
        putIfPresent(metadata, "userAgent", context.userAgent());
        return metadata;
    }

    private static Map<String, Object> withMetadata(Map<String, Object> snapshot, Map<String, Object> metadata) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (snapshot != null) {
            result.putAll(snapshot);
        }
        result.put(AUDIT_METADATA, metadata);
        return result;
    }

    private static String stringValue(Object value) {
        return value instanceof String text ? text : null;
    }

    private static void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value instanceof UUID uuid ? uuid.toString() : value);
        }
    }
}
