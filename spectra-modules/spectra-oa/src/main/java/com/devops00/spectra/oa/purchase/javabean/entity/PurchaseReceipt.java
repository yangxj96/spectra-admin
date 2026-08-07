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

import java.time.LocalDate;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/// 采购收货批次。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/7
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_purchase_receipt", schema = "spectra_oa")
@DataScope
public class PurchaseReceipt extends BaseEntity {

    @TableField("purchase_id")
    private UUID purchaseId;

    @TableField("receipt_no")
    private String receiptNo;

    @TableField("received_date")
    private LocalDate receivedDate;

    @TableField("receiver_id")
    private UUID receiverId;

    @TableField("status")
    private String status;

    @TableField("remark")
    private String remark;
}
