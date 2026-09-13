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

package com.devops00.spectra.oa.report.javabean.entity;

import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 部门统计统一报表查询的结果行模型。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Data
public class DepartmentStatsRow {

    /** 部门 ID。 */
    private UUID departmentId;

    /** 资产条目数。 */
    private long assetCount;

    /** 资产数量合计。 */
    private BigDecimal assetQuantity = BigDecimal.ZERO;

    /** 资产金额合计。 */
    private BigDecimal assetValue = BigDecimal.ZERO;

    /** 办公用品 SKU 数。 */
    private long supplySkuCount;

    /** 办公用品当前库存合计。 */
    private BigDecimal supplyStock = BigDecimal.ZERO;

    /** 办公用品最低库存合计。 */
    private BigDecimal supplyMinStock = BigDecimal.ZERO;

    /** 报销单数。 */
    private long reimbursementCount;

    /** 报销金额合计。 */
    private BigDecimal reimbursementAmount = BigDecimal.ZERO;

    /** 采购申请数。 */
    private long purchaseCount;

    /** 采购预算合计。 */
    private BigDecimal purchaseBudget = BigDecimal.ZERO;
}
