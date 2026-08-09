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

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/// 报销费用明细参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class ReimbursementItemFrom {

    /// 费用日期。
    @NotNull(message = "费用日期不能为空")
    private String expenseDate;

    /// 分类。
    @NotBlank(message = "费用类别不能为空")
    private String category;

    /// 描述。
    @NotBlank(message = "费用说明不能为空")
    private String description;

    /// 金额。
    @NotNull(message = "费用金额不能为空")
    @DecimalMin(value = "0.01", message = "费用金额必须大于 0")
    private BigDecimal amount;

    /// 税额。
    @NotNull(message = "税额不能为空")
    @DecimalMin(value = "0.00", message = "税额不能为负数")
    private BigDecimal taxAmount = BigDecimal.ZERO;

    /// 发票编号。
    private String invoiceNo;
}
