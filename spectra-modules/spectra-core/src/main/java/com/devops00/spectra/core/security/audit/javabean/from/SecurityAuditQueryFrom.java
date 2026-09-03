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

package com.devops00.spectra.core.security.audit.javabean.from;

import com.devops00.spectra.core.security.audit.AuditResult;
import lombok.Data;

import java.util.UUID;

/**
 * 安全审计查询条件。时间范围由服务统一限制在可查询热存窗口和数据库实际范围内。
 */
@Data
public class SecurityAuditQueryFrom {

    private String eventType;

    private UUID operatorId;

    private UUID targetId;

    private AuditResult result;

    private String from;

    private String to;
}
