package com.devops00.spectra.oa.supply.javabean.from;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

/// 办公用品 SKU 保存参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Data
public class SupplySaveFrom {
    private UUID id;

    private String category;

    @NotBlank(message = "办公用品 SKU 不能为空")
    private String sku;

    @NotBlank(message = "办公用品名称不能为空")
    private String name;

    private String specification;

    @NotBlank(message = "办公用品单位不能为空")
    private String unit = "件";

    @NotNull(message = "最低库存不能为空")
    @DecimalMin(value = "0", message = "最低库存不能小于 0")
    private BigDecimal minStock = BigDecimal.ZERO;

    private String status = "ACTIVE";
    private String supplier;
    private String location;
    private UUID departmentId;
    private String remark;
}
