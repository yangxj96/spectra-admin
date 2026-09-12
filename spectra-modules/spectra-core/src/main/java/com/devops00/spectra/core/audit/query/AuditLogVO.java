/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.query;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/** Audit row view. occurredAt is required to address the partitioned primary key. */
public record AuditLogVO(UUID eventId,
                         LocalDateTime occurredAt,
                         AuditCategory category,
                         String eventType,
                         UUID operatorId,
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

    private static Map<String, Object> immutableMap(Map<String, Object> source) {
        return source == null || source.isEmpty()
                ? Map.of()
                : Collections.unmodifiableMap(new LinkedHashMap<>(source));
    }
}
