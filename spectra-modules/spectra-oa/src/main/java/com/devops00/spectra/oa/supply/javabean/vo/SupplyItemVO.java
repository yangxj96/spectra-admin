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

package com.devops00.spectra.oa.supply.javabean.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * 办公用品库存响应视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class SupplyItemVO {

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
    private String sku;

    /**
     * 名称。
     */
    private String name;

    /**
     * 规格。
     */
    private String specification;

    /**
     * 单位。
     */
    private String unit;

    /**
     * 当前库存。
     */
    private BigDecimal currentStock;

    /**
     * 最低库存。
     */
    private BigDecimal minStock;

    /**
     * 是否低库存。
     */
    private Boolean lowStock;

    /**
     * 状态。
     */
    private String status;

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

    /**
     * 创建时间。
     */
    private List<SupplyOperationVO> operations = List.of();
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}
