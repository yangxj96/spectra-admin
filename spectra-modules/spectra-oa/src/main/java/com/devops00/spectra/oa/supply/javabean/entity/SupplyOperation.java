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

package com.devops00.spectra.oa.supply.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 办公用品库存变动记录。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_supply_operation", schema = "spectra_oa")
public class SupplyOperation extends BaseEntity {

    /**
     * 办公用品 ID。
     */
    @TableField("supply_id")
    private UUID supplyId;

    /**
     * 操作类型。
     */
    @TableField("operation_type")
    private String operationType;

    /**
     * 数量。
     */
    @TableField("quantity")
    private BigDecimal quantity;

    /**
     * 操作前库存。
     */
    @TableField("before_stock")
    private BigDecimal beforeStock;

    /**
     * 操作后库存。
     */
    @TableField("after_stock")
    private BigDecimal afterStock;

    /**
     * 部门 ID。
     */
    @TableField("department_id")
    private UUID departmentId;

    /**
     * 用户 ID。
     */
    @TableField("user_id")
    private UUID userId;

    /**
     * 位置。
     */
    @TableField("location")
    private String location;

    /**
     * 操作时间。
     */
    @TableField("operation_date")
    private Instant operationDate;

    /**
     * 原因。
     */
    @TableField("reason")
    private String reason;

    /**
     * 来源采购单 ID。
     */
    @TableField("source_purchase_id")
    private UUID sourcePurchaseId;

    /**
     * 来源收货单 ID。
     */
    @TableField("source_receipt_id")
    private UUID sourceReceiptId;

    /**
     * 来源采购明细 ID。
     */
    @TableField("source_purchase_item_id")
    private UUID sourcePurchaseItemId;

    /**
     * 状态。
     */
    @TableField("status")
    private String status;
}
