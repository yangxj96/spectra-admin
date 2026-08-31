/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.system.outbox;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.time.Clock;
import java.time.Instant;
import java.util.Objects;

/** 普通操作审计事件的事务内 outbox 写入器。 */
@Component
public class OperationLogOutboxWriter {

    private final OperationLogOutboxRepository repository;
    private final ObjectMapper objectMapper;
    private final Clock clock;

    public OperationLogOutboxWriter(OperationLogOutboxRepository repository,
                                    ObjectMapper objectMapper,
                                    Clock clock) {
        this.repository = repository;
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    /**
     * 将普通操作事件写入当前 PostgreSQL 事务；序列化或数据库失败必须向上抛出。
     */
    public void write(AuditRecord record) {
        Objects.requireNonNull(record, "操作审计记录不能为空");
        if (record.category() != AuditCategory.OPERATION) {
            throw new AuditService.AuditRecordingException("操作日志 outbox 只接受 OPERATION 事件");
        }
        try {
            Instant now = clock.instant();
            String payload = objectMapper.writeValueAsString(record);
            repository.enqueue(record.eventId(), record.eventId().toString(), payload, now, record.context().operatorId());
        } catch (AuditService.AuditRecordingException exception) {
            throw exception;
        } catch (RuntimeException exception) {
            throw new AuditService.AuditRecordingException("操作日志 outbox 写入失败", exception);
        }
    }
}
