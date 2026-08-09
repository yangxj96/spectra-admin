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

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

/**
 * 资产领用、归还、调拨、维修和报废操作参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Data
public class AssetOperationFrom {

    /**
     * 目标部门 ID。
     */
    private UUID toDepartmentId;

    /**
     * 目标用户 ID。
     */
    private UUID toUserId;

    /**
     * 目标位置。
     */
    private String toLocation;

    /**
     * 操作时间。
     */
    private String operationDate;

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
