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

package com.devops00.spectra.oa.reimbursement.javabean.entity;

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

/**
 * 费用报销明细。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_reimbursement_item", schema = "spectra_oa")
@DataScope
public class ReimbursementItem extends BaseEntity {

    /**
     * 报销单 ID。
     */
    @TableField("reimbursement_id")
    private UUID reimbursementId;

    /**
     * 部门 ID。
     */
    @TableField("department_id")
    private UUID departmentId;

    /**
     * 费用日期。
     */
    @TableField("expense_date")
    private Instant expenseDate;

    /**
     * 费用类别。
     */
    @TableField("category")
    private String category;

    /**
     * 费用描述。
     */
    @TableField("description")
    private String description;

    /**
     * 费用金额。
     */
    @TableField("amount")
    private BigDecimal amount;

    /**
     * 税额。
     */
    @TableField("tax_amount")
    private BigDecimal taxAmount;

    /**
     * 发票号码。
     */
    @TableField("invoice_no")
    private String invoiceNo;
}
