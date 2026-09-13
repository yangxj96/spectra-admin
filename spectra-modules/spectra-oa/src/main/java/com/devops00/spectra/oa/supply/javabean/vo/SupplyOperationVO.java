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
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * 办公用品库存变动记录响应。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class SupplyOperationVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 办公用品 ID。
     */
    private UUID supplyId;

    /**
     * 操作类型。
     */
    private String operationType;

    /**
     * 数量。
     */
    private BigDecimal quantity;

    /**
     * 操作前库存。
     */
    private BigDecimal beforeStock;

    /**
     * 操作后库存。
     */
    private BigDecimal afterStock;

    /**
     * 部门 ID。
     */
    private UUID departmentId;

    /**
     * 用户 ID。
     */
    private UUID userId;

    /**
     * 位置。
     */
    private String location;

    /**
     * 操作时间。
     */
    private LocalDate operationDate;

    /**
     * 原因。
     */
    private String reason;

    /**
     * 来源采购单 ID。
     */
    private UUID sourcePurchaseId;

    /**
     * 来源收货单 ID。
     */
    private UUID sourceReceiptId;

    /**
     * 来源采购明细 ID。
     */
    private UUID sourcePurchaseItemId;

    /**
     * 状态。
     */
    private String status;

    /**
     * 创建时间。
     */
    private LocalDateTime createdAt;
}
