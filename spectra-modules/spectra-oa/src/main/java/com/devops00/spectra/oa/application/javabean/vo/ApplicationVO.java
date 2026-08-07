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

package com.devops00.spectra.oa.application.javabean.vo;

import java.time.Instant;
import java.util.UUID;

import lombok.Data;

/// OA 申请响应。
@Data
public class ApplicationVO {
    private UUID id;
    private String applicationNo;
    private String typeCode;
    private UUID bizId;
    private UUID applicantId;
    private UUID departmentId;
    private String title;
    private String status;
    private String processInstanceId;
    private Instant submittedAt;
    private Instant completedAt;
    private String rejectReason;
    private Instant createdAt;
}
