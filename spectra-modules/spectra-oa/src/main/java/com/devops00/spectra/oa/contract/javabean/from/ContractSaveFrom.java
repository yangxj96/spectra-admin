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

    /// 标题。
    @NotBlank(message = "合同标题不能为空")
    private String title;

    /// 合同类型字段。
    @NotBlank(message = "合同类型不能为空")
    private String contractType;

    /// 对方名称。
    @NotBlank(message = "相对方名称不能为空")
    private String counterpartyName;

    /// 对方联系人。
    private String counterpartyContact;

    /// 金额。
    @NotNull(message = "合同金额不能为空")
    @DecimalMin(value = "0.00", message = "合同金额不能小于 0")
    private BigDecimal amount;

    /// 币种。
    private String currency = "CNY";

    /// 开始日期。
    private LocalDate startDate;

    /// 结束日期。
    private LocalDate endDate;

    /// 可见范围。
    private String visibility = "DEPARTMENT";

    /// 摘要。
    private String summary;
}
