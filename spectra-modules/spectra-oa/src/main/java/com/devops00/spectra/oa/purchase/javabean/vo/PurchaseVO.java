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
    private UUID id;
    private UUID applicationId;
    private String applicationNo;
    private String title;
    private String status;
    private UUID applicantId;
    private UUID departmentId;
    private String purpose;
    private LocalDate expectedDate;
    private BigDecimal budgetAmount;
    private String currency;
    private String suggestedSupplier;
    private String executionStatus;
    private UUID purchaserId;
    private String orderNo;
    private Instant orderedAt;
    private Instant completedAt;
    private String executionRemark;
    private String processInstanceId;
    private String rejectReason;
    private List<PurchaseItemVO> items = List.of();
    private List<PurchaseReceiptVO> receipts = List.of();
    private Instant createdAt;
    private Instant updatedAt;
}
