package com.devops00.spectra.oa.supply.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.supply.javabean.converter.SupplyConverter;
import com.devops00.spectra.oa.supply.javabean.entity.SupplyItem;
import com.devops00.spectra.oa.supply.javabean.entity.SupplyOperation;
import com.devops00.spectra.oa.supply.javabean.from.SupplyOperationFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplyPageFrom;
import com.devops00.spectra.oa.supply.javabean.from.SupplySaveFrom;
import com.devops00.spectra.oa.supply.javabean.vo.SupplyItemVO;
import com.devops00.spectra.oa.supply.mapper.SupplyItemMapper;
import com.devops00.spectra.oa.supply.mapper.SupplyOperationMapper;
import com.devops00.spectra.oa.supply.service.SupplyService;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 办公用品库存服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Service
@RequiredArgsConstructor
public class SupplyServiceImpl extends BaseServiceImpl<SupplyItemMapper, SupplyItem> implements SupplyService {
    private static final String STATUS_ACTIVE = "ACTIVE";
    private static final String STATUS_INACTIVE = "INACTIVE";
    private static final String OP_INBOUND = "INBOUND";
    private static final String OP_ISSUE = "ISSUE";
    private static final String OP_RETURN = "RETURN";
    private static final String OP_ADJUST = "ADJUST";

    private final SupplyOperationMapper operationMapper;
    private final SupplyConverter supplyConverter;
    private final TimeMapper timeMapper;
    private final SecurityContextAccessor securityContextAccessor;

    @Override
    public IPage<SupplyItemVO> page(PageFrom page, SupplyPageFrom params) {
        var wrapper = new LambdaQueryWrapper<SupplyItem>();
        if (params != null) {
            if (StringUtils.hasText(params.getKeyword())) {
                wrapper.and(query -> query.like(SupplyItem::getSku, params.getKeyword())
                        .or()
                        .like(SupplyItem::getName, params.getKeyword())
                        .or()
                        .like(SupplyItem::getSpecification, params.getKeyword()));
            }
            if (StringUtils.hasText(params.getCategory())) {
                wrapper.eq(SupplyItem::getCategory, params.getCategory());
            }
            if (StringUtils.hasText(params.getStatus())) {
                wrapper.eq(SupplyItem::getStatus, params.getStatus());
            }
            if (Boolean.TRUE.equals(params.getLowStock())) {
                wrapper.apply("current_stock <= min_stock");
            }
        }
        wrapper.orderByAsc(SupplyItem::getName).orderByAsc(SupplyItem::getSku);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<SupplyItemVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(item -> assembleView(item, false)).toList());
        return voPage;
    }

    @Override
    public SupplyItemVO get(UUID id) {
        return assembleView(require(id), true);
    }

    @Override
    @Transactional
    public UUID created(SupplySaveFrom from) {
        validateSku(from.getSku(), null);
        var entity = supplyConverter.toEntity(from);
        applyDefaults(entity);
        if (!this.save(entity)) {
            throw new DataSaveException("保存办公用品失败");
        }
        return entity.getId();
    }

    @Override
    @Transactional
    public void modify(UUID id, SupplySaveFrom from) {
        var entity = require(id);
        validateSku(from.getSku(), id);
        supplyConverter.updateEntity(from, entity);
        applyDefaults(entity);
        if (!this.updateById(entity)) {
            throw new DataSaveException("更新办公用品失败");
        }
    }

    @Override
    @Transactional
    public void inbound(UUID id, SupplyOperationFrom from) {
        var operationFrom = requireFrom(from);
        var quantity = positiveQuantity(operationFrom);
        change(id, OP_INBOUND, quantity, operationFrom);
    }

    @Override
    @Transactional
    public void issue(UUID id, SupplyOperationFrom from) {
        var operationFrom = requireFrom(from);
        var quantity = positiveQuantity(operationFrom).negate();
        if (operationFrom.getUserId() == null) {
            operationFrom.setUserId(securityContextAccessor.currentUserId());
        }
        change(id, OP_ISSUE, quantity, operationFrom);
    }

    @Override
    @Transactional
    public void returnStock(UUID id, SupplyOperationFrom from) {
        var operationFrom = requireFrom(from);
        var quantity = positiveQuantity(operationFrom);
        if (operationFrom.getUserId() == null) {
            operationFrom.setUserId(securityContextAccessor.currentUserId());
        }
        change(id, OP_RETURN, quantity, operationFrom);
    }

    @Override
    @Transactional
    public void adjust(UUID id, SupplyOperationFrom from) {
        var operationFrom = requireFrom(from);
        if (operationFrom.getTargetStock() == null || operationFrom.getTargetStock().signum() < 0) {
            throw new DataSaveException("盘点调整后的库存不能为空且不能小于 0");
        }
        var entity = require(id);
        change(entity, OP_ADJUST, operationFrom.getTargetStock().subtract(stock(entity)), operationFrom);
    }

    @Override
    public List<SupplyItemVO> lowStock() {
        return this.list(new LambdaQueryWrapper<SupplyItem>().eq(SupplyItem::getStatus, STATUS_ACTIVE)
                .apply("current_stock <= min_stock")
                .orderByAsc(SupplyItem::getName)).stream().map(item -> assembleView(item, false)).toList();
    }

    private void change(UUID id, String type, BigDecimal delta, SupplyOperationFrom from) {
        change(require(id), type, delta, from);
    }

    private void change(SupplyItem entity, String type, BigDecimal delta, SupplyOperationFrom from) {
        if (STATUS_INACTIVE.equals(entity.getStatus())) {
            throw new DataSaveException("停用的办公用品不能变更库存");
        }
        var before = stock(entity);
        var after = before.add(delta);
        if (after.signum() < 0) {
            throw new DataSaveException("库存不足，无法领用");
        }
        entity.setCurrentStock(after);
        var operation = new SupplyOperation();
        operation.setSupplyId(entity.getId());
        operation.setOperationType(type);
        operation.setQuantity(delta);
        operation.setBeforeStock(before);
        operation.setAfterStock(after);
        operation.setDepartmentId(from.getDepartmentId());
        operation.setUserId(from.getUserId());
        operation.setLocation(StringUtils.hasText(from.getLocation()) ? from.getLocation() : entity.getLocation());
        operation.setOperationDate(from.getOperationDate() == null ? Instant.now() : timeMapper.toInstant(from.getOperationDate()));
        operation.setReason(from.getReason());
        operation.setSourcePurchaseId(from.getSourcePurchaseId());
        operation.setSourceReceiptId(from.getSourceReceiptId());
        operation.setSourcePurchaseItemId(from.getSourcePurchaseItemId());
        operation.setStatus("COMPLETE");
        if (!this.updateById(entity) || operationMapper.insert(operation) != 1) {
            throw new DataSaveException("保存办公用品库存变动失败");
        }
    }

    private BigDecimal positiveQuantity(SupplyOperationFrom from) {
        if (from.getQuantity() == null || from.getQuantity().signum() <= 0) {
            throw new DataSaveException("库存变动数量必须大于 0");
        }
        return from.getQuantity();
    }

    private SupplyOperationFrom requireFrom(SupplyOperationFrom from) {
        if (from == null) {
            throw new DataSaveException("库存变动参数不能为空");
        }
        return from;
    }

    private SupplyItem require(UUID id) {
        var entity = this.getById(id);
        if (entity == null) {
            throw new DataNotExistException("办公用品不存在: " + id);
        }
        return entity;
    }

    private BigDecimal stock(SupplyItem entity) {
        return entity.getCurrentStock() == null ? BigDecimal.ZERO : entity.getCurrentStock();
    }

    private void applyDefaults(SupplyItem entity) {
        if (entity.getCurrentStock() == null) {
            entity.setCurrentStock(BigDecimal.ZERO);
        }
        if (entity.getMinStock() == null) {
            entity.setMinStock(BigDecimal.ZERO);
        }
        if (!StringUtils.hasText(entity.getUnit())) {
            entity.setUnit("件");
        }
        if (!StringUtils.hasText(entity.getStatus())) {
            entity.setStatus(STATUS_ACTIVE);
        }
    }

    private void validateSku(String sku, UUID id) {
        if (!StringUtils.hasText(sku)) {
            return;
        }
        var query = new LambdaQueryWrapper<SupplyItem>().eq(SupplyItem::getSku, sku);
        if (id != null) {
            query.ne(SupplyItem::getId, id);
        }
        if (this.count(query) > 0) {
            throw new DataSaveException("办公用品 SKU 已存在");
        }
    }

    private SupplyItemVO assembleView(SupplyItem entity, boolean withOperations) {
        var vo = supplyConverter.toVO(entity);
        vo.setLowStock(stock(entity).compareTo(entity.getMinStock() == null ? BigDecimal.ZERO : entity.getMinStock()) <= 0);
        if (withOperations) {
            vo.setOperations(operationMapper
                    .selectList(new LambdaQueryWrapper<SupplyOperation>().eq(SupplyOperation::getSupplyId, entity.getId())
                            .orderByDesc(SupplyOperation::getOperationDate)
                            .orderByDesc(SupplyOperation::getCreatedAt))
                    .stream()
                    .map(supplyConverter::toOperationVO)
                    .toList());
        }
        return vo;
    }
}
