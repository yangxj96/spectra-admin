package com.devops00.spectra.oa.supply.controller;

import java.util.List;
import java.util.UUID;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.Verify;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.oa.supply.javabean.from.SupplyOperationFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplyPageFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplySaveFrom;
import com.devops00.spectra.oa.supply.javabean.vo.SupplyItemVO;
import com.devops00.spectra.oa.supply.service.SupplyService;

import lombok.RequiredArgsConstructor;

/**
 * 办公用品库存接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@RestController
@RequestMapping("/oa/supplies")
@RequiredArgsConstructor
public class SupplyController {
    private final SupplyService supplyService;

    /**
     * 创建办公用品。
     */
    @ULog("'创建办公用品'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:INSERT')")
    public UUID create(@Validated(Verify.Insert.class) @RequestBody SupplySaveFrom from) {
        return supplyService.created(from);
    }

    /**
     * 修改办公用品。
     */
    @ULog("'修改办公用品'")
    @PutMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void update(@PathVariable UUID id, @Validated(Verify.Update.class) @RequestBody SupplySaveFrom from) {
        supplyService.modify(id, from);
    }

    /**
     * 分页查询办公用品库存。
     */
    @ULog("'分页查询办公用品库存'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:QUERY')")
    public IPage<SupplyItemVO> page(PageFrom page, SupplyPageFrom params) {
        return supplyService.page(page, params);
    }

    /**
     * 查询办公用品详情。
     */
    @ULog("'查询办公用品详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:QUERY')")
    public SupplyItemVO get(@PathVariable UUID id) {
        return supplyService.get(id);
    }

    /**
     * 查询低库存办公用品。
     */
    @ULog("'查询低库存办公用品'")
    @GetMapping(value = "/low-stock", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:QUERY')")
    public List<SupplyItemVO> lowStock() {
        return supplyService.lowStock();
    }

    /**
     * 办公用品入库。
     */
    @ULog("'办公用品入库'")
    @PostMapping(value = "/{id}/inbound", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void inbound(@PathVariable UUID id, @Validated @RequestBody SupplyOperationFrom from) {
        supplyService.inbound(id, from);
    }

    /**
     * 办公用品领用。
     */
    @ULog("'办公用品领用'")
    @PostMapping(value = "/{id}/issue", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void issue(@PathVariable UUID id, @Validated @RequestBody SupplyOperationFrom from) {
        supplyService.issue(id, from);
    }

    /**
     * 办公用品退库。
     */
    @ULog("'办公用品退库'")
    @PostMapping(value = "/{id}/return", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void returnStock(@PathVariable UUID id, @Validated @RequestBody SupplyOperationFrom from) {
        supplyService.returnStock(id, from);
    }

    /**
     * 办公用品盘点调整。
     */
    @ULog("'办公用品盘点调整'")
    @PostMapping(value = "/{id}/adjust", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void adjust(@PathVariable UUID id, @Validated @RequestBody SupplyOperationFrom from) {
        supplyService.adjust(id, from);
    }
}
