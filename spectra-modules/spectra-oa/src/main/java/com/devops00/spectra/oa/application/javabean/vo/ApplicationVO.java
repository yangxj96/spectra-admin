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

import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * OA 申请响应。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class ApplicationVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 申请编号。
     */
    private String applicationNo;

    /**
     * 类型编码。
     */
    private String typeCode;

    /**
     * 业务 ID。
     */
    private UUID bizId;

    /**
     * 申请人 ID。
     */
    private UUID applicantId;

    /**
     * 部门 ID。
     */
    private UUID departmentId;

    /**
     * 标题。
     */
    private String title;

    /**
     * 状态。
     */
    private String status;

    /**
     * 流程实例 ID。
     */
    private String processInstanceId;

    /**
     * 提交时间。
     */
    private LocalDateTime submittedAt;

    /**
     * 完成时间。
     */
    private LocalDateTime completedAt;

    /**
     * 驳回原因。
     */
    private String rejectReason;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
