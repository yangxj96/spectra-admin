package com.devops00.spectra.oa.asset.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.asset.javabean.converter.AssetConverter;
import com.devops00.spectra.oa.asset.javabean.constant.AssetOperationStatus;
import com.devops00.spectra.oa.asset.javabean.constant.AssetOperationType;
import com.devops00.spectra.oa.asset.javabean.constant.AssetStatus;
import com.devops00.spectra.oa.asset.javabean.entity.Asset;
import com.devops00.spectra.oa.asset.javabean.entity.AssetCategory;
import com.devops00.spectra.oa.asset.javabean.entity.AssetOperation;
import com.devops00.spectra.oa.asset.javabean.from.*;
import com.devops00.spectra.oa.asset.javabean.vo.AssetCategoryVO;
import com.devops00.spectra.oa.asset.javabean.vo.AssetVO;
import com.devops00.spectra.oa.asset.mapper.AssetCategoryMapper;
import com.devops00.spectra.oa.asset.mapper.AssetMapper;
import com.devops00.spectra.oa.asset.mapper.AssetOperationMapper;
import com.devops00.spectra.oa.asset.service.AssetService;
import com.devops00.spectra.oa.purchase.javabean.entity.PurchaseItem;
import com.devops00.spectra.oa.purchase.javabean.entity.PurchaseReceipt;
import com.devops00.spectra.oa.purchase.javabean.entity.PurchaseReceiptItem;
import com.devops00.spectra.oa.purchase.mapper.PurchaseItemMapper;
import com.devops00.spectra.oa.purchase.mapper.PurchaseMapper;
import com.devops00.spectra.oa.purchase.mapper.PurchaseReceiptItemMapper;
import com.devops00.spectra.oa.purchase.mapper.PurchaseReceiptMapper;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 资产管理业务服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Service
@RequiredArgsConstructor
public class AssetServiceImpl extends BaseServiceImpl<AssetMapper, Asset> implements AssetService {

    private final AssetCategoryMapper categoryMapper;
    private final AssetOperationMapper operationMapper;
    private final PurchaseMapper purchaseMapper;
    private final PurchaseItemMapper purchaseItemMapper;
    private final PurchaseReceiptMapper receiptMapper;
    private final PurchaseReceiptItemMapper receiptItemMapper;
    private final AssetConverter assetConverter;
    private final TimeMapper timeMapper;
    private final SecurityContextAccessor securityContextAccessor;

    @Override
    public IPage<AssetVO> page(PageFrom page, AssetPageFrom params) {
        var wrapper = new LambdaQueryWrapper<Asset>();
        if (params != null) {
            if (StringUtils.hasText(params.getKeyword())) {
                wrapper.and(query -> query.like(Asset::getAssetNo, params.getKeyword())
                        .or()
                        .like(Asset::getName, params.getKeyword())
                        .or()
                        .like(Asset::getSerialNo, params.getKeyword()));
            }
            if (StringUtils.hasText(params.getStatus())) {
                wrapper.eq(Asset::getStatus, params.getStatus());
            }
            if (params.getCategoryId() != null) {
                wrapper.eq(Asset::getCategoryId, params.getCategoryId());
            }
            if (params.getDepartmentId() != null) {
                wrapper.eq(Asset::getDepartmentId, params.getDepartmentId());
            }
            if (params.getCustodianId() != null) {
                wrapper.eq(Asset::getCustodianId, params.getCustodianId());
            }
        }
        wrapper.orderByDesc(Asset::getCreatedAt);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<AssetVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::assembleView).toList());
        return voPage;
    }

    @Override
    public AssetVO get(UUID id) {
        return assembleView(require(id));
    }

    @Override
    @Transactional
    public UUID created(AssetSaveFrom from) {
        validateAssetNo(from.getAssetNo(), null);
        var entity = assetConverter.toEntity(from);
        applyDefaults(entity);
        if (!this.save(entity)) {
            throw new DataSaveException("保存资产失败");
        }
        return entity.getId();
    }

    @Override
    @Transactional
    public void modify(UUID id, AssetSaveFrom from) {
        var entity = require(id);
        validateAssetNo(from.getAssetNo(), id);
        assetConverter.updateEntity(from, entity);
        applyDefaults(entity);
        if (!this.updateById(entity)) {
            throw new DataSaveException("更新资产失败");
        }
    }

    @Override
    public List<AssetCategoryVO> categories() {
        return categoryMapper.selectList(new LambdaQueryWrapper<AssetCategory>().eq(AssetCategory::getEnabled, true)
                .orderByAsc(AssetCategory::getSort)
                .orderByAsc(AssetCategory::getCode)).stream().map(assetConverter::toCategoryVO).toList();
    }

    @Override
    @Transactional
    public UUID createdCategory(AssetCategorySaveFrom from) {
        var duplicate = categoryMapper.selectOne(new LambdaQueryWrapper<AssetCategory>().eq(AssetCategory::getCode, from.getCode()));
        if (duplicate != null) {
            throw new DataSaveException("资产分类编码已存在");
        }
        var entity = assetConverter.toCategoryEntity(from);
        if (entity.getSort() == null) {
            entity.setSort(0);
        }
        if (entity.getEnabled() == null) {
            entity.setEnabled(true);
        }
        if (categoryMapper.insert(entity) != 1) {
            throw new DataSaveException("保存资产分类失败");
        }
        return entity.getId();
    }

    @Override
    @Transactional
    public void modifyCategory(UUID id, AssetCategorySaveFrom from) {
        var entity = categoryMapper.selectById(id);
        if (entity == null) {
            throw new DataNotExistException("资产分类不存在: " + id);
        }
        var duplicate = categoryMapper
                .selectOne(new LambdaQueryWrapper<AssetCategory>().eq(AssetCategory::getCode, from.getCode()).ne(AssetCategory::getId, id));
        if (duplicate != null) {
            throw new DataSaveException("资产分类编码已存在");
        }
        assetConverter.updateCategoryEntity(from, entity);
        if (categoryMapper.updateById(entity) != 1) {
            throw new DataSaveException("更新资产分类失败");
        }
    }

    @Override
    @Transactional
    public void assign(UUID id, AssetOperationFrom from) {
        var entity = require(id);
        ensureOperable(entity);
        if (AssetStatus.IN_USE.getValue().equals(entity.getStatus())
                && entity.getCustodianId() != null
                && (from == null || from.getToUserId() == null || !Objects.equals(entity.getCustodianId(), from.getToUserId()))) {
            throw new DataSaveException("资产已被其他人员领用");
        }
        var operation = operation(entity, AssetOperationType.ASSIGN.getValue(), from);
        operation.setToDepartmentId(from == null || from.getToDepartmentId() == null ? entity.getDepartmentId() : from.getToDepartmentId());
        var targetUserId = from == null || from.getToUserId() == null ? securityContextAccessor.currentUserId() : from.getToUserId();
        if (targetUserId == null) {
            throw new DataSaveException("领用人不能为空");
        }
        operation.setToUserId(targetUserId);
        operation.setToLocation(from == null || !StringUtils.hasText(from.getToLocation()) ? entity.getLocation() : from.getToLocation());
        entity.setDepartmentId(operation.getToDepartmentId());
        entity.setCustodianId(operation.getToUserId());
        entity.setLocation(operation.getToLocation());
        entity.setStatus(AssetStatus.IN_USE.getValue());
        saveOperation(entity, operation);
    }

    @Override
    @Transactional
    public void returnAsset(UUID id, AssetOperationFrom from) {
        var entity = require(id);
        ensureOperable(entity);
        var operation = operation(entity, AssetOperationType.RETURN.getValue(), from);
        entity.setStatus(AssetStatus.IN_STOCK.getValue());
        entity.setCustodianId(null);
        operation.setToDepartmentId(entity.getDepartmentId());
        operation.setToLocation(from == null || !StringUtils.hasText(from.getToLocation()) ? entity.getLocation() : from.getToLocation());
        entity.setLocation(operation.getToLocation());
        saveOperation(entity, operation);
    }

    @Override
    @Transactional
    public void transfer(UUID id, AssetOperationFrom from) {
        var entity = require(id);
        ensureOperable(entity);
        var operation = operation(entity, AssetOperationType.TRANSFER.getValue(), from);
        operation.setToDepartmentId(from == null || from.getToDepartmentId() == null ? entity.getDepartmentId() : from.getToDepartmentId());
        operation.setToUserId(from == null || from.getToUserId() == null ? entity.getCustodianId() : from.getToUserId());
        operation.setToLocation(from == null || !StringUtils.hasText(from.getToLocation()) ? entity.getLocation() : from.getToLocation());
        entity.setDepartmentId(operation.getToDepartmentId());
        entity.setCustodianId(operation.getToUserId());
        entity.setLocation(operation.getToLocation());
        if (AssetStatus.DRAFT.getValue().equals(entity.getStatus())) {
            entity.setStatus(AssetStatus.IN_STOCK.getValue());
        }
        saveOperation(entity, operation);
    }

    @Override
    @Transactional
    public void maintenance(UUID id, AssetOperationFrom from) {
        var entity = require(id);
        ensureOperable(entity);
        var operation = operation(entity, AssetOperationType.MAINTENANCE.getValue(), from);
        operation.setMaintenanceContent(from == null ? null : from.getMaintenanceContent());
        operation.setMaintenanceCost(from == null ? null : from.getMaintenanceCost());
        operation.setStatus(from != null && AssetOperationStatus.COMPLETE.getValue().equalsIgnoreCase(from.getStatus())
                ? AssetOperationStatus.COMPLETE.getValue()
                : AssetOperationStatus.STARTED.getValue());
        entity.setStatus(AssetOperationStatus.COMPLETE.getValue().equalsIgnoreCase(operation.getStatus())
                ? entity.getCustodianId() == null ? AssetStatus.IN_STOCK.getValue() : AssetStatus.IN_USE.getValue()
                : AssetStatus.MAINTENANCE.getValue());
        saveOperation(entity, operation);
    }

    @Override
    @Transactional
    public void scrap(UUID id, AssetOperationFrom from) {
        var entity = require(id);
        if (AssetStatus.SCRAPPED.getValue().equals(entity.getStatus())) {
            throw new DataSaveException("资产已经报废");
        }
        var operation = operation(entity, AssetOperationType.SCRAP.getValue(), from);
        entity.setStatus(AssetStatus.SCRAPPED.getValue());
        entity.setCustodianId(null);
        saveOperation(entity, operation);
    }

    @Override
    @Transactional
    public List<AssetVO> createFromPurchase(AssetPurchaseDraftFrom from) {
        var purchase = purchaseMapper.selectById(from.getPurchaseId());
        if (purchase == null) {
            throw new DataNotExistException("采购申请不存在: " + from.getPurchaseId());
        }
        var receipt = receiptMapper.selectById(from.getReceiptId());
        if (receipt == null || !from.getPurchaseId().equals(receipt.getPurchaseId())) {
            throw new DataNotExistException("采购收货单不存在或不属于该采购申请");
        }
        var itemMap = purchaseItemMapper.selectList(new LambdaQueryWrapper<PurchaseItem>().eq(PurchaseItem::getPurchaseId, purchase.getId()))
                .stream()
                .collect(Collectors.toMap(PurchaseItem::getId, item -> item));
        var result = new ArrayList<AssetVO>();
        var receiptItems = receiptItemMapper
                .selectList(new LambdaQueryWrapper<PurchaseReceiptItem>().eq(PurchaseReceiptItem::getReceiptId, receipt.getId()));
        for (var receiptItem : receiptItems) {
            if (Boolean.FALSE.equals(receiptItem.getAccepted())) {
                continue;
            }
            var purchaseItem = itemMap.get(receiptItem.getPurchaseItemId());
            if (purchaseItem == null || "SERVICE".equalsIgnoreCase(purchaseItem.getItemType())) {
                continue;
            }
            var existing = this.getOne(new LambdaQueryWrapper<Asset>().eq(Asset::getSourceReceiptId, receipt.getId())
                    .eq(Asset::getSourcePurchaseItemId, purchaseItem.getId()), false);
            if (existing != null) {
                result.add(assembleView(existing));
                continue;
            }
            var entity = new Asset();
            entity.setCategoryId(from.getCategoryId());
            entity.setName(purchaseItem.getItemName());
            entity.setSpecification(purchaseItem.getSpecification());
            entity.setAssetType("FIXED");
            entity.setStatus(AssetStatus.DRAFT.getValue());
            entity.setQuantity(receiptItem.getQuantity() == null ? BigDecimal.ONE : receiptItem.getQuantity());
            entity.setAcquisitionDate(receipt.getReceivedDate());
            entity.setAcquisitionAmount(purchaseItem.getEstimatedUnitPrice() == null
                    ? BigDecimal.ZERO
                    : purchaseItem.getEstimatedUnitPrice().multiply(entity.getQuantity()));
            entity.setCurrency("CNY");
            entity.setDepartmentId(purchase.getDepartmentId());
            entity.setSourcePurchaseId(purchase.getId());
            entity.setSourceReceiptId(receipt.getId());
            entity.setSourcePurchaseItemId(purchaseItem.getId());
            entity.setRemark("由采购收货自动生成，请补充资产编号");
            if (!this.save(entity)) {
                throw new DataSaveException("生成资产草稿失败");
            }
            saveInboundOperation(entity, receipt);
            result.add(assembleView(entity));
        }
        return result;
    }

    private void applyDefaults(Asset entity) {
        if (!StringUtils.hasText(entity.getAssetType())) {
            entity.setAssetType("FIXED");
        }
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus(AssetStatus.DRAFT.getValue());
        }
        if (entity.getQuantity() == null) {
            entity.setQuantity(BigDecimal.ONE);
        }
        if (entity.getAcquisitionAmount() == null) {
            entity.setAcquisitionAmount(BigDecimal.ZERO);
        }
        if (!StringUtils.hasText(entity.getCurrency())) {
            entity.setCurrency("CNY");
        }
    }

    private void validateAssetNo(String assetNo, UUID id) {
        if (!StringUtils.hasText(assetNo)) {
            return;
        }
        var query = new LambdaQueryWrapper<Asset>().eq(Asset::getAssetNo, assetNo);
        if (id != null) {
            query.ne(Asset::getId, id);
        }
        if (this.count(query) > 0) {
            throw new DataSaveException("资产编号已存在");
        }
    }

    private Asset require(UUID id) {
        var entity = this.getById(id);
        if (entity == null) {
            throw new DataNotExistException("资产不存在: " + id);
        }
        return entity;
    }

    private void ensureOperable(Asset entity) {
        if (AssetStatus.SCRAPPED.getValue().equals(entity.getStatus())) {
            throw new DataSaveException("已报废资产不能继续操作");
        }
    }

    private AssetOperation operation(Asset entity, String type, AssetOperationFrom from) {
        var operation = new AssetOperation();
        operation.setAssetId(entity.getId());
        operation.setOperationType(type);
        operation.setFromDepartmentId(entity.getDepartmentId());
        operation.setFromUserId(entity.getCustodianId());
        operation.setFromLocation(entity.getLocation());
        operation.setOperationDate(from == null || from.getOperationDate() == null ? Instant.now() : timeMapper.toInstant(from.getOperationDate()));
        operation.setReason(from == null ? null : from.getReason());
        operation.setStatus(AssetOperationStatus.COMPLETE.getValue());
        return operation;
    }

    private void saveOperation(Asset entity, AssetOperation operation) {
        if (!this.updateById(entity) || operationMapper.insert(operation) != 1) {
            throw new DataSaveException("保存资产生命周期操作失败");
        }
    }

    private void saveInboundOperation(Asset entity, PurchaseReceipt receipt) {
        var operation = new AssetOperation();
        operation.setAssetId(entity.getId());
        operation.setOperationType(AssetOperationType.INBOUND.getValue());
        operation.setToDepartmentId(entity.getDepartmentId());
        operation.setOperationDate(receipt.getReceivedDate() == null ? Instant.now() : receipt.getReceivedDate());
        operation.setReason("采购收货单 " + receipt.getReceiptNo());
        operation.setStatus(AssetOperationStatus.COMPLETE.getValue());
        if (operationMapper.insert(operation) != 1) {
            throw new DataSaveException("保存资产入库记录失败");
        }
    }

    private AssetVO assembleView(Asset entity) {
        var vo = assetConverter.toVO(entity);
        if (entity.getCategoryId() != null) {
            var category = categoryMapper.selectById(entity.getCategoryId());
            if (category != null) {
                vo.setCategoryName(category.getName());
            }
        }
        var operations = operationMapper.selectList(new LambdaQueryWrapper<AssetOperation>().eq(AssetOperation::getAssetId, entity.getId())
                .orderByDesc(AssetOperation::getOperationDate)
                .orderByDesc(AssetOperation::getCreatedAt));
        vo.setOperations(operations.stream().map(assetConverter::toOperationVO).toList());
        return vo;
    }
}
