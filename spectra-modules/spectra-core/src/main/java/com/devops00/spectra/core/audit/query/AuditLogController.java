/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.query;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.UUID;

/** Unified audit query and export API. */
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogQueryService queryService;

    @Audit(value = "'分页查询审计日志'", eventType = "AUDIT_LOG_PAGE_VIEWED",
            category = com.devops00.spectra.common.audit.AuditCategory.SECURITY,
            captureArguments = false, captureResult = false)
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'audit:read')")
    public AuditLogPageVO page(PageFrom page, AuditLogQueryFrom query, Authentication viewer) {
        return queryService.page(viewer, page, query);
    }

    @Audit(value = "'查询审计日志详情'", eventType = "AUDIT_LOG_DETAIL_VIEWED",
            category = com.devops00.spectra.common.audit.AuditCategory.SECURITY,
            captureArguments = false, captureResult = false)
    @GetMapping(value = "/{eventId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'audit:read')")
    public AuditLogVO detail(@PathVariable UUID eventId,
                             @RequestParam("occurred_at") Instant occurredAt,
                             Authentication viewer) {
        return queryService.detail(viewer, eventId, occurredAt);
    }

    @Audit(value = "'导出审计日志'", eventType = "AUDIT_LOG_EXPORTED",
            category = com.devops00.spectra.common.audit.AuditCategory.SECURITY,
            captureArguments = false, captureResult = false)
    @GetMapping(value = "/export", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'audit:export')")
    public ResponseEntity<byte[]> export(AuditLogQueryFrom query, Authentication viewer) {
        byte[] content = queryService.export(viewer, query).getBytes(StandardCharsets.UTF_8);
        var headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename("audit-log.csv", StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(content);
    }
}
