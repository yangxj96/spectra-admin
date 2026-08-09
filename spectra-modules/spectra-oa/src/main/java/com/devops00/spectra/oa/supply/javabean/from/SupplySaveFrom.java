package com.devops00.spectra.oa.supply.javabean.from;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 办公用品 SKU 保存参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class SupplySaveFrom {

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
    @NotBlank(message = "办公用品 SKU 不能为空")
    private String sku;

    /**
     * 名称。
     */
    @NotBlank(message = "办公用品名称不能为空")
    private String name;

    /**
     * 规格。
     */
    private String specification;

    /**
     * 单位。
     */
    @NotBlank(message = "办公用品单位不能为空")
    private String unit = "件";

    /**
     * 最低库存。
     */
    @NotNull(message = "最低库存不能为空")
    @DecimalMin(value = "0", message = "最低库存不能小于 0")
    private BigDecimal minStock = BigDecimal.ZERO;

    /**
     * 状态。
     */
    private String status = "ACTIVE";

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
}
