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

package com.devops00.spectra.oa.purchase.javabean.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/// 采购申请响应视图。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class PurchaseVO {

    /// 主键 ID。
    private UUID id;

    /// 申请 ID。
    private UUID applicationId;

    /// 申请编号。
    private String applicationNo;

    /// 标题。
    private String title;

    /// 状态。
    private String status;

    /// 申请人 ID。
    private UUID applicantId;

    /// 部门 ID。
    private UUID departmentId;

    /// 用途。
    private String purpose;

    /// 预计日期。
    private LocalDate expectedDate;

    /// 预算金额。
    private BigDecimal budgetAmount;

    /// 币种。
    private String currency;

    /// 建议供应商。
    private String suggestedSupplier;

    /// 执行状态。
    private String executionStatus;

    /// 采购人 ID。
    private UUID purchaserId;

    /// 订单编号。
    private String orderNo;

    /// 下单时间。
    private Instant orderedAt;

    /// 完成时间。
    private Instant completedAt;

    /// 执行备注。
    private String executionRemark;

    /// 流程实例 ID。
    private String processInstanceId;

    /// 驳回原因。
    private String rejectReason;

    /// 创建时间。
    private List<PurchaseItemVO> items = List.of();
    private List<PurchaseReceiptVO> receipts = List.of();
    private Instant createdAt;

    /// 更新时间。
    private Instant updatedAt;
}
