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

package com.devops00.spectra.oa.asset.javabean.from;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/// 资产入库或台账保存参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Data
public class AssetSaveFrom {
    private UUID categoryId;

    private String assetNo;

    @NotBlank(message = "资产名称不能为空")
    private String name;

    private String specification;
    private String serialNo;
    private String assetType = "FIXED";
    private String status;

    @NotNull(message = "资产数量不能为空")
    @DecimalMin(value = "0.001", message = "资产数量必须大于 0")
    private BigDecimal quantity = BigDecimal.ONE;

    private LocalDate acquisitionDate;
    private BigDecimal acquisitionAmount = BigDecimal.ZERO;
    private String currency = "CNY";
    private String supplier;
    private String location;
    private UUID departmentId;
    private UUID custodianId;
    private LocalDate warrantyUntil;
    private String remark;
}
