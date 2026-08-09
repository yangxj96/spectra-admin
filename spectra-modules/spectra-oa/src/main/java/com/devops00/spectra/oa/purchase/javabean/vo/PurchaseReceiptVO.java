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

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/**
 * 采购收货批次响应视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Data
public class PurchaseReceiptVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 收货单号。
     */
    private String receiptNo;

    /**
     * 收货日期。
     */
    private LocalDate receivedDate;

    /**
     * 接收人 ID。
     */
    private UUID receiverId;

    /**
     * 状态。
     */
    private String status;

    /**
     * 备注。
     */
    private String remark;
    private List<PurchaseReceiptItemVO> items = List.of();
}
