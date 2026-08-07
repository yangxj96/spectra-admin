package com.devops00.spectra.oa.supply.javabean.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

/// 办公用品库存变动记录响应。
@Data
public class SupplyOperationVO {
    private UUID id;
    private UUID supplyId;
    private String operationType;
    private BigDecimal quantity;
    private BigDecimal beforeStock;
    private BigDecimal afterStock;
    private UUID departmentId;
    private UUID userId;
    private String location;
    private LocalDate operationDate;
    private String reason;
    private UUID sourcePurchaseId;
    private UUID sourceReceiptId;
    private UUID sourcePurchaseItemId;
    private String status;
    private Instant createdAt;
}
