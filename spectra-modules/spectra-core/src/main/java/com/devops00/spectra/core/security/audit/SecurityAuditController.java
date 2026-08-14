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

package com.devops00.spectra.core.security.audit;

import com.devops00.spectra.common.base.javabean.from.PageFrom;
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
public class SecurityAuditController {

    private final SecurityAuditQueryService queryService;

    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'audit:read')")
    public SecurityAuditPageVO page(PageFrom page, SecurityAuditQueryFrom query, Authentication viewer) {
        return queryService.page(viewer, page, query);
    }

    @GetMapping(value = "/{eventId}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'audit:read')")
    public SecurityAuditVO detail(@PathVariable UUID eventId, Authentication viewer) {
        return queryService.detail(viewer, eventId);
    }

    @GetMapping(value = "/export", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'audit:export')")
    public ResponseEntity<byte[]> export(SecurityAuditQueryFrom query, Authentication viewer) {
        byte[] content = queryService.export(viewer, query).getBytes(StandardCharsets.UTF_8);
        var headers = new HttpHeaders();
        headers.setContentType(new MediaType("text", "csv", StandardCharsets.UTF_8));
        headers.setContentDisposition(ContentDisposition.attachment().filename("security-audit.csv", StandardCharsets.UTF_8).build());
        return ResponseEntity.ok().headers(headers).body(content);
    }

    @GetMapping(value = "/retention", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'audit:read')")
    public SecurityAuditRetentionVO retention() {
        return queryService.retention();
    }
}
