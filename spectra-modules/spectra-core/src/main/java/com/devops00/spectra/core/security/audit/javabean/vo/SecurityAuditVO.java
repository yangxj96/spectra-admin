/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at

 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.security.audit.javabean.vo;

import com.devops00.spectra.security.base.audit.AuditResult;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * 安全审计对外只读视图。before/after 已经过查询侧二次脱敏。
 */
public record SecurityAuditVO(UUID eventId,
                              String eventType,
                              UUID operatorId,
                              UUID targetId,
                              String client,
                              String ip,
                              String userAgent,
                              Map<String, Object> before,
                              Map<String, Object> after,
                              String reason,
                              LocalDateTime occurredAt,
                              AuditResult result,
                              String correlationId) {
}
