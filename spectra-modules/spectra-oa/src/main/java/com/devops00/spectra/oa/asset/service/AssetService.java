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

/**
 * 资产管理业务服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
public interface AssetService extends BaseService<Asset> {

    /**
     * 分页查询资产。
     */
    IPage<AssetVO> page(PageFrom page, AssetPageFrom params);

    /**
     * 查询资产详情。
     */
    AssetVO get(UUID id);

    /**
     * 创建资产。
     */
    UUID created(AssetSaveFrom from);

    /**
     * 修改资产基础信息。
     */
    void modify(UUID id, AssetSaveFrom from);

    /**
     * 查询资产分类。
     */
    List<AssetCategoryVO> categories();

    /**
     * 创建资产分类。
     */
    UUID createdCategory(AssetCategorySaveFrom from);

    /**
     * 修改资产分类。
     */
    void modifyCategory(UUID id, AssetCategorySaveFrom from);

    /**
     * 领用资产。
     */
    void assign(UUID id, AssetOperationFrom from);

    /**
     * 归还资产。
     */
    void returnAsset(UUID id, AssetOperationFrom from);

    /**
     * 转移资产使用人或部门。
     */
    void transfer(UUID id, AssetOperationFrom from);

    /**
     * 登记资产维修。
     */
    void maintenance(UUID id, AssetOperationFrom from);

    /**
     * 报废资产。
     */
    void scrap(UUID id, AssetOperationFrom from);

    /**
     * 根据采购草稿创建资产。
     */
    List<AssetVO> createFromPurchase(AssetPurchaseDraftFrom from);
}
