package com.devops00.spectra.oa.supply.javabean.from;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

/// 办公用品入库、领用、退库和盘点调整参数。
@Data
public class SupplyOperationFrom {
    @DecimalMin(value = "0.001", message = "数量必须大于 0")
    private BigDecimal quantity;

    @DecimalMin(value = "0", message = "调整后的库存不能小于 0")
    private BigDecimal targetStock;

    private UUID departmentId;
    private UUID userId;
    private String location;
    private LocalDate operationDate;
    private String reason;
    private UUID sourcePurchaseId;
    private UUID sourceReceiptId;
    private UUID sourcePurchaseItemId;
}
