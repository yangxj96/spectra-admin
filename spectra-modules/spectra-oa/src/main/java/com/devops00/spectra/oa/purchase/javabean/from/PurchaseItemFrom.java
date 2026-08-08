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

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/// 采购明细保存参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Data
public class PurchaseItemFrom {

    /// 明细类型字段。
    @NotBlank(message = "采购类型不能为空")
    private String itemType;

    /// 明细名称字段。
    @NotBlank(message = "物品或服务名称不能为空")
    private String itemName;

    /// 规格。
    private String specification;

    /// 数量。
    @NotNull(message = "采购数量不能为空")
    @DecimalMin(value = "0.001", message = "采购数量必须大于 0")
    private BigDecimal quantity;

    /// 预计单价。
    @NotNull(message = "估价单价不能为空")
    @DecimalMin(value = "0.00", message = "估价单价不能为负数")
    private BigDecimal estimatedUnitPrice;

    /// 用途。
    private String purpose;
}
