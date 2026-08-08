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

package com.devops00.spectra.oa.asset.javabean.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/// OA 资产台账实体。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_asset", schema = "spectra_oa")
@DataScope
public class Asset extends BaseEntity {

    /// 分类 ID。
    @TableField("category_id")
    private UUID categoryId;

    /// 资产编号。
    @TableField("asset_no")
    private String assetNo;

    /// 名称。
    @TableField("name")
    private String name;

    /// 规格。
    @TableField("specification")
    private String specification;

    /// 序列号。
    @TableField("serial_no")
    private String serialNo;

    /// 资产类型。
    @TableField("asset_type")
    private String assetType;

    /// 状态。
    @TableField("status")
    private String status;

    /// 数量。
    @TableField("quantity")
    private BigDecimal quantity;

    /// 购置日期。
    @TableField("acquisition_date")
    private Instant acquisitionDate;

    /// 购置金额。
    @TableField("acquisition_amount")
    private BigDecimal acquisitionAmount;

    /// 币种。
    @TableField("currency")
    private String currency;

    /// 供应商。
    @TableField("supplier")
    private String supplier;

    /// 位置。
    @TableField("location")
    private String location;

    /// 部门 ID。
    @TableField("department_id")
    private UUID departmentId;

    /// 保管人 ID。
    @TableField("custodian_id")
    private UUID custodianId;

    /// 保修截止日期。
    @TableField("warranty_until")
    private Instant warrantyUntil;

    /// 来源采购单 ID。
    @TableField("source_purchase_id")
    private UUID sourcePurchaseId;

    /// 来源收货单 ID。
    @TableField("source_receipt_id")
    private UUID sourceReceiptId;

    /// 来源采购明细 ID。
    @TableField("source_purchase_item_id")
    private UUID sourcePurchaseItemId;

    /// 备注。
    @TableField("remark")
    private String remark;
}
