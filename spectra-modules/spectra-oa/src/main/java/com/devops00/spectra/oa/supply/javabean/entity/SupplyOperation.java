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
    @TableField("supply_id")
    private UUID supplyId;

    @TableField("operation_type")
    private String operationType;

    @TableField("quantity")
    private BigDecimal quantity;

    @TableField("before_stock")
    private BigDecimal beforeStock;

    @TableField("after_stock")
    private BigDecimal afterStock;

    @TableField("department_id")
    private UUID departmentId;

    @TableField("user_id")
    private UUID userId;

    @TableField("location")
    private String location;

    @TableField("operation_date")
    private LocalDate operationDate;

    @TableField("reason")
    private String reason;

    @TableField("source_purchase_id")
    private UUID sourcePurchaseId;

    @TableField("source_receipt_id")
    private UUID sourceReceiptId;

    @TableField("source_purchase_item_id")
    private UUID sourcePurchaseItemId;

    @TableField("status")
    private String status;
}
