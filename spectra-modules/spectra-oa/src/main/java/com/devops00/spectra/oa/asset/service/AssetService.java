package com.devops00.spectra.oa.asset.service;

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

import java.util.List;
import java.util.UUID;

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
     *
     * @param page   分页条件，包含页码、页大小和排序字段。
     * @param params 资产关键字、状态、分类、所属部门和保管人等分页筛选条件。
     * @return 返回按分页条件查询的OA 资产分页；无匹配时 records 为空、total 为 0，结果对象不返回 null。
     */
    IPage<AssetVO> page(PageFrom page, AssetPageFrom params);

    /**
     * 查询资产详情。
     *
     * @param id 目标OA 业务记录的唯一标识。
     * @return 返回符合条件的OA 资产详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    AssetVO get(UUID id);

    /**
     * 创建资产。
     *
     * @param from 资产分类、编号、名称、规格、数量、取得信息、位置和保管关系等创建字段。
     * @return 返回新建资产的唯一标识；资产字段校验或写入失败时抛出业务异常，不返回 null。
     */
    UUID created(AssetSaveFrom from);

    /**
     * 修改资产基础信息。
     *
     * @param id   待修改资产的唯一标识。
     * @param from 资产分类、编号、名称、规格、数量、取得信息、位置和保管关系等修改字段。
     */
    void modify(UUID id, AssetSaveFrom from);

    /**
     * 查询资产分类。
     *
     * @return 返回符合查询条件的OA 资产分类列表；无匹配时返回空列表，不返回 null。
     */
    List<AssetCategoryVO> categories();

    /**
     * 创建资产分类。
     *
     * @param from 分类父节点、编码、名称、资产类型、排序、启用状态和描述等分类字段。
     * @return 返回新建资产分类的唯一标识；分类名称冲突或写入失败时抛出业务异常，不返回 null。
     */
    UUID createdCategory(AssetCategorySaveFrom from);

    /**
     * 修改资产分类。
     *
     * @param id   待修改资产分类的唯一标识。
     * @param from 分类父节点、编码、名称、资产类型、排序、启用状态和描述等修改字段。
     */
    void modifyCategory(UUID id, AssetCategorySaveFrom from);

    /**
     * 领用资产。
     *
     * @param id   待领用资产的唯一标识。
     * @param from 领用目标部门、领用人、位置、日期和原因等资产操作字段。
     */
    void assign(UUID id, AssetOperationFrom from);

    /**
     * 归还资产。
     *
     * @param id   待归还资产的唯一标识。
     * @param from 归还目标部门、位置、日期和归还原因等资产操作字段。
     */
    void returnAsset(UUID id, AssetOperationFrom from);

    /**
     * 转移资产使用人或部门。
     *
     * @param id   待转移资产的唯一标识。
     * @param from 新部门、新保管人、新位置、转移日期和转移原因等资产操作字段。
     */
    void transfer(UUID id, AssetOperationFrom from);

    /**
     * 登记资产维修。
     *
     * @param id   待登记维修的资产唯一标识。
     * @param from 维修内容、维修费用、处理状态、日期和原因等维修字段。
     */
    void maintenance(UUID id, AssetOperationFrom from);

    /**
     * 报废资产。
     *
     * @param id   待报废资产的唯一标识。
     * @param from 报废状态、日期和报废原因等资产处置字段。
     */
    void scrap(UUID id, AssetOperationFrom from);

    /**
     * 根据采购草稿创建资产。
     *
     * @param from 采购单、收货单和采购明细 ID，以及新资产分类 ID。
     * @return 返回根据已收货采购明细生成的资产列表；没有可生成的明细时返回空列表，不返回 null。
     */
    List<AssetVO> createFromPurchase(AssetPurchaseDraftFrom from);
}
