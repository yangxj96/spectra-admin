package com.devops00.spectra.oa.supply.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.annotation.DataScope;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * 办公用品 SKU 与库存实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Getter
@Setter
@EqualsAndHashCode(callSuper = true)
@TableName(value = "oa_supply_item", schema = "spectra_oa")
@DataScope
public class SupplyItem extends BaseEntity {

    /**
     * 分类。
     */
    @TableField("category")
    private String category;

    /**
     * SKU 编码。
     */
    @TableField("sku")
    private String sku;

    /**
     * 名称。
     */
    @TableField("name")
    private String name;

    /**
     * 规格。
     */
    @TableField("specification")
    private String specification;

    /**
     * 单位。
     */
    @TableField("unit")
    private String unit;

    /**
     * 当前库存。
     */
    @TableField("current_stock")
    private BigDecimal currentStock;

    /**
     * 最低库存。
     */
    @TableField("min_stock")
    private BigDecimal minStock;

    /**
     * 状态。
     */
    @TableField("status")
    private String status;

    /**
     * 供应商。
     */
    @TableField("supplier")
    private String supplier;

    /**
     * 位置。
     */
    @TableField("location")
    private String location;

    /**
     * 部门 ID。
     */
    @TableField("department_id")
    private UUID departmentId;

    /**
     * 备注。
     */
    @TableField("remark")
    private String remark;
}
