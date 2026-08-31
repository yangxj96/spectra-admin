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

package com.devops00.spectra.core.security.audit.service;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 归档运维适配器的最高等级审计链。
 * <p>
 * 归档 backend 由部署侧选择（必须具备不可变对象/完整性校验能力）；该适配器只负责在
 * planned/started/completed/failed/verified/restored 边界写不可变 Security Audit。它不提供分区卸载、
 * 删除或修改 manifest 的应用权限，因此归档失败不会触发热数据提前移除。
 */
@Component
@RequiredArgsConstructor
public class SecurityAuditArchiveAuditTrail {

    private static final Set<String> ARCHIVE_EVENTS = Set.of(
            "SECURITY_AUDIT_ARCHIVE_PLANNED",
            "SECURITY_AUDIT_ARCHIVE_RETRY_REQUESTED",
            "SECURITY_AUDIT_ARCHIVE_RESTORE_REQUESTED",
            "SECURITY_AUDIT_ARCHIVE_STARTED",
            "SECURITY_AUDIT_ARCHIVE_COMPLETED",
            "SECURITY_AUDIT_ARCHIVE_FAILED",
            "SECURITY_AUDIT_ARCHIVE_VERIFIED",
            "SECURITY_AUDIT_ARCHIVE_RESTORED");

    private final AuditService auditService;

    /**
     * 更新或推进目标状态（{@code append}）。
     */
    public void append(String eventType, UUID operatorId, String partitionName, String detail) {
        if (!ARCHIVE_EVENTS.contains(eventType)) {
            throw new IllegalArgumentException("非法的审计归档事件类型");
        }
        String safeDetail = safeDetail(detail);
        Map<String, Object> snapshot = Map.of(
                "partitionName", partitionName == null ? "UNKNOWN" : partitionName,
                "detail", safeDetail);
        auditService.record(new AuditRecord(UUID.randomUUID(), AuditCategory.SECURITY, eventType, null,
                resultFor(eventType), null, new AuditContext(operatorId, null, null, "OPS", null, null),
                Map.of(), snapshot, safeDetail));
    }

    /**
     * 处理内部业务逻辑（{@code resultFor}）。
     */
    private static AuditRecord.Result resultFor(String eventType) {
        return switch (eventType) {
            case "SECURITY_AUDIT_ARCHIVE_PLANNED",
                    "SECURITY_AUDIT_ARCHIVE_RETRY_REQUESTED",
                    "SECURITY_AUDIT_ARCHIVE_RESTORE_REQUESTED",
                    "SECURITY_AUDIT_ARCHIVE_STARTED" ->
                AuditRecord.Result.STARTED;
            case "SECURITY_AUDIT_ARCHIVE_FAILED" -> AuditRecord.Result.FAILED;
            case "SECURITY_AUDIT_ARCHIVE_COMPLETED",
                    "SECURITY_AUDIT_ARCHIVE_VERIFIED",
                    "SECURITY_AUDIT_ARCHIVE_RESTORED" ->
                AuditRecord.Result.SUCCEEDED;
            default -> throw new IllegalArgumentException("非法的审计归档事件类型");
        };
    }

    private static String safeDetail(String detail) {
        if (detail == null || detail.isBlank()) {
            return "";
        }
        String normalized = detail.replaceAll("[\\r\\n\\t]+", " ").trim();
        return normalized.length() <= 500 ? normalized : normalized.substring(0, 500);
    }
}
