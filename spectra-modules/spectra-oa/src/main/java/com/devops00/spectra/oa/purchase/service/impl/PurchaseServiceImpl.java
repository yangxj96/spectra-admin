/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.oa.purchase.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.application.javabean.constant.ApplicationStatus;
import com.devops00.spectra.oa.application.javabean.entity.Application;
import com.devops00.spectra.oa.application.javabean.entity.ApplicationType;
import com.devops00.spectra.oa.application.mapper.ApplicationMapper;
import com.devops00.spectra.oa.application.mapper.ApplicationTypeMapper;
import com.devops00.spectra.oa.application.service.ApplicationService;
import com.devops00.spectra.oa.purchase.javabean.converter.PurchaseConverter;
import com.devops00.spectra.oa.purchase.javabean.entity.Purchase;
import com.devops00.spectra.oa.purchase.javabean.entity.PurchaseItem;
import com.devops00.spectra.oa.purchase.javabean.entity.PurchaseReceipt;
import com.devops00.spectra.oa.purchase.javabean.entity.PurchaseReceiptItem;
import com.devops00.spectra.oa.purchase.javabean.from.*;
import com.devops00.spectra.oa.purchase.javabean.vo.PurchaseReceiptVO;
import com.devops00.spectra.oa.purchase.javabean.vo.PurchaseVO;
import com.devops00.spectra.oa.purchase.mapper.PurchaseItemMapper;
import com.devops00.spectra.oa.purchase.mapper.PurchaseMapper;
import com.devops00.spectra.oa.purchase.mapper.PurchaseReceiptItemMapper;
import com.devops00.spectra.oa.purchase.mapper.PurchaseReceiptMapper;
import com.devops00.spectra.oa.purchase.service.PurchaseService;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.workflow.service.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.*;

/**
 * 采购申请业务闭环服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PurchaseServiceImpl extends BaseServiceImpl<PurchaseMapper, Purchase> implements PurchaseService {

    private static final String TYPE_CODE = "purchase";
    private static final String EXECUTION_NOT_STARTED = "NOT_STARTED";
    private static final String EXECUTION_ORDERED = "ORDERED";
    private static final String EXECUTION_PARTIAL_RECEIVED = "PARTIAL_RECEIVED";
    private static final String EXECUTION_RECEIVED = "RECEIVED";
    private static final String RECEIPT_PARTIAL = "PARTIAL";
    private static final String RECEIPT_DIFFERENCE = "DIFFERENCE";
    private static final String RECEIPT_COMPLETE = "COMPLETE";

    private final PurchaseItemMapper itemMapper;
    private final PurchaseReceiptMapper receiptMapper;
    private final PurchaseReceiptItemMapper receiptItemMapper;
    private final TimeMapper timeMapper;
    private final ApplicationMapper applicationMapper;
    private final ApplicationTypeMapper applicationTypeMapper;
    private final ApplicationService applicationService;
    private final ProcessInstanceService processInstanceService;
    private final NotificationGateway notificationGateway;
    private final PurchaseConverter purchaseConverter;

    @Override
    public IPage<PurchaseVO> page(PageFrom page, PurchasePageFrom params) {
        var wrapper = new LambdaQueryWrapper<Purchase>();
        var user = SecUtil.getCurrentUser();
        if (user == null || user.getId() == null || user.getDepartmentId() == null) {
            return new Page<>(page.getPageNum(), page.getPageSize(), 0);
        }
        var applicationWrapper = new LambdaQueryWrapper<Application>().eq(Application::getTypeCode, TYPE_CODE)
                .and(query -> query.eq(Application::getApplicantId, user.getId()).or().eq(Application::getDepartmentId, user.getDepartmentId()));
        if (params != null && StringUtils.hasText(params.getStatus())) {
            applicationWrapper.eq(Application::getStatus, params.getStatus());
        }
        var applicationIds = applicationMapper.selectList(applicationWrapper).stream().map(Application::getId).toList();
        if (applicationIds.isEmpty()) {
            return new Page<>(page.getPageNum(), page.getPageSize(), 0);
        }
        wrapper.in(Purchase::getApplicationId, applicationIds);
        if (params != null && StringUtils.hasText(params.getExecutionStatus())) {
            wrapper.eq(Purchase::getExecutionStatus, params.getExecutionStatus());
        }
        if (params != null && StringUtils.hasText(params.getKeyword())) {
            wrapper.and(query -> query.like(Purchase::getPurpose, params.getKeyword())
                    .or()
                    .like(Purchase::getSuggestedSupplier, params.getKeyword())
                    .or()
                    .like(Purchase::getOrderNo, params.getKeyword()));
        }
        wrapper.orderByDesc(Purchase::getCreatedAt);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<PurchaseVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::assembleView).toList());
        return voPage;
    }

    @Override
    public PurchaseVO get(UUID id) {
        var entity = require(id);
        applicationService.requireVisible(entity.getApplicationId());
        return assembleView(entity);
    }

    @Override
    @Transactional
    public UUID created(PurchaseSaveFrom from) {
        validate(from);
        var application = applicationService.createDraft(TYPE_CODE, null, "采购申请 - " + from.getPurpose());
        var entity = purchaseConverter.toEntity(from);
        entity.setApplicationId(application.getId());
        entity.setDepartmentId(application.getDepartmentId());
        entity.setExecutionStatus(EXECUTION_NOT_STARTED);
        entity.setBudgetAmount(from.getBudgetAmount().setScale(2, RoundingMode.HALF_UP));
        entity.setCurrency(StringUtils.hasText(from.getCurrency()) ? from.getCurrency().toUpperCase() : "CNY");
        if (!this.save(entity)) {
            throw new DataSaveException("保存采购申请失败");
        }
        replaceItems(entity, from.getItems());
        applicationService.bindBizId(application.getId(), entity.getId());
        log.info("创建采购申请草稿成功: id={}, applicationId={}", entity.getId(), application.getId());
        return entity.getId();
    }

    @Override
    @Transactional
    public void modify(UUID id, PurchaseSaveFrom from) {
        validate(from);
        var entity = require(id);
        var application = requireEditableApplication(entity);
        purchaseConverter.updateEntity(from, entity);
        entity.setBudgetAmount(from.getBudgetAmount().setScale(2, RoundingMode.HALF_UP));
        entity.setCurrency(StringUtils.hasText(from.getCurrency()) ? from.getCurrency().toUpperCase() : "CNY");
        if (!this.updateById(entity)) {
            throw new DataSaveException("更新采购申请失败");
        }
        replaceItems(entity, from.getItems());
        log.info("修改采购申请草稿成功: id={}, applicationId={}", id, application.getId());
    }

    @Override
    @Transactional
    public void submit(UUID id, PurchaseSubmitFrom from) {
        var entity = require(id);
        var application = requireEditableApplication(entity);
        var type = applicationTypeMapper
                .selectOne(new LambdaQueryWrapper<ApplicationType>().eq(ApplicationType::getCode, TYPE_CODE).eq(ApplicationType::getEnabled, true));
        if (type == null || !StringUtils.hasText(type.getProcessDefinitionKey())) {
            throw new DataSaveException("采购流程尚未配置");
        }
        applicationService.submit(application.getId());
        var currentUser = SecUtil.getCurrentUser();
        var username = currentUser == null ? null : currentUser.getUsername();
        if (!StringUtils.hasText(username)) {
            throw new DataSaveException("当前用户缺少流程用户名");
        }
        var variables = new LinkedHashMap<String, Object>();
        variables.put("applicant", username);
        var approver = from == null ? null : from.getApproverUsername();
        variables.put("approver", StringUtils.hasText(approver) ? approver : username);
        variables.put("applicantId", application.getApplicantId().toString());
        variables.put("amount", entity.getBudgetAmount().doubleValue());
        var processId = processInstanceService.start(type.getProcessDefinitionKey(), application.getId().toString(), variables);
        applicationService.bindProcessInstance(application.getId(), processId);
        log.info("提交采购审批成功: id={}, processInstanceId={}", id, processId);
    }

    @Override
    @Transactional
    public void withdraw(UUID id) {
        var entity = require(id);
        var application = requireApplicantApplication(entity);
        applicationService.withdraw(application.getId());
        terminateProcess(application);
    }

    @Override
    @Transactional
    public void cancel(UUID id) {
        var entity = require(id);
        var application = requireApplicantApplication(entity);
        applicationService.cancel(application.getId());
        terminateProcess(application);
        entity.setExecutionStatus("CANCELLED");
        this.updateById(entity);
    }

    @Override
    @Transactional
    public void execute(UUID id, PurchaseExecuteFrom from) {
        var entity = require(id);
        var application = applicationService.require(entity.getApplicationId());
        if (!ApplicationStatus.APPROVED.name().equals(application.getStatus())) {
            throw new DataSaveException("只有审批通过的采购申请可以执行");
        }
        if (EXECUTION_RECEIVED.equals(entity.getExecutionStatus())) {
            throw new DataSaveException("采购申请已完成收货");
        }
        var currentUserId = SecUtil.getCurrentUserId();
        var purchaserId = from == null || from.getPurchaserId() == null ? currentUserId : from.getPurchaserId();
        if (purchaserId == null) {
            throw new DataSaveException("采购执行人不能为空");
        }
        var status = from == null || !StringUtils.hasText(from.getExecutionStatus()) ? EXECUTION_ORDERED : from.getExecutionStatus();
        if (!EXECUTION_ORDERED.equals(status) && !EXECUTION_PARTIAL_RECEIVED.equals(status)) {
            throw new DataSaveException("采购执行状态不合法");
        }
        entity.setPurchaserId(purchaserId);
        if (StringUtils.hasText(from == null ? null : from.getOrderNo())) {
            entity.setOrderNo(from.getOrderNo());
        }
        entity.setExecutionStatus(EXECUTION_PARTIAL_RECEIVED.equals(entity.getExecutionStatus()) ? EXECUTION_PARTIAL_RECEIVED : status);
        entity.setOrderedAt(entity.getOrderedAt() == null ? Instant.now() : entity.getOrderedAt());
        entity.setExecutionRemark(from == null ? null : from.getExecutionRemark());
        if (!this.updateById(entity)) {
            throw new DataSaveException("登记采购执行失败");
        }
        sendNotification(application, "采购申请已进入执行", "采购申请已审批通过并进入采购执行环节");
    }

    @Override
    @Transactional
    public void receive(UUID id, PurchaseReceiptFrom from) {
        var entity = require(id);
        var application = applicationService.require(entity.getApplicationId());
        if (!ApplicationStatus.APPROVED.name().equals(application.getStatus())) {
            throw new DataSaveException("只有审批通过的采购申请可以收货");
        }
        if (EXECUTION_RECEIVED.equals(entity.getExecutionStatus()) || "CANCELLED".equals(entity.getExecutionStatus())) {
            throw new DataSaveException("当前采购状态不允许收货");
        }
        var receiverId = from.getReceiverId() == null ? SecUtil.getCurrentUserId() : from.getReceiverId();
        if (receiverId == null) {
            throw new DataSaveException("收货人不能为空");
        }
        var purchaseItems = itemMapper.selectList(new LambdaQueryWrapper<PurchaseItem>().eq(PurchaseItem::getPurchaseId, id));
        var itemMap = purchaseItems.stream().collect(java.util.stream.Collectors.toMap(PurchaseItem::getId, item -> item));
        var receiptItems = new LinkedHashMap<UUID, PurchaseReceiptItemFrom>();
        from.getItems().forEach(item -> {
            if (receiptItems.put(item.getPurchaseItemId(), item) != null) {
                throw new DataSaveException("同一采购项不能重复登记收货");
            }
        });
        var allReceived = true;
        var hasDifference = false;
        for (var entry : receiptItems.entrySet()) {
            var purchaseItem = itemMap.get(entry.getKey());
            if (purchaseItem == null) {
                throw new DataNotExistException("采购明细不存在: " + entry.getKey());
            }
            var quantity = entry.getValue().getQuantity().setScale(3, RoundingMode.HALF_UP);
            var received = purchaseItem.getReceivedQuantity() == null ? BigDecimal.ZERO : purchaseItem.getReceivedQuantity();
            if (received.add(quantity).compareTo(purchaseItem.getQuantity()) > 0) {
                throw new DataSaveException("收货数量不能超过采购数量: " + purchaseItem.getItemName());
            }
            purchaseItem.setReceivedQuantity(received.add(quantity).setScale(3, RoundingMode.HALF_UP));
            if (Boolean.FALSE.equals(entry.getValue().getAccepted())) {
                hasDifference = true;
            }
            if (purchaseItem.getReceivedQuantity().compareTo(purchaseItem.getQuantity()) < 0) {
                allReceived = false;
            }
        }
        for (var purchaseItem : purchaseItems) {
            if (!receiptItems.containsKey(purchaseItem.getId())
                    && (purchaseItem.getReceivedQuantity() == null || purchaseItem.getReceivedQuantity().compareTo(purchaseItem.getQuantity()) < 0)) {
                allReceived = false;
            }
        }
        var receipt = new PurchaseReceipt();
        receipt.setPurchaseId(id);
        receipt.setReceiptNo(StringUtils.hasText(from.getReceiptNo()) ? from.getReceiptNo() : "GR" + Instant.now().toEpochMilli());
        receipt.setReceivedDate(timeMapper.toInstant(from.getReceivedDate()));
        receipt.setReceiverId(receiverId);
        receipt.setStatus(hasDifference ? RECEIPT_DIFFERENCE : allReceived ? RECEIPT_COMPLETE : RECEIPT_PARTIAL);
        receipt.setRemark(from.getRemark());
        if (receiptMapper.insert(receipt) != 1) {
            throw new DataSaveException("保存采购收货单失败");
        }
        for (var entry : receiptItems.entrySet()) {
            var item = entry.getValue();
            var receiptItem = new PurchaseReceiptItem();
            receiptItem.setReceiptId(receipt.getId());
            receiptItem.setPurchaseItemId(item.getPurchaseItemId());
            receiptItem.setQuantity(item.getQuantity().setScale(3, RoundingMode.HALF_UP));
            receiptItem.setAccepted(item.getAccepted() == null || item.getAccepted());
            receiptItem.setDifferenceReason(item.getDifferenceReason());
            if (receiptItemMapper.insert(receiptItem) != 1) {
                throw new DataSaveException("保存采购收货明细失败");
            }
            if (!this.updatePurchaseItem(itemMap.get(entry.getKey()))) {
                throw new DataSaveException("更新采购收货数量失败");
            }
        }
        entity.setExecutionStatus(allReceived ? EXECUTION_RECEIVED : EXECUTION_PARTIAL_RECEIVED);
        if (allReceived) {
            entity.setCompletedAt(Instant.now());
        }
        if (!this.updateById(entity)) {
            throw new DataSaveException("更新采购执行状态失败");
        }
        sendNotification(application, allReceived ? "采购已全部收货" : "采购已部分收货", allReceived ? "采购申请的全部明细已完成收货" : "采购申请已登记一批收货记录");
    }

    @Override
    @Transactional
    public void onApproved(String businessKey, Map<String, Object> variables) {
        var application = requireApplication(businessKey);
        if (!ApplicationStatus.IN_REVIEW.name().equals(application.getStatus())) {
            return;
        }
        var entity = require(application.getBizId());
        applicationService.updateStatus(application.getId(), ApplicationStatus.APPROVED.name(), null);
        if (!EXECUTION_NOT_STARTED.equals(entity.getExecutionStatus())) {
            entity.setExecutionStatus(EXECUTION_NOT_STARTED);
            this.updateById(entity);
        }
        sendNotification(application, "采购申请已通过", "您的采购申请已审批通过，请安排采购执行");
    }

    @Override
    @Transactional
    public void onRejected(String businessKey, String reason) {
        var application = requireApplication(businessKey);
        if (!ApplicationStatus.IN_REVIEW.name().equals(application.getStatus())) {
            return;
        }
        applicationService.updateStatus(application.getId(), ApplicationStatus.REJECTED.name(), reason);
        sendNotification(application, "采购申请已驳回", StringUtils.hasText(reason) ? reason : "采购申请未通过审批");
    }

    @Override
    @Transactional
    public void onTerminated(String businessKey, String reason) {
        var application = requireApplication(businessKey);
        if (ApplicationStatus.IN_REVIEW.name().equals(application.getStatus())) {
            applicationService.updateStatus(application.getId(), ApplicationStatus.CANCELLED.name(), reason);
        }
    }

    private void validate(PurchaseSaveFrom from) {
        var estimate = from.getItems()
                .stream()
                .map(item -> item.getQuantity().multiply(item.getEstimatedUnitPrice()))
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2, RoundingMode.HALF_UP);
        if (estimate.compareTo(from.getBudgetAmount().setScale(2, RoundingMode.HALF_UP)) > 0) {
            throw new DataSaveException("采购明细估价合计不能超过采购预算");
        }
    }

    private void replaceItems(Purchase entity, List<PurchaseItemFrom> items) {
        itemMapper.delete(new LambdaQueryWrapper<PurchaseItem>().eq(PurchaseItem::getPurchaseId, entity.getId()));
        items.forEach(item -> {
            var target = purchaseConverter.toItemEntity(item);
            target.setPurchaseId(entity.getId());
            target.setDepartmentId(entity.getDepartmentId());
            target.setQuantity(item.getQuantity().setScale(3, RoundingMode.HALF_UP));
            target.setEstimatedUnitPrice(item.getEstimatedUnitPrice().setScale(2, RoundingMode.HALF_UP));
            target.setEstimatedAmount(item.getQuantity().multiply(item.getEstimatedUnitPrice()).setScale(2, RoundingMode.HALF_UP));
            target.setReceivedQuantity(BigDecimal.ZERO.setScale(3));
            if (itemMapper.insert(target) != 1) {
                throw new DataSaveException("保存采购明细失败");
            }
        });
    }

    private boolean updatePurchaseItem(PurchaseItem item) {
        return itemMapper.updateById(item) == 1;
    }

    private PurchaseVO assembleView(Purchase entity) {
        var application = applicationService.require(entity.getApplicationId());
        var vo = purchaseConverter.toVO(entity);
        vo.setApplicationNo(application.getApplicationNo());
        vo.setTitle(application.getTitle());
        vo.setStatus(application.getStatus());
        vo.setApplicantId(application.getApplicantId());
        vo.setProcessInstanceId(application.getProcessInstanceId());
        vo.setRejectReason(application.getRejectReason());
        vo.setItems(itemMapper
                .selectList(
                        new LambdaQueryWrapper<PurchaseItem>().eq(PurchaseItem::getPurchaseId, entity.getId()).orderByAsc(PurchaseItem::getCreatedAt))
                .stream()
                .map(purchaseConverter::toItemVO)
                .toList());
        var receipts = receiptMapper.selectList(new LambdaQueryWrapper<PurchaseReceipt>().eq(PurchaseReceipt::getPurchaseId, entity.getId())
                .orderByDesc(PurchaseReceipt::getReceivedDate));
        vo.setReceipts(receipts.stream().map(this::assembleReceiptView).toList());
        return vo;
    }

    private PurchaseReceiptVO assembleReceiptView(PurchaseReceipt receipt) {
        var vo = purchaseConverter.toReceiptVO(receipt);
        vo.setItems(receiptItemMapper.selectList(new LambdaQueryWrapper<PurchaseReceiptItem>().eq(PurchaseReceiptItem::getReceiptId, receipt.getId()))
                .stream()
                .map(purchaseConverter::toReceiptItemVO)
                .toList());
        return vo;
    }

    private Purchase require(UUID id) {
        var entity = this.getById(id);
        if (entity == null) {
            throw new DataNotExistException("采购申请不存在: " + id);
        }
        return entity;
    }

    private Application requireEditableApplication(Purchase entity) {
        var application = requireApplicantApplication(entity);
        if (!ApplicationStatus.DRAFT.name().equals(application.getStatus()) && !ApplicationStatus.REJECTED.name().equals(application.getStatus())) {
            throw new DataSaveException("当前状态不允许修改或提交采购申请");
        }
        return application;
    }

    private Application requireApplicantApplication(Purchase entity) {
        var application = applicationService.require(entity.getApplicationId());
        if (!Objects.equals(application.getApplicantId(), SecUtil.getCurrentUserId())) {
            throw new DataNotExistException("采购申请不存在或无权操作");
        }
        return application;
    }

    private Application requireApplication(String businessKey) {
        try {
            return applicationService.require(UUID.fromString(businessKey));
        } catch (IllegalArgumentException exception) {
            throw new DataNotExistException("审批业务KEY无效: " + businessKey);
        }
    }

    private void terminateProcess(Application application) {
        if (StringUtils.hasText(application.getProcessInstanceId())) {
            processInstanceService.terminate(application.getProcessInstanceId(), "OA 采购申请已撤回或取消");
        }
    }

    private void sendNotification(Application application, String title, String content) {
        notificationGateway.enqueue(NotificationRequest.inApp("oa:purchase:" + application.getBizId() + ":" + title,
                NotificationPurpose.OA_NOTICE, List.of(application.getApplicantId()), "oa.application.status", title, content,
                "OA_PURCHASE", application.getBizId().toString(), "OA", "/oa/purchase/" + application.getBizId()));
    }
}
