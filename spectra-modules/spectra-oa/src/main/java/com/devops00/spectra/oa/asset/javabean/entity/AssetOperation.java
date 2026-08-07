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
import java.time.LocalDate;
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
    @TableField("asset_id")
    private UUID assetId;

    @TableField("operation_type")
    private String operationType;

    @TableField("from_department_id")
    private UUID fromDepartmentId;

    @TableField("to_department_id")
    private UUID toDepartmentId;

    @TableField("from_user_id")
    private UUID fromUserId;

    @TableField("to_user_id")
    private UUID toUserId;

    @TableField("from_location")
    private String fromLocation;

    @TableField("to_location")
    private String toLocation;

    @TableField("operation_date")
    private LocalDate operationDate;

    @TableField("reason")
    private String reason;

    @TableField("maintenance_content")
    private String maintenanceContent;

    @TableField("maintenance_cost")
    private BigDecimal maintenanceCost;

    @TableField("status")
    private String status;
}
