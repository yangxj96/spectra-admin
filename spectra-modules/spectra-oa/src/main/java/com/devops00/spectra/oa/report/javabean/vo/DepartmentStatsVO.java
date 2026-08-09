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

package com.devops00.spectra.oa.report.javabean.vo;

import java.math.BigDecimal;
import java.util.UUID;

import lombok.Data;

/**
 * 部门维度 OA 业务统计结果。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Data
public class DepartmentStatsVO {

    /**
     * 部门 ID。
     */
    private UUID departmentId;

    /**
     * 部门完整路径。
     */
    private String departmentName;

    /**
     * 资产数量。
     */
    private long assetCount;

    /**
     * 资产台账数量合计。
     */
    private BigDecimal assetQuantity = BigDecimal.ZERO;

    /**
     * 资产取得金额合计。
     */
    private BigDecimal assetValue = BigDecimal.ZERO;

    /**
     * 办公用品 SKU 数。
     */
    private long supplySkuCount;

    /**
     * 办公用品当前库存合计。
     */
    private BigDecimal supplyStock = BigDecimal.ZERO;

    /**
     * 办公用品最低库存合计。
     */
    private BigDecimal supplyMinStock = BigDecimal.ZERO;

    /**
     * 报销单数量。
     */
    private long reimbursementCount;

    /**
     * 报销金额合计。
     */
    private BigDecimal reimbursementAmount = BigDecimal.ZERO;

    /**
     * 采购申请数量。
     */
    private long purchaseCount;

    /**
     * 采购预算合计。
     */
    private BigDecimal purchaseBudget = BigDecimal.ZERO;
}
