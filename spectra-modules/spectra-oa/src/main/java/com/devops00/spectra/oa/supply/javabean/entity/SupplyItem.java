package com.devops00.spectra.oa.supply.javabean.entity;

import java.math.BigDecimal;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

/// 办公用品 SKU 与库存实体。
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_supply_item", schema = "spectra_oa")
@DataScope
public class SupplyItem extends BaseEntity {
    @TableField("category")
    private String category;

    @TableField("sku")
    private String sku;

    @TableField("name")
    private String name;

    @TableField("specification")
    private String specification;

    @TableField("unit")
    private String unit;

    @TableField("current_stock")
    private BigDecimal currentStock;

    @TableField("min_stock")
    private BigDecimal minStock;

    @TableField("status")
    private String status;

    @TableField("supplier")
    private String supplier;

    @TableField("location")
    private String location;

    @TableField("department_id")
    private UUID departmentId;

    @TableField("remark")
    private String remark;
}
