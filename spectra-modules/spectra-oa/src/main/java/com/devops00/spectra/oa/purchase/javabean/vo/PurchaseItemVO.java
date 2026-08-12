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

package com.devops00.spectra.oa.purchase.javabean.vo;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 采购明细响应视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Data
public class PurchaseItemVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 明细类型字段。
     */
    private String itemType;

    /**
     * 明细名称字段。
     */
    private String itemName;

    /**
     * 规格。
     */
    private String specification;

    /**
     * 数量。
     */
    private BigDecimal quantity;

    /**
     * 预计单价。
     */
    private BigDecimal estimatedUnitPrice;

    /**
     * 预计金额。
     */
    private BigDecimal estimatedAmount;

    /**
     * 用途。
     */
    private String purpose;

    /**
     * 实收数量。
     */
    private BigDecimal receivedQuantity;
}
