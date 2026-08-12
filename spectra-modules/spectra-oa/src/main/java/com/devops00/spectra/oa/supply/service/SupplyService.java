package com.devops00.spectra.oa.supply.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplyOperationFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplyPageFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplySaveFrom;
import com.devops00.spectra.oa.supply.javabean.vo.SupplyItemVO;

import java.util.List;
import java.util.UUID;

/**
 * 办公用品库存服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
public interface SupplyService {
    /**
     * 分页查询办公用品库存。
     */
    IPage<SupplyItemVO> page(PageFrom page, SupplyPageFrom params);

    /**
     * 查询办公用品详情。
     */
    SupplyItemVO get(UUID id);

    /**
     * 创建办公用品 SKU。
     */
    UUID created(SupplySaveFrom from);

    /**
     * 修改办公用品 SKU。
     */
    void modify(UUID id, SupplySaveFrom from);

    /**
     * 办公用品入库。
     */
    void inbound(UUID id, SupplyOperationFrom from);

    /**
     * 办公用品领用出库。
     */
    void issue(UUID id, SupplyOperationFrom from);

    /**
     * 办公用品退库。
     */
    void returnStock(UUID id, SupplyOperationFrom from);

    /**
     * 调整办公用品库存。
     */
    void adjust(UUID id, SupplyOperationFrom from);

    /**
     * 查询低库存办公用品。
     */
    List<SupplyItemVO> lowStock();
}
