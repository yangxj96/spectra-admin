package com.devops00.spectra.oa.supply.javabean.from;

import lombok.Data;

/// 办公用品库存分页查询参数。
@Data
public class SupplyPageFrom {
    private String keyword;
    private String category;
    private String status;
    private Boolean lowStock;
}
