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
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/**
 * 采购收货明细。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_purchase_receipt_item", schema = "spectra_oa")
@DataScope
public class PurchaseReceiptItem extends BaseEntity {

    /**
     * 收货单 ID。
     */
    @TableField("receipt_id")
    private UUID receiptId;

    /**
     * 采购明细 ID。
     */
    @TableField("purchase_item_id")
    private UUID purchaseItemId;

    /**
     * 数量。
     */
    @TableField("quantity")
    private BigDecimal quantity;

    /**
     * 是否已接受。
     */
    @TableField("accepted")
    private Boolean accepted;

    /**
     * 差异原因。
     */
    @TableField("difference_reason")
    private String differenceReason;
}
