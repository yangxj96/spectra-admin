package com.devops00.spectra.oa.supply.javabean.from;

import lombok.Data;

/**
 * 办公用品库存分页查询参数。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Data
public class SupplyPageFrom {

    /**
     * 搜索关键字。
     */
    private String keyword;

    /**
     * 分类。
     */
    private String category;

    /**
     * 状态。
     */
    private String status;

    /**
     * 是否低库存。
     */
    private Boolean lowStock;
}
