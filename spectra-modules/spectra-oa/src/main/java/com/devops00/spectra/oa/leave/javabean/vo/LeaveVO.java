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

package com.devops00.spectra.oa.leave.javabean.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 请假申请详情响应。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class LeaveVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 申请 ID。
     */
    private UUID applicationId;

    /**
     * 申请编号。
     */
    private String applicationNo;

    /**
     * 标题。
     */
    private String title;

    /**
     * 状态。
     */
    private String status;

    /**
     * 申请人 ID。
     */
    private UUID applicantId;

    /**
     * 请假类型编码。
     */
    private String leaveTypeCode;

    /**
     * 开始时间。
     */
    private LocalDateTime startTime;

    /**
     * 结束时间。
     */
    private LocalDateTime endTime;

    /**
     * 时长（小时）。
     */
    private BigDecimal durationHours;

    /**
     * 原因。
     */
    private String reason;

    /**
     * 联系地址。
     */
    private String contactAddress;

    /**
     * 流程实例 ID。
     */
    private String processInstanceId;

    /**
     * 驳回原因。
     */
    private String rejectReason;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
