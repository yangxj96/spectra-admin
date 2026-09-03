/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.audit.outbox;

import com.devops00.spectra.common.port.audit.SecurityAuditArchiveIntegrity;
import com.devops00.spectra.core.security.audit.AuditResult;
import com.devops00.spectra.core.security.audit.SecurityAuditEvent;
import com.devops00.spectra.core.security.audit.SecurityAuditUnavailableException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

import java.nio.charset.StandardCharsets;
import java.time.Instant;

/** 安全配置变更的事务内 outbox 生产者。 */
@Component
@RequiredArgsConstructor
public class SecurityChangeOutboxProducer {

    private final SecurityChangeOutboxRepository repository;

    private final ObjectMapper objectMapper;

    /**
     * 只为成功的安全配置变更产生外部动作事件；登录和审计查询仍只写安全事实表。
     * 调用方必须在安全变更数据库事务内调用本方法。
     */
    public void publish(SecurityAuditEvent event) {
        if (event == null || event.result() != AuditResult.SUCCEEDED || !isDispatchable(event.eventType())) {
            return;
        }
        try {
            // SecurityAuditEvent 的构造器已经完成快照脱敏；直接序列化稳定事件模型，
            // 让 worker 可以无损恢复同一份安全变更事实，避免 map 字段和 record 字段漂移。
            String serialized = objectMapper.writeValueAsString(event);
            String idempotencyKey = SecurityAuditArchiveIntegrity.sha256(serialized.getBytes(StandardCharsets.UTF_8));
            repository.enqueue(event.eventId(), idempotencyKey, event.eventType(), aggregateType(event.eventType()),
                    event.targetId(), serialized, event.correlationId(), Instant.now(), event.operatorId());
        } catch (RuntimeException exception) {
            throw new SecurityAuditUnavailableException("安全变更 outbox 写入失败", exception);
        }
    }

    public static boolean isDispatchable(String eventType) {
        if (eventType == null || eventType.isBlank()) {
            return false;
        }
        return eventType.startsWith("USER_")
                || eventType.startsWith("ROLE_")
                || eventType.startsWith("AUTHORIZATION_")
                || eventType.startsWith("ORGANIZATION_")
                || eventType.equals("SESSION_POLICY_CHANGED")
                || eventType.equals("PASSWORD_POLICY_CHANGED");
    }

    private static String aggregateType(String eventType) {
        if (eventType.startsWith("USER_")) {
            return "USER";
        }
        if (eventType.startsWith("ROLE_") || eventType.startsWith("AUTHORIZATION_")) {
            return "AUTHORIZATION";
        }
        if (eventType.startsWith("ORGANIZATION_")) {
            return "ORGANIZATION";
        }
        return "SECURITY_POLICY";
    }
}
