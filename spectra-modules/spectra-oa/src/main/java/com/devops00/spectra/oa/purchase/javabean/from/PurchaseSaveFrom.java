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

package com.devops00.spectra.oa.purchase.javabean.from;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/// 采购申请保存参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class PurchaseSaveFrom {
    @NotBlank(message = "采购事由不能为空")
    private String purpose;

    @NotNull(message = "期望到货日期不能为空")
    private LocalDate expectedDate;

    @NotNull(message = "采购预算不能为空")
    @DecimalMin(value = "0.01", message = "采购预算必须大于 0")
    private BigDecimal budgetAmount;

    private String currency = "CNY";

    private String suggestedSupplier;

    @NotEmpty(message = "至少填写一条采购明细")
    @Valid
    private List<PurchaseItemFrom> items;
}
