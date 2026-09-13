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

package com.devops00.spectra.core.audit.javabean.domain;

import lombok.Data;

import java.time.Instant;
import java.util.UUID;

/**
 * 描述审计日志查询行数据相关的领域数据和查询结果。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
public class AuditLogQueryRow {

    private UUID eventId;

    private Instant occurredAt;

    private String category;

    private String eventType;

    private UUID operatorId;

    private String operatorName;

    private UUID targetId;

    private String client;

    private String ip;

    private String userAgent;

    private String httpMethod;

    private String requestUrl;

    private Integer httpStatus;

    private Long durationMs;

    private String beforeSnapshot;

    private String afterSnapshot;

    private String reason;

    private String result;

    private String failureCode;

    private String failureType;

    private String failureReason;

    private String correlationId;
}
