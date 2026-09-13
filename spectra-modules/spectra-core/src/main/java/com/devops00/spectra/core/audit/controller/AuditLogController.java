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

package com.devops00.spectra.core.audit.controller;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.audit.javabean.vo.AuditLogPageVO;
import com.devops00.spectra.core.audit.javabean.from.AuditLogQueryFrom;
import com.devops00.spectra.core.audit.service.AuditLogQueryService;
import com.devops00.spectra.core.audit.javabean.vo.AuditLogVO;
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

/**
 * 提供审计日志相关的 HTTP 接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@RestController
@RequestMapping("/audit")
@RequiredArgsConstructor
public class AuditLogController {

    private final AuditLogQueryService queryService;

    /**
     * 按查询条件分页查询审计日志。
     *
     * @param page   分页参数。
     * @param query  查询筛选条件。
     * @param viewer 当前已认证用户信息。
     * @return 符合条件的分页结果。
     */
    @Audit(value = "'分页查询审计日志'", eventType = "AUDIT_LOG_PAGE_VIEWED", category = com.devops00.spectra.common.audit.AuditCategory.SECURITY, captureArguments = false, captureResult = false)
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'audit:read')")
    public AuditLogPageVO page(PageFrom page, AuditLogQueryFrom query, Authentication viewer) {
        return queryService.page(viewer, page, query);
    }

    /**
     * 按查询条件查询审计日志详情。
     *
     * @param eventId    审计事件标识。
     * @param occurredAt 审计事件发生时间，用于定位对应的分区记录。
     * @param viewer     当前已认证用户信息。
     * @return 审计日志数据。
     */
    @Audit(value = "'查询审计日志详情'", eventType = "AUDIT_LOG_DETAIL_VIEWED", category = com.devops00.spectra.common.audit.AuditCategory.SECURITY, captureArguments = false, captureResult = false)
    // RequestGetParamsFilter maps the public snake_case GET parameter to camelCase before binding.
    @GetMapping(value = "/{eventId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'audit:read')")
    public AuditLogVO detail(@PathVariable UUID eventId,
                             @RequestParam("occurredAt") Instant occurredAt,
                             Authentication viewer) {
        return queryService.detail(viewer, eventId, occurredAt);
    }

    /**
     * 查询审计日志的导出数据。
     *
     * @param query  查询筛选条件。
     * @param viewer 当前已认证用户信息。
     * @return 包含处理结果的 HTTP 响应。
     */
    @Audit(value = "'导出审计日志'", eventType = "AUDIT_LOG_EXPORTED", category = com.devops00.spectra.common.audit.AuditCategory.SECURITY, captureArguments = false, captureResult = false)
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
