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
import com.devops00.spectra.common.base.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/// 资产生命周期操作记录。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_asset_operation", schema = "spectra_oa")
public class AssetOperation extends BaseEntity {

    /// 资产 ID。
    @TableField("asset_id")
    private UUID assetId;

    /// 操作类型。
    @TableField("operation_type")
    private String operationType;

    /// 原部门 ID。
    @TableField("from_department_id")
    private UUID fromDepartmentId;

    /// 目标部门 ID。
    @TableField("to_department_id")
    private UUID toDepartmentId;

    /// 原用户 ID。
    @TableField("from_user_id")
    private UUID fromUserId;

    /// 目标用户 ID。
    @TableField("to_user_id")
    private UUID toUserId;

    /// 原位置。
    @TableField("from_location")
    private String fromLocation;

    /// 目标位置。
    @TableField("to_location")
    private String toLocation;

    /// 操作时间。
    @TableField("operation_date")
    private Instant operationDate;

    /// 原因。
    @TableField("reason")
    private String reason;

    /// 维修内容。
    @TableField("maintenance_content")
    private String maintenanceContent;

    /// 维修费用。
    @TableField("maintenance_cost")
    private BigDecimal maintenanceCost;

    /// 状态。
    @TableField("status")
    private String status;
}
