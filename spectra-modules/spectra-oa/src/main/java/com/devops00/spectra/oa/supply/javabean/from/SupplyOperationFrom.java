package com.devops00.spectra.oa.supply.javabean.from;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 办公用品入库、领用、退库和盘点调整参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class SupplyOperationFrom {

    /**
     * 数量。
     */
    @DecimalMin(value = "0.001", message = "数量必须大于 0")
    private BigDecimal quantity;

    /**
     * 目标库存。
     */
    @DecimalMin(value = "0", message = "调整后的库存不能小于 0")
    private BigDecimal targetStock;

    /**
     * 部门 ID。
     */
    private UUID departmentId;

    /**
     * 用户 ID。
     */
    private UUID userId;

    /**
     * 位置。
     */
    private String location;

    /**
     * 操作时间。
     */
    private String operationDate;

    /**
     * 原因。
     */
    private String reason;

    /**
     * 来源采购单 ID。
     */
    private UUID sourcePurchaseId;

    /**
     * 来源收货单 ID。
     */
    private UUID sourceReceiptId;

    /**
     * 来源采购明细 ID。
     */
    private UUID sourcePurchaseItemId;
}
