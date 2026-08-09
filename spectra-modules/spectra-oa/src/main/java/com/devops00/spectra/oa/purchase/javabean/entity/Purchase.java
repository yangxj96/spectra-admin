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

package com.devops00.spectra.oa.purchase.javabean.entity;

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
 * 采购申请主表。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_purchase", schema = "spectra_oa")
@DataScope
public class Purchase extends BaseEntity {

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
     * 用途。
     */
    @TableField("purpose")
    private String purpose;

    /**
     * 预计日期。
     */
    @TableField("expected_date")
    private Instant expectedDate;

    /**
     * 预算金额。
     */
    @TableField("budget_amount")
    private BigDecimal budgetAmount;

    /**
     * 币种。
     */
    @TableField("currency")
    private String currency;

    /**
     * 建议供应商。
     */
    @TableField("suggested_supplier")
    private String suggestedSupplier;

    /**
     * 执行状态。
     */
    @TableField("execution_status")
    private String executionStatus;

    /**
     * 采购人 ID。
     */
    @TableField("purchaser_id")
    private UUID purchaserId;

    /**
     * 订单编号。
     */
    @TableField("order_no")
    private String orderNo;

    /**
     * 下单时间。
     */
    @TableField("ordered_at")
    private Instant orderedAt;

    /**
     * 完成时间。
     */
    @TableField("completed_at")
    private Instant completedAt;

    /**
     * 执行备注。
     */
    @TableField("execution_remark")
    private String executionRemark;
}
