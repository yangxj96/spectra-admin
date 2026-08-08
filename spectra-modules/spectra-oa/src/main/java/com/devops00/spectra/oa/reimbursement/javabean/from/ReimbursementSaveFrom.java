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

package com.devops00.spectra.oa.reimbursement.javabean.from;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/// 报销单保存参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class ReimbursementSaveFrom {

    /// 用途。
    @NotBlank(message = "报销事由不能为空")
    private String purpose;

    /// 费用开始日期。
    @NotNull(message = "费用开始日期不能为空")
    private String expenseStart;

    /// 费用结束日期。
    @NotNull(message = "费用结束日期不能为空")
    private String expenseEnd;

    /// 总金额。
    @NotNull(message = "报销总额不能为空")
    @DecimalMin(value = "0.01", message = "报销总额必须大于 0")
    private BigDecimal totalAmount;

    /// 币种。
    private String currency = "CNY";

    /// 收款人姓名。
    @NotBlank(message = "收款人不能为空")
    private String payeeName;

    /// 收款账户。
    private String payeeAccount;

    /// 明细列表。
    @NotEmpty(message = "至少填写一条费用明细")
    @Valid
    private List<ReimbursementItemFrom> items;

    /// 附件列表。
    @Valid
    private List<ReimbursementAttachmentFrom> attachments;
}
