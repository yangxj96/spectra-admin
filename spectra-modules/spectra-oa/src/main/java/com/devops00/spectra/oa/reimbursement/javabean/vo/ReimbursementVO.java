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

package com.devops00.spectra.oa.reimbursement.javabean.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/// 费用报销响应视图。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class ReimbursementVO {
    private UUID id;
    private UUID applicationId;
    private String applicationNo;
    private String title;
    private String status;
    private UUID applicantId;
    private UUID departmentId;
    private String purpose;
    private LocalDate expenseStart;
    private LocalDate expenseEnd;
    private BigDecimal totalAmount;
    private String currency;
    private String payeeName;
    private String payeeAccountMasked;
    private String paymentStatus;
    private Instant paymentAt;
    private String paymentRemark;
    private String processInstanceId;
    private String rejectReason;
    private List<ReimbursementItemVO> items = List.of();
    private List<ReimbursementAttachmentVO> attachments = List.of();
    private Instant createdAt;
    private Instant updatedAt;
}
