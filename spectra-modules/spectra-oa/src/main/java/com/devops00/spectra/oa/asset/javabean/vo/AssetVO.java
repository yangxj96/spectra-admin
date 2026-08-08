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

package com.devops00.spectra.oa.asset.javabean.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/// 资产台账响应视图。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Data
public class AssetVO {

    /// 主键 ID。
    private UUID id;

    /// 分类 ID。
    private UUID categoryId;

    /// 分类名称。
    private String categoryName;

    /// 资产编号。
    private String assetNo;

    /// 名称。
    private String name;

    /// 规格。
    private String specification;

    /// 序列号。
    private String serialNo;

    /// 资产类型。
    private String assetType;

    /// 状态。
    private String status;

    /// 数量。
    private BigDecimal quantity;

    /// 购置日期。
    private LocalDate acquisitionDate;

    /// 购置金额。
    private BigDecimal acquisitionAmount;

    /// 币种。
    private String currency;

    /// 供应商。
    private String supplier;

    /// 位置。
    private String location;

    /// 部门 ID。
    private UUID departmentId;

    /// 保管人 ID。
    private UUID custodianId;

    /// 保修截止日期。
    private LocalDate warrantyUntil;

    /// 来源采购单 ID。
    private UUID sourcePurchaseId;

    /// 来源收货单 ID。
    private UUID sourceReceiptId;

    /// 来源采购明细 ID。
    private UUID sourcePurchaseItemId;

    /// 备注。
    private String remark;

    /// 创建时间。
    private List<AssetOperationVO> operations = List.of();
    private Instant createdAt;

    /// 更新时间。
    private Instant updatedAt;
}
