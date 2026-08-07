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

package com.devops00.spectra.oa.contract.javabean.from;

import java.math.BigDecimal;
import java.time.LocalDate;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/// 合同台账保存参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Data
public class ContractSaveFrom {
    @NotBlank(message = "合同标题不能为空")
    private String title;

    @NotBlank(message = "合同类型不能为空")
    private String contractType;

    @NotBlank(message = "相对方名称不能为空")
    private String counterpartyName;

    private String counterpartyContact;

    @NotNull(message = "合同金额不能为空")
    @DecimalMin(value = "0.00", message = "合同金额不能小于 0")
    private BigDecimal amount;

    private String currency = "CNY";
    private LocalDate startDate;
    private LocalDate endDate;
    private String visibility = "DEPARTMENT";
    private String summary;
}
