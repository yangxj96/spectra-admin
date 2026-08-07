package com.devops00.spectra.oa.asset.controller;

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
import com.devops00.spectra.oa.asset.javabean.from.AssetCategorySaveFrom;
import com.devops00.spectra.oa.asset.javabean.from.AssetOperationFrom;
import com.devops00.spectra.oa.asset.javabean.from.AssetPageFrom;
import com.devops00.spectra.oa.asset.javabean.from.AssetPurchaseDraftFrom;
import com.devops00.spectra.oa.asset.javabean.from.AssetSaveFrom;
import com.devops00.spectra.oa.asset.javabean.vo.AssetCategoryVO;
import com.devops00.spectra.oa.asset.javabean.vo.AssetVO;
import com.devops00.spectra.oa.asset.service.AssetService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/oa/assets")
@RequiredArgsConstructor
public class AssetController {

    private final AssetService assetService;

    @ULog("'创建资产'")
    @PostMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:INSERT')")
    public UUID create(@Validated(Verify.Insert.class) @RequestBody AssetSaveFrom from) {
        return assetService.created(from);
    }

    @ULog("'修改资产'")
    @PutMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void update(@PathVariable UUID id,
            @Validated(Verify.Update.class) @RequestBody AssetSaveFrom from) {
        assetService.modify(id, from);
    }

    @ULog("'分页查询资产台账'")
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:QUERY')")
    public IPage<AssetVO> page(PageFrom page, AssetPageFrom params) {
        return assetService.page(page, params);
    }

    @ULog("'查询资产详情'")
    @GetMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:QUERY')")
    public AssetVO get(@PathVariable UUID id) {
        return assetService.get(id);
    }

    @ULog("'查询资产分类'")
    @GetMapping(value = "/categories", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:QUERY')")
    public List<AssetCategoryVO> categories() {
        return assetService.categories();
    }

    @ULog("'创建资产分类'")
    @PostMapping(value = "/categories", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:INSERT')")
    public UUID createCategory(@Validated(Verify.Insert.class) @RequestBody AssetCategorySaveFrom from) {
        return assetService.createdCategory(from);
    }

    @ULog("'修改资产分类'")
    @PutMapping(value = "/categories/{id}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void updateCategory(@PathVariable UUID id,
            @Validated(Verify.Update.class) @RequestBody AssetCategorySaveFrom from) {
        assetService.modifyCategory(id, from);
    }

    @ULog("'资产领用'")
    @PostMapping(value = "/{id}/assign", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void assign(@PathVariable UUID id, @RequestBody(required = false) AssetOperationFrom from) {
        assetService.assign(id, from);
    }

    @ULog("'资产归还'")
    @PostMapping(value = "/{id}/return", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void returnAsset(@PathVariable UUID id, @RequestBody(required = false) AssetOperationFrom from) {
        assetService.returnAsset(id, from);
    }

    @ULog("'资产调拨'")
    @PostMapping(value = "/{id}/transfer", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void transfer(@PathVariable UUID id, @RequestBody(required = false) AssetOperationFrom from) {
        assetService.transfer(id, from);
    }

    @ULog("'资产维修'")
    @PostMapping(value = "/{id}/maintenance", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void maintenance(@PathVariable UUID id, @RequestBody(required = false) AssetOperationFrom from) {
        assetService.maintenance(id, from);
    }

    @ULog("'资产报废'")
    @PostMapping(value = "/{id}/scrap", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:UPDATE')")
    public void scrap(@PathVariable UUID id, @RequestBody(required = false) AssetOperationFrom from) {
        assetService.scrap(id, from);
    }

    @ULog("'采购收货生成资产草稿'")
    @PostMapping(value = "/from-purchase", version = "1.0.0+")
    @PreAuthorize("hasPermission(null, 'OA_ASSET:INSERT')")
    public List<AssetVO> createFromPurchase(@Validated(Verify.Insert.class) @RequestBody AssetPurchaseDraftFrom from) {
        return assetService.createFromPurchase(from);
    }
}
