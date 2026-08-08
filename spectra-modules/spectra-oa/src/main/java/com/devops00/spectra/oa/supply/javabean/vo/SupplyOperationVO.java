package com.devops00.spectra.oa.supply.javabean.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

import lombok.Data;

/// 办公用品库存变动记录响应。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Data
public class SupplyOperationVO {

    /// 主键 ID。
    private UUID id;

    /// 办公用品 ID。
    private UUID supplyId;

    /// 操作类型。
    private String operationType;

    /// 数量。
    private BigDecimal quantity;

    /// 操作前库存。
    private BigDecimal beforeStock;

    /// 操作后库存。
    private BigDecimal afterStock;

    /// 部门 ID。
    private UUID departmentId;

    /// 用户 ID。
    private UUID userId;

    /// 位置。
    private String location;

    /// 操作时间。
    private LocalDate operationDate;

    /// 原因。
    private String reason;

    /// 来源采购单 ID。
    private UUID sourcePurchaseId;

    /// 来源收货单 ID。
    private UUID sourceReceiptId;

    /// 来源采购明细 ID。
    private UUID sourcePurchaseItemId;

    /// 状态。
    private String status;

    /// 创建时间。
    private Instant createdAt;
}
