package com.devops00.spectra.oa.supply.javabean.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/**
 * 办公用品库存响应视图。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class SupplyItemVO {

    /**
     * 主键 ID。
     */
    private UUID id;

    /**
     * 分类。
     */
    private String category;

    /**
     * SKU 编码。
     */
    private String sku;

    /**
     * 名称。
     */
    private String name;

    /**
     * 规格。
     */
    private String specification;

    /**
     * 单位。
     */
    private String unit;

    /**
     * 当前库存。
     */
    private BigDecimal currentStock;

    /**
     * 最低库存。
     */
    private BigDecimal minStock;

    /**
     * 是否低库存。
     */
    private Boolean lowStock;

    /**
     * 状态。
     */
    private String status;

    /**
     * 供应商。
     */
    private String supplier;

    /**
     * 位置。
     */
    private String location;

    /**
     * 部门 ID。
     */
    private UUID departmentId;

    /**
     * 备注。
     */
    private String remark;

    /**
     * 创建时间。
     */
    private List<SupplyOperationVO> operations = List.of();
    private LocalDateTime createdAt;

    /**
     * 更新时间。
     */
    private LocalDateTime updatedAt;
}
