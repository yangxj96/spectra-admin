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

package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.core.system.javabean.entity.OperationLog;
import com.devops00.spectra.core.system.javabean.enums.SysLogType;
import com.devops00.spectra.core.system.mapper.OperationLogMapper;
import com.devops00.spectra.core.system.service.OperationLogService;
import com.devops00.spectra.core.system.outbox.OperationLogOutboxWriter;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;

/**
 * 操作日志service层-实现
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Service
public class OperationLogServiceImpl extends BaseServiceImpl<OperationLogMapper, OperationLog> implements OperationLogService {

    private static final String AUDIT_METADATA = "_audit";

    private final OperationLogOutboxWriter outboxWriter;

    public OperationLogServiceImpl(OperationLogOutboxWriter outboxWriter) {
        this.outboxWriter = outboxWriter;
    }

    /**
     * 将统一操作审计记录写入 PostgreSQL outbox。
     *
     * <p>该调用和业务写入共享当前事务；只有 outbox 写入成功，业务事务才允许提交。</p>
     *
     * @param record 统一操作审计记录
     */
    @Override
    public void record(AuditRecord record) {
        outboxWriter.write(Objects.requireNonNull(record, "操作审计记录不能为空"));
    }

    /**
     * 将 outbox 中的操作审计记录幂等写入正式操作日志表。
     *
     * @param record 已从 outbox 读取的操作审计记录
     */
    @Override
    public void persist(AuditRecord record) {
        Objects.requireNonNull(record, "操作审计记录不能为空");
        OperationLog entity = toEntity(record);
        if (!saveIdempotently(entity)) {
            throw new AuditService.AuditRecordingException("操作日志 sys_log 幂等写入失败");
        }
    }

    /**
     * 映射统一审计记录；数据库唯一约束保证重复消费不会生成第二条记录。
     */
    private static OperationLog toEntity(AuditRecord record) {
        OperationLog entity = new OperationLog();
        entity.setId(record.eventId());
        entity.setCreatedBy(record.context().operatorId());
        entity.setCreatedAt(record.occurredAt());
        entity.setUpdatedBy(record.context().operatorId());
        entity.setUpdatedAt(record.occurredAt());
        entity.setType(SysLogType.GENERAL);
        entity.setExplain(explain(record));
        entity.setStatus(shortValue(record.after().get("status")));
        entity.setIp(record.context().ip());
        entity.setMethod(requestValue(record.before(), "method"));
        entity.setUrl(requestValue(record.before(), "url"));
        entity.setArgs(withMetadata(record.before(), record));
        entity.setResult(withMetadata(record.after(), record));
        entity.setTimeCost(longValue(record.after().get("durationMs")));
        entity.setOutboxEventId(record.eventId());
        entity.setVersion(0L);
        return entity;
    }

    /**
     * 通过 mapper 的 ON CONFLICT 语句执行幂等插入；0 行表示已存在且视为成功。
     */
    protected boolean saveIdempotently(OperationLog entity) {
        return baseMapper.insertIfAbsent(entity) >= 0;
    }

    private static String explain(AuditRecord record) {
        return record.reason() == null || record.reason().isBlank() ? record.eventType() : record.reason();
    }

    private static String requestValue(Map<String, Object> snapshot, String key) {
        Object direct = snapshot.get(key);
        if (direct == null && snapshot.get("request") instanceof Map<?, ?> request) {
            direct = request.get(key);
        }
        return direct == null ? null : String.valueOf(direct);
    }

    private static Short shortValue(Object value) {
        if (!(value instanceof Number number)) {
            return null;
        }
        long normalized = number.longValue();
        if (normalized < Short.MIN_VALUE || normalized > Short.MAX_VALUE) {
            return null;
        }
        return (short) normalized;
    }

    private static Long longValue(Object value) {
        return value instanceof Number number ? number.longValue() : null;
    }

    private static Map<String, Object> withMetadata(Map<String, Object> snapshot, AuditRecord record) {
        Map<String, Object> result = new LinkedHashMap<>();
        result.putAll(snapshot);
        Map<String, Object> metadata = new LinkedHashMap<>();
        metadata.put("eventId", record.eventId().toString());
        metadata.put("eventType", record.eventType());
        metadata.put("result", record.result().name());
        metadata.put("occurredAt", record.occurredAt().toString());
        putIfPresent(metadata, "targetId", record.targetId());
        putIfPresent(metadata, "operatorId", record.context().operatorId());
        putIfPresent(metadata, "requestId", record.context().requestId());
        putIfPresent(metadata, "correlationId", record.context().correlationId());
        putIfPresent(metadata, "client", record.context().client());
        putIfPresent(metadata, "userAgent", record.context().userAgent());
        result.put(AUDIT_METADATA, metadata);
        return result;
    }

    private static void putIfPresent(Map<String, Object> target, String key, Object value) {
        if (value != null) {
            target.put(key, value instanceof java.util.UUID uuid ? uuid.toString() : value);
        }
    }
}
