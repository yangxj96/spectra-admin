package com.devops00.spectra.oa.supply.javabean.vo;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import lombok.Data;

/// 办公用品库存响应视图。
@Data
public class SupplyItemVO {
    private UUID id;
    private String category;
    private String sku;
    private String name;
    private String specification;
    private String unit;
    private BigDecimal currentStock;
    private BigDecimal minStock;
    private Boolean lowStock;
    private String status;
    private String supplier;
    private String location;
    private UUID departmentId;
    private String remark;
    private List<SupplyOperationVO> operations = List.of();
    private Instant createdAt;
    private Instant updatedAt;
}
