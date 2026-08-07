package com.devops00.spectra.oa.supply.service;

import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplyOperationFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplyPageFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplySaveFrom;
import com.devops00.spectra.oa.supply.javabean.vo.SupplyItemVO;

/// 办公用品库存服务。
public interface SupplyService {
    IPage<SupplyItemVO> page(PageFrom page, SupplyPageFrom params);

    SupplyItemVO get(UUID id);

    UUID created(SupplySaveFrom from);

    void modify(UUID id, SupplySaveFrom from);

    void inbound(UUID id, SupplyOperationFrom from);

    void issue(UUID id, SupplyOperationFrom from);

    void returnStock(UUID id, SupplyOperationFrom from);

    void adjust(UUID id, SupplyOperationFrom from);

    List<SupplyItemVO> lowStock();
}
