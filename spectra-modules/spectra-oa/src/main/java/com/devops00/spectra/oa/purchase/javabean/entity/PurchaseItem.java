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

package com.devops00.spectra.oa.purchase.javabean.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/// 采购明细。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_purchase_item", schema = "spectra_oa")
@DataScope
public class PurchaseItem extends BaseEntity {

    /// 采购单 ID。
    @TableField("purchase_id")
    private UUID purchaseId;

    /// 部门 ID。
    @TableField("department_id")
    private UUID departmentId;

    /// 明细类型字段。
    @TableField("item_type")
    private String itemType;

    /// 明细名称字段。
    @TableField("item_name")
    private String itemName;

    /// 规格。
    @TableField("specification")
    private String specification;

    /// 数量。
    @TableField("quantity")
    private BigDecimal quantity;

    /// 预计单价。
    @TableField("estimated_unit_price")
    private BigDecimal estimatedUnitPrice;

    /// 预计金额。
    @TableField("estimated_amount")
    private BigDecimal estimatedAmount;

    /// 用途。
    @TableField("purpose")
    private String purpose;

    /// 实收数量。
    @TableField("received_quantity")
    private BigDecimal receivedQuantity;
}
