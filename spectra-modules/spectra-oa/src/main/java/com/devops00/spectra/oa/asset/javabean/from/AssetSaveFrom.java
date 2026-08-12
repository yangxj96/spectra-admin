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

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 资产入库或台账保存参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Data
public class AssetSaveFrom {

    /**
     * 分类 ID。
     */
    private UUID categoryId;

    /**
     * 资产编号。
     */
    private String assetNo;

    /**
     * 名称。
     */
    @NotBlank(message = "资产名称不能为空")
    private String name;

    /**
     * 规格。
     */
    private String specification;

    /**
     * 序列号。
     */
    private String serialNo;

    /**
     * 资产类型。
     */
    private String assetType = "FIXED";

    /**
     * 状态。
     */
    private String status;

    /**
     * 数量。
     */
    @NotNull(message = "资产数量不能为空")
    @DecimalMin(value = "0.001", message = "资产数量必须大于 0")
    private BigDecimal quantity = BigDecimal.ONE;

    /**
     * 购置日期。
     */
    private String acquisitionDate;

    /**
     * 购置金额。
     */
    private BigDecimal acquisitionAmount = BigDecimal.ZERO;

    /**
     * 币种。
     */
    private String currency = "CNY";

    /**
     * 供应商。
     */
    private String supplier;

    /**
     * 位置。
     */
    private String location;

    /**
     * 部门 ID。
     */
    private UUID departmentId;

    /**
     * 保管人 ID。
     */
    private UUID custodianId;

    /**
     * 保修截止日期。
     */
    private String warrantyUntil;

    /**
     * 备注。
     */
    private String remark;
}
