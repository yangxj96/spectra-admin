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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * 费用报销主表。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_reimbursement", schema = "spectra_oa")
@DataScope
public class Reimbursement extends BaseEntity {

    /**
     * 申请 ID。
     */
    @TableField("application_id")
    private UUID applicationId;

    /**
     * 部门 ID。
     */
    @TableField("department_id")
    private UUID departmentId;

    /**
     * 报销用途。
     */
    @TableField("purpose")
    private String purpose;

    /**
     * 费用开始日期。
     */
    @TableField("expense_start")
    private Instant expenseStart;

    /**
     * 费用结束日期。
     */
    @TableField("expense_end")
    private Instant expenseEnd;

    /**
     * 报销总金额。
     */
    @TableField("total_amount")
    private BigDecimal totalAmount;

    /**
     * 币种。
     */
    @TableField("currency")
    private String currency;

    /**
     * 收款人姓名。
     */
    @TableField("payee_name")
    private String payeeName;

    /**
     * 收款账户。
     */
    @TableField("payee_account")
    private String payeeAccount;

    /**
     * 支付状态。
     */
    @TableField("payment_status")
    private String paymentStatus;

    /**
     * 支付时间。
     */
    @TableField("payment_at")
    private Instant paymentAt;

    /**
     * 支付备注。
     */
    @TableField("payment_remark")
    private String paymentRemark;
}
