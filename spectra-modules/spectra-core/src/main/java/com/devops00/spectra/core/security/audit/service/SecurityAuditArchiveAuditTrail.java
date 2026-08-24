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

import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 归档运维适配器的最高等级审计链。
 * <p>
 * 归档 backend 由部署侧选择（必须具备不可变对象/完整性校验能力）；该适配器只负责在
 * started/completed/failed/verified 四个边界写不可变 Security Audit。它不提供分区卸载、
 * 删除或修改 manifest 的应用权限，因此归档失败不会触发热数据提前移除。
 */
@Component
@RequiredArgsConstructor
public class SecurityAuditArchiveAuditTrail {

    private static final Set<String> ARCHIVE_EVENTS = Set.of(
            "SECURITY_AUDIT_ARCHIVE_STARTED",
            "SECURITY_AUDIT_ARCHIVE_COMPLETED",
            "SECURITY_AUDIT_ARCHIVE_FAILED",
            "SECURITY_AUDIT_ARCHIVE_VERIFIED");

    private final SecurityAuditWriter securityAuditWriter;

    /**
     * 更新或推进目标状态（{@code append}）。
     */
    public void append(String eventType, UUID operatorId, String partitionName, String detail) {
        if (!ARCHIVE_EVENTS.contains(eventType)) {
            throw new IllegalArgumentException("非法的审计归档事件类型");
        }
        Map<String, Object> snapshot = Map.of(
                "partitionName", partitionName == null ? "UNKNOWN" : partitionName,
                "detail", detail == null ? "" : detail);
        securityAuditWriter.append(new SecurityAuditEvent(UUID.randomUUID(), eventType, operatorId, null, "OPS", null, null,
                Map.of(), snapshot, null, null, resultFor(eventType), null));
    }

    /**
     * 处理内部业务逻辑（{@code resultFor}）。
     */
    private static AuditResult resultFor(String eventType) {
        return switch (eventType) {
            case "SECURITY_AUDIT_ARCHIVE_STARTED" -> AuditResult.STARTED;
            case "SECURITY_AUDIT_ARCHIVE_FAILED" -> AuditResult.FAILED;
            case "SECURITY_AUDIT_ARCHIVE_COMPLETED", "SECURITY_AUDIT_ARCHIVE_VERIFIED" -> AuditResult.SUCCEEDED;
            default -> throw new IllegalArgumentException("非法的审计归档事件类型");
        };
    }
}
