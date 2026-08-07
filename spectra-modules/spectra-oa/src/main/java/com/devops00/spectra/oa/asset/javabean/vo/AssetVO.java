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

package com.devops00.spectra.oa.asset.javabean.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/// 资产台账响应视图。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Data
public class AssetVO {
    private UUID id;
    private UUID categoryId;
    private String categoryName;
    private String assetNo;
    private String name;
    private String specification;
    private String serialNo;
    private String assetType;
    private String status;
    private BigDecimal quantity;
    private LocalDate acquisitionDate;
    private BigDecimal acquisitionAmount;
    private String currency;
    private String supplier;
    private String location;
    private UUID departmentId;
    private UUID custodianId;
    private LocalDate warrantyUntil;
    private UUID sourcePurchaseId;
    private UUID sourceReceiptId;
    private UUID sourcePurchaseItemId;
    private String remark;
    private List<AssetOperationVO> operations = List.of();
    private Instant createdAt;
    private Instant updatedAt;
}
