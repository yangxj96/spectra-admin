package com.devops00.spectra.oa.asset.service;

import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.oa.asset.javabean.entity.Asset;
import com.devops00.spectra.oa.asset.javabean.from.AssetCategorySaveFrom;
import com.devops00.spectra.oa.asset.javabean.from.AssetOperationFrom;
import com.devops00.spectra.oa.asset.javabean.from.AssetPageFrom;
import com.devops00.spectra.oa.asset.javabean.from.AssetPurchaseDraftFrom;
import com.devops00.spectra.oa.asset.javabean.from.AssetSaveFrom;
import com.devops00.spectra.oa.asset.javabean.vo.AssetCategoryVO;
import com.devops00.spectra.oa.asset.javabean.vo.AssetVO;

public interface AssetService extends BaseService<Asset> {

    IPage<AssetVO> page(PageFrom page, AssetPageFrom params);

    AssetVO get(UUID id);

    UUID created(AssetSaveFrom from);

    void modify(UUID id, AssetSaveFrom from);

    List<AssetCategoryVO> categories();

    UUID createdCategory(AssetCategorySaveFrom from);

    void modifyCategory(UUID id, AssetCategorySaveFrom from);

    void assign(UUID id, AssetOperationFrom from);

    void returnAsset(UUID id, AssetOperationFrom from);

    void transfer(UUID id, AssetOperationFrom from);

    void maintenance(UUID id, AssetOperationFrom from);

    void scrap(UUID id, AssetOperationFrom from);

    List<AssetVO> createFromPurchase(AssetPurchaseDraftFrom from);
}
