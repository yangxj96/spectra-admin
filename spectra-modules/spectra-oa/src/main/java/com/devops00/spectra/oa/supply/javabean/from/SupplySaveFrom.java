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

package com.devops00.spectra.oa.supply.javabean.from;

import com.devops00.spectra.oa.supply.javabean.constant.SupplyItemStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 办公用品 SKU 保存参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class SupplySaveFrom {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 分类。
     */
    private String category;

    /**
     * SKU 编码。
     */
    @NotBlank(message = "办公用品 SKU 不能为空")
    private String sku;

    /**
     * 名称。
     */
    @NotBlank(message = "办公用品名称不能为空")
    private String name;

    /**
     * 规格。
     */
    private String specification;

    /**
     * 单位。
     */
    @NotBlank(message = "办公用品单位不能为空")
    private String unit = "件";

    /**
     * 最低库存。
     */
    @NotNull(message = "最低库存不能为空")
    @DecimalMin(value = "0", message = "最低库存不能小于 0")
    private BigDecimal minStock = BigDecimal.ZERO;

    /**
     * 状态。
     */
    private String status = SupplyItemStatus.ACTIVE.getValue();

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
     * 备注。
     */
    private String remark;
}
