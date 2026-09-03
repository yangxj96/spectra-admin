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
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.core.system.service.OperationLogService;
import com.devops00.spectra.core.security.audit.AuditResult;
import com.devops00.spectra.core.security.audit.SecurityAuditEvent;
import com.devops00.spectra.core.security.audit.SecurityAuditWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * Core 统一审计编排器。
 *
 * <p>Core 是审计语义和 sink 路由的唯一入口。安全事件同步写入不可变安全审计事实，
 * 普通操作事件交给操作日志 sink；操作日志 sink 在当前业务事务内写入 PostgreSQL outbox，
 * 由 Core 的 outbox worker 最终落入 sys_log。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/31
 */
@Service
@RequiredArgsConstructor
public class CoreAuditService implements AuditService {

    private static final String AUDIT_METADATA = "_audit";

    private final SecurityAuditWriter securityAuditWriter;

    private final OperationLogService operationLogService;

    private final AuditSanitizer auditSanitizer;

    /**
     * 清洗统一快照并路由到对应 sink。
     *
     * <p>安全 sink 的异常不被捕获，保证安全审计 fail-closed；操作 sink 的异常同样向上抛出，
     * 使 outbox 写入失败时当前业务事务回滚。</p>
     *
     * @param record 统一审计记录
     */
    @Override
    @Transactional
    public void record(AuditRecord record) {
        AuditRecord accepted = sanitize(record);
        if (accepted.category() == AuditCategory.SECURITY) {
            securityAuditWriter.append(toSecurityEvent(accepted));
            return;
        }
        operationLogService.record(accepted);
    }

    /**
     * 使用唯一的公共脱敏端口准备记录，并把请求追踪元数据写入两类快照。
     */
    private AuditRecord sanitize(AuditRecord record) {
        Objects.requireNonNull(record, "统一审计记录不能为空");
        Map<String, Object> metadata = metadata(record);
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
                record.reason());
    }

    /**
     * 转换为安全审计基础模块使用的不可变事件。
     */
    private SecurityAuditEvent toSecurityEvent(AuditRecord record) {
        AuditContext context = record.context();
        return new SecurityAuditEvent(
                record.eventId(),
                record.eventType(),
                context.operatorId(),
                record.targetId(),
                context.client(),
                context.ip(),
                context.userAgent(),
                record.before(),
                record.after(),
                record.reason(),
                record.occurredAt(),
                AuditResult.valueOf(record.result().name()),
                context.correlationId());
    }

    /**
     * 生成两条 sink 都能保存的稳定追踪元数据。
     */
    private static Map<String, Object> metadata(AuditRecord record) {
        AuditContext context = record.context();
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("eventId", record.eventId().toString());
        metadata.put("eventType", record.eventType());
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

    /**
     * 使用内部保留字段覆盖调用方可能伪造的旧追踪元数据。
     */
    private static Map<String, Object> withMetadata(Map<String, Object> snapshot, Map<String, Object> metadata) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (snapshot != null) {
            result.putAll(snapshot);
        }
        result.put(AUDIT_METADATA, metadata);
        return result;
    }

    private static void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value instanceof java.util.UUID uuid ? uuid.toString() : value);
        }
    }
}
