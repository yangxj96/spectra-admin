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

package com.devops00.spectra.core.security.audit.controller;

import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.security.audit.archive.SecurityAuditArchiveOrchestrator;
import com.devops00.spectra.core.security.audit.archive.SecurityAuditArchiveWorker;
import com.devops00.spectra.core.security.audit.javabean.vo.SecurityAuditPageVO;
import com.devops00.spectra.core.security.audit.javabean.from.SecurityAuditQueryFrom;
import com.devops00.spectra.core.security.audit.service.SecurityAuditQueryService;
import com.devops00.spectra.core.security.audit.javabean.vo.SecurityAuditRetentionVO;
import com.devops00.spectra.core.security.audit.javabean.vo.SecurityAuditVO;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.security.authentication.util.AuthenticationContextUtils;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.util.UUID;

/**
 * Security Audit 只读查询与导出接口。
 * <p>
 * Controller 只声明 Catalog 权限，具体 Root/SYSTEM_ADMIN/普通用户可见性统一由查询策略处理。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@RestController
@RequestMapping("/security/audit")
@RequiredArgsConstructor
@Slf4j
public class SecurityAuditController {

    private final SecurityAuditQueryService queryService;

    private final SecurityAuditArchiveOrchestrator archiveOrchestrator;

    private final SecurityAuditArchiveWorker archiveWorker;

    private final SecurityContextAccessor securityContextAccessor;

    /**
     * 查询或获取目标数据（{@code page}）。
     */
    @Audit("'分页查询安全审计'")
    @GetMapping(value = "/page", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'audit:read')")
    public SecurityAuditPageVO page(PageFrom page, SecurityAuditQueryFrom query, Authentication viewer) {
        return queryService.page(viewer, page, query);
    }

    /**
     * 查询或获取目标数据（{@code detail}）。
     */
    @Audit("'查询安全审计详情'")
    @GetMapping(value = "/{eventId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'audit:read')")
    public SecurityAuditVO detail(@PathVariable UUID eventId, Authentication viewer) {
        return queryService.detail(viewer, eventId);
    }

    /**
     * 处理内部业务逻辑（{@code export}）。
     */
    @Audit("'导出安全审计'")
    @GetMapping(value = "/export", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'audit:export')")
    public ResponseEntity<byte[]> export(SecurityAuditQueryFrom query, Authentication viewer) {
        byte[] content = queryService.export(viewer, query).getBytes(StandardCharsets.UTF_8);
        var headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename("security-audit.csv", StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(content);
    }

    /**
     * 处理内部业务逻辑（{@code retention}）。
     */
    @Audit("'查询安全审计保留策略'")
    @GetMapping(value = "/retention", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'audit:read')")
    public SecurityAuditRetentionVO retention() {
        return queryService.retention();
    }

    /** 创建一个按分区唯一的安全审计归档计划。 */
    @PostMapping(value = "/archive/plan", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public SecurityAuditArchiveOrchestrator.ManifestView planArchive(
                                                                     @RequestParam String partitionName,
                                                                     @RequestParam String rangeStart,
                                                                     @RequestParam String rangeEnd) {
        UUID operatorId = currentOperatorId();
        return archiveOrchestrator.plan(partitionName,
                SecurityAuditArchiveOrchestrator.parseInstant(rangeStart, "rangeStart"),
                SecurityAuditArchiveOrchestrator.parseInstant(rangeEnd, "rangeEnd"), operatorId);
    }

    /** 查询归档 manifest 状态。 */
    @GetMapping(value = "/archive/{manifestId}", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public SecurityAuditArchiveOrchestrator.ManifestView archive(@PathVariable UUID manifestId) {
        return archiveOrchestrator.get(manifestId);
    }

    /** 将 FAILED 归档计划清理旧对象元数据后重新排队。 */
    @PostMapping(value = "/archive/{manifestId}/retry", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public SecurityAuditArchiveOrchestrator.ManifestView retryArchive(@PathVariable UUID manifestId) {
        return archiveOrchestrator.retryFailed(manifestId, currentOperatorId());
    }

    /** 对 VERIFIED 归档申请恢复校验；不会删除源安全审计事实。 */
    @PostMapping(value = "/archive/{manifestId}/restore", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public SecurityAuditArchiveOrchestrator.ManifestView requestArchiveRestore(@PathVariable UUID manifestId) {
        return archiveOrchestrator.requestRestore(manifestId, currentOperatorId());
    }

    /** 立即按 worker 租约执行一次归档对象和源范围校验。 */
    @PostMapping(value = "/archive/{manifestId}/verify", version = "1.0.0")
    @PreAuthorize("hasRole('ROLE_DEV_OPS')")
    public SecurityAuditArchiveOrchestrator.ManifestView verifyArchive(@PathVariable UUID manifestId) {
        return archiveWorker.verifyNow(manifestId, currentOperatorId());
    }

    private UUID currentOperatorId() {
        return AuthenticationContextUtils.requireCurrentUserId(securityContextAccessor);
    }
}
