package com.devops00.spectra.oa.supply.javabean.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/// 办公用品库存变动记录。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_supply_operation", schema = "spectra_oa")
public class SupplyOperation extends BaseEntity {

    /// 办公用品 ID。
    @TableField("supply_id")
    private UUID supplyId;

    /// 操作类型。
    @TableField("operation_type")
    private String operationType;

    /// 数量。
    @TableField("quantity")
    private BigDecimal quantity;

    /// 操作前库存。
    @TableField("before_stock")
    private BigDecimal beforeStock;

    /// 操作后库存。
    @TableField("after_stock")
    private BigDecimal afterStock;

    /// 部门 ID。
    @TableField("department_id")
    private UUID departmentId;

    /// 用户 ID。
    @TableField("user_id")
    private UUID userId;

    /// 位置。
    @TableField("location")
    private String location;

    /// 操作时间。
    @TableField("operation_date")
    private LocalDate operationDate;

    /// 原因。
    @TableField("reason")
    private String reason;

    /// 来源采购单 ID。
    @TableField("source_purchase_id")
    private UUID sourcePurchaseId;

    /// 来源收货单 ID。
    @TableField("source_receipt_id")
    private UUID sourceReceiptId;

    /// 来源采购明细 ID。
    @TableField("source_purchase_item_id")
    private UUID sourcePurchaseItemId;

    /// 状态。
    @TableField("status")
    private String status;
}
