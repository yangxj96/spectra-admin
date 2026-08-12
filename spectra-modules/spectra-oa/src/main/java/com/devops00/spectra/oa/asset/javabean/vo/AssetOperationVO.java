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

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * 资产生命周期操作响应视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Data
public class AssetOperationVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 资产 ID。
     */
    private UUID assetId;

    /**
     * 操作类型。
     */
    private String operationType;

    /**
     * 原部门 ID。
     */
    private UUID fromDepartmentId;

    /**
     * 目标部门 ID。
     */
    private UUID toDepartmentId;

    /**
     * 原用户 ID。
     */
    private UUID fromUserId;

    /**
     * 目标用户 ID。
     */
    private UUID toUserId;

    /**
     * 原位置。
     */
    private String fromLocation;

    /**
     * 目标位置。
     */
    private String toLocation;

    /**
     * 操作时间。
     */
    private LocalDate operationDate;

    /**
     * 原因。
     */
    private String reason;

    /**
     * 维修内容。
     */
    private String maintenanceContent;

    /**
     * 维修费用。
     */
    private BigDecimal maintenanceCost;

    /**
     * 状态。
     */
    private String status;
}
