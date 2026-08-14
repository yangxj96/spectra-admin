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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * 采购收货批次。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_purchase_receipt", schema = "spectra_oa")
@DataScope(readPermission = "oa:purchase:read", writePermission = "oa:purchase:update", column = "", relations = {
        @DataScope.Relation(schema = "spectra_oa", table = "oa_purchase", joinColumn = "id", userColumn = "",
                mainColumn = "purchase_id", departmentColumn = "department_id")})
public class PurchaseReceipt extends BaseEntity {

    /**
     * 采购单 ID。
     */
    @TableField("purchase_id")
    private UUID purchaseId;

    /**
     * 收货单号。
     */
    @TableField("receipt_no")
    private String receiptNo;

    /**
     * 收货日期。
     */
    @TableField("received_date")
    private Instant receivedDate;

    /**
     * 接收人 ID。
     */
    @TableField("receiver_id")
    private UUID receiverId;

    /**
     * 状态。
     */
    @TableField("status")
    private String status;

    /**
     * 备注。
     */
    @TableField("remark")
    private String remark;
}
