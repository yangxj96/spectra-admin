package com.devops00.spectra.oa.supply.javabean.from;

import lombok.Data;

/// 办公用品库存分页查询参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/9
@Data
public class SupplyPageFrom {
    private String keyword;
    private String category;
    private String status;
    private Boolean lowStock;
}
