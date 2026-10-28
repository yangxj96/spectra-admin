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

package com.devops00.spectra.core.audit.service;

import com.devops00.spectra.core.audit.javabean.from.AuditLogQueryFrom;
import com.devops00.spectra.core.audit.javabean.vo.AuditLogPageVO;
import com.devops00.spectra.core.audit.javabean.vo.AuditLogVO;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import org.springframework.security.core.Authentication;

import java.time.Instant;
import java.util.UUID;

/**
 * 审计日志查询应用服务契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
public interface AuditLogQueryService {

    AuditLogPageVO page(Authentication viewer, PageFrom page, AuditLogQueryFrom query);

    AuditLogVO detail(Authentication viewer, UUID eventId, Instant occurredAt);

    String export(Authentication viewer, AuditLogQueryFrom query);
}
