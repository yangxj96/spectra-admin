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

package com.devops00.spectra.oa.reimbursement.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.application.javabean.constant.ApplicationStatus;
import com.devops00.spectra.oa.application.javabean.entity.Application;
import com.devops00.spectra.oa.application.javabean.entity.ApplicationAttachment;
import com.devops00.spectra.oa.application.javabean.entity.ApplicationType;
import com.devops00.spectra.oa.application.mapper.ApplicationAttachmentMapper;
import com.devops00.spectra.oa.application.mapper.ApplicationMapper;
import com.devops00.spectra.oa.application.mapper.ApplicationTypeMapper;
import com.devops00.spectra.oa.application.service.ApplicationService;
import com.devops00.spectra.oa.application.support.OaApplicationWorkflowSupport;
import com.devops00.spectra.oa.reimbursement.javabean.converter.ReimbursementConverter;
import com.devops00.spectra.oa.reimbursement.javabean.constant.ReimbursementPaymentStatus;
import com.devops00.spectra.oa.reimbursement.javabean.entity.Reimbursement;
import com.devops00.spectra.oa.reimbursement.javabean.entity.ReimbursementItem;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementAttachmentFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementItemFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementPageFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementPaymentFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementSaveFrom;
import com.devops00.spectra.oa.reimbursement.javabean.from.ReimbursementSubmitFrom;
import com.devops00.spectra.oa.reimbursement.javabean.vo.ReimbursementVO;
import com.devops00.spectra.oa.reimbursement.mapper.ReimbursementItemMapper;
import com.devops00.spectra.oa.reimbursement.mapper.ReimbursementMapper;
import com.devops00.spectra.oa.reimbursement.service.ReimbursementService;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.file.FileReferenceService;
import com.devops00.spectra.oa.support.OaFileReferenceBinder;
import com.devops00.spectra.oa.support.OaFileReferenceType;
import com.devops00.spectra.common.port.file.FileAssetPort;
import com.devops00.spectra.workflow.api.ProcessInstanceService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 费用报销业务闭环服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/7
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReimbursementServiceImpl extends BaseServiceImpl<ReimbursementMapper, Reimbursement> implements ReimbursementService {

    private static final String TYPE_CODE = "reimbursement";

    private final ReimbursementItemMapper itemMapper;
    private final ApplicationAttachmentMapper attachmentMapper;
    private final ApplicationMapper applicationMapper;
    private final ApplicationTypeMapper applicationTypeMapper;
    private final ApplicationService applicationService;
    private final ProcessInstanceService processInstanceService;
    private final OaApplicationWorkflowSupport workflowSupport;
    private final ReimbursementConverter reimbursementConverter;
    private final TimeMapper timeMapper;
    private final SecurityContextAccessor securityContextAccessor;
    private final FileAssetPort fileAssetPort;
    private final FileReferenceService fileReferenceService;
    private final OaFileReferenceBinder fileReferenceBinder;

    @Override
    public IPage<ReimbursementVO> page(PageFrom page, ReimbursementPageFrom params) {
        var wrapper = new LambdaQueryWrapper<Reimbursement>();
        var user = securityContextAccessor.currentUser();
        if (user == null || user.getId() == null || user.getDepartmentId() == null) {
            return new Page<>(page.getPageNum(), page.getPageSize(), 0);
        }
        var visibleApplications = new LambdaQueryWrapper<Application>().eq(Application::getTypeCode, TYPE_CODE)
                .and(query -> query.eq(Application::getApplicantId, user.getId()).or().eq(Application::getDepartmentId, user.getDepartmentId()));
        var applicationIds = applicationMapper.selectList(visibleApplications).stream().map(Application::getId).toList();
        if (applicationIds.isEmpty()) {
            return new Page<>(page.getPageNum(), page.getPageSize(), 0);
        }
        wrapper.in(Reimbursement::getApplicationId, applicationIds);
        if (params != null && StringUtils.hasText(params.getPaymentStatus())) {
            wrapper.eq(Reimbursement::getPaymentStatus, params.getPaymentStatus());
        }
        if (params != null && StringUtils.hasText(params.getKeyword())) {
            wrapper.and(
                    query -> query.like(Reimbursement::getPurpose, params.getKeyword()).or().like(Reimbursement::getPayeeName, params.getKeyword()));
        }
        if (params != null && StringUtils.hasText(params.getStatus())) {
            var statusApplicationIds = applicationMapper.selectList(new LambdaQueryWrapper<Application>().eq(Application::getTypeCode, TYPE_CODE)
                    .in(Application::getId, applicationIds)
                    .eq(Application::getStatus, params.getStatus())).stream().map(Application::getId).toList();
            if (statusApplicationIds.isEmpty()) {
                return new Page<>(page.getPageNum(), page.getPageSize(), 0);
            }
            wrapper.in(Reimbursement::getApplicationId, statusApplicationIds);
        }
        wrapper.orderByDesc(Reimbursement::getCreatedAt);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<ReimbursementVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(this::assembleView).toList());
        return voPage;
    }

    @Override
    public ReimbursementVO get(UUID id) {
        var entity = require(id);
        applicationService.requireVisible(entity.getApplicationId());
        return assembleView(entity);
    }

    @Override
    @Transactional
    public UUID created(ReimbursementSaveFrom from) {
        validate(from);
        var user = securityContextAccessor.currentUser();
        if (user == null || securityContextAccessor.currentUserId() == null || user.getDepartmentId() == null) {
            throw new DataSaveException("当前用户组织信息不可用");
        }
        var application = applicationService.createDraft(TYPE_CODE, null, "费用报销 - " + from.getPurpose());
        var entity = reimbursementConverter.toEntity(from);
        entity.setApplicationId(application.getId());
        entity.setDepartmentId(application.getDepartmentId());
        entity.setTotalAmount(from.getTotalAmount().setScale(2, RoundingMode.HALF_UP));
        entity.setCurrency(StringUtils.hasText(from.getCurrency()) ? from.getCurrency().toUpperCase() : "CNY");
        entity.setPaymentStatus(ReimbursementPaymentStatus.PENDING.getValue());
        if (!this.save(entity)) {
            throw new DataSaveException("保存报销单失败");
        }
        replaceItems(entity, from.getItems());
        replaceAttachments(application.getId(), from.getAttachments());
        applicationService.bindBizId(application.getId(), entity.getId());
        log.info("创建报销草稿成功: id={}, applicationId={}", entity.getId(), application.getId());
        return entity.getId();
    }

    @Override
    @Transactional
    public void modify(UUID id, ReimbursementSaveFrom from) {
        validate(from);
        var entity = require(id);
        var application = requireEditableApplication(entity);
        reimbursementConverter.updateEntity(from, entity);
        entity.setTotalAmount(from.getTotalAmount().setScale(2, RoundingMode.HALF_UP));
        entity.setCurrency(StringUtils.hasText(from.getCurrency()) ? from.getCurrency().toUpperCase() : "CNY");
        if (!this.updateById(entity)) {
            throw new DataSaveException("更新报销单失败");
        }
        replaceItems(entity, from.getItems());
        replaceAttachments(application.getId(), from.getAttachments());
    }

    @Override
    @Transactional
    public void submit(UUID id, ReimbursementSubmitFrom from) {
        var entity = require(id);
        var application = requireEditableApplication(entity);
        var type = applicationTypeMapper
                .selectOne(new LambdaQueryWrapper<ApplicationType>().eq(ApplicationType::getCode, TYPE_CODE).eq(ApplicationType::getEnabled, true));
        if (type == null || !StringUtils.hasText(type.getProcessDefinitionKey())) {
            throw new DataSaveException("报销流程尚未配置");
        }
        applicationService.submit(application.getId());
        var currentUser = securityContextAccessor.currentUser();
        var applicantUsername = currentUser == null ? null : currentUser.getUsername();
        if (!StringUtils.hasText(applicantUsername)) {
            throw new DataSaveException("当前用户缺少流程用户名");
        }
        var approver = from == null ? null : from.getApproverEmail();
        var variables = new LinkedHashMap<String, Object>();
        variables.put("applicant", applicantUsername);
        variables.put("approver", StringUtils.hasText(approver) ? approver : applicantUsername);
        variables.put("applicantId", application.getApplicantId().toString());
        variables.put("amount", entity.getTotalAmount().doubleValue());
        var processId = processInstanceService.start(type.getProcessDefinitionKey(), application.getId().toString(), variables);
        applicationService.bindProcessInstance(application.getId(), processId);
        log.info("提交报销审批成功: id={}, processInstanceId={}", id, processId);
    }

    @Override
    @Transactional
    public void withdraw(UUID id) {
        var entity = require(id);
        var application = workflowSupport.requireApplicantApplication(entity.getApplicationId(), "报销单不存在或无权操作");
        applicationService.withdraw(application.getId());
        workflowSupport.terminateProcess(application, "OA 报销申请已撤回或取消");
    }

    @Override
    @Transactional
    public void cancel(UUID id) {
        var entity = require(id);
        var application = workflowSupport.requireApplicantApplication(entity.getApplicationId(), "报销单不存在或无权操作");
        applicationService.cancel(application.getId());
        workflowSupport.terminateProcess(application, "OA 报销申请已撤回或取消");
    }

    @Override
    @Transactional
    public void markPaid(UUID id, ReimbursementPaymentFrom from) {
        var entity = require(id);
        var application = applicationService.require(entity.getApplicationId());
        if (!ApplicationStatus.APPROVED.name().equals(application.getStatus())) {
            throw new DataSaveException("只有审批通过的报销单可以登记付款");
        }
        if (!ReimbursementPaymentStatus.PENDING.getValue().equals(entity.getPaymentStatus())) {
            throw new DataSaveException("当前付款状态不允许登记");
        }
        entity.setPaymentStatus(ReimbursementPaymentStatus.PAID.getValue());
        entity.setPaymentAt(Instant.now());
        entity.setPaymentRemark(from == null ? null : from.getPaymentRemark());
        if (!this.updateById(entity)) {
            throw new DataSaveException("登记报销付款失败");
        }
        workflowSupport.sendNotification(application, "reimbursement", "报销已付款", "您的报销申请已完成付款");
    }

    @Override
    @Transactional
    public void onApproved(String businessKey, Map<String, Object> variables) {
        var application = workflowSupport.requireApplication(businessKey);
        if (!ApplicationStatus.IN_REVIEW.name().equals(application.getStatus())) {
            return;
        }
        var entity = require(application.getBizId());
        applicationService.updateStatus(application.getId(), ApplicationStatus.APPROVED.name(), null);
        entity.setPaymentStatus(ReimbursementPaymentStatus.PENDING.getValue());
        this.updateById(entity);
        workflowSupport.sendNotification(application, "reimbursement", "报销申请已通过", "您的报销申请已审批通过，等待付款");
    }

    @Override
    @Transactional
    public void onRejected(String businessKey, String reason) {
        var application = workflowSupport.requireApplication(businessKey);
        if (!ApplicationStatus.IN_REVIEW.name().equals(application.getStatus())) {
            return;
        }
        applicationService.updateStatus(application.getId(), ApplicationStatus.REJECTED.name(), reason);
        workflowSupport.sendNotification(application, "reimbursement", "报销申请已驳回", StringUtils.hasText(reason) ? reason : "报销申请未通过审批");
    }

    @Override
    @Transactional
    public void onTerminated(String businessKey, String reason) {
        var application = workflowSupport.requireApplication(businessKey);
        if (ApplicationStatus.IN_REVIEW.name().equals(application.getStatus())) {
            applicationService.updateStatus(application.getId(), ApplicationStatus.CANCELLED.name(), reason);
        }
    }

    /**
     * 校验并确保数据满足当前约束（{@code validate}）。
     */
    private void validate(ReimbursementSaveFrom from) {
        if (from == null || from.getItems() == null || from.getItems().isEmpty()) {
            throw new DataSaveException("报销参数和费用明细不能为空");
        }
        var expenseStart = Objects.requireNonNull(timeMapper.toInstant(from.getExpenseStart()), "费用开始日期不能为空");
        var expenseEnd = Objects.requireNonNull(timeMapper.toInstant(from.getExpenseEnd()), "费用结束日期不能为空");
        if (expenseEnd.isBefore(expenseStart)) {
            throw new DataSaveException("费用结束日期不能早于开始日期");
        }
        var total = from.getItems()
                .stream()
                .map(ReimbursementItemFrom::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .setScale(2,
                        RoundingMode.HALF_UP);
        if (total.compareTo(from.getTotalAmount().setScale(2, RoundingMode.HALF_UP)) != 0) {
            throw new DataSaveException("报销总额必须等于费用明细合计");
        }
        from.getItems().forEach(item -> {
            var expenseDate = Objects.requireNonNull(timeMapper.toInstant(item.getExpenseDate()), "费用日期不能为空");
            if (expenseDate.isBefore(expenseStart) || expenseDate.isAfter(expenseEnd)) {
                throw new DataSaveException("费用日期必须在报销期间内");
            }
        });
    }

    /**
     * 更新或推进目标状态（{@code replaceItems}）。
     */
    private void replaceItems(Reimbursement entity, List<ReimbursementItemFrom> items) {
        itemMapper.delete(new LambdaQueryWrapper<ReimbursementItem>().eq(ReimbursementItem::getReimbursementId, entity.getId()));
        var invoiceNos = items.stream().map(ReimbursementItemFrom::getInvoiceNo).filter(StringUtils::hasText).map(String::trim).toList();
        if (invoiceNos.size() != new HashSet<>(invoiceNos).size()) {
            throw new DataSaveException("同一报销单不能重复使用发票号码");
        }
        if (!invoiceNos.isEmpty()
                && itemMapper.selectCount(new LambdaQueryWrapper<ReimbursementItem>().in(ReimbursementItem::getInvoiceNo, invoiceNos)) > 0) {
            throw new DataSaveException("发票号码已被其他报销单使用");
        }
        var entities = items.stream().map(item -> {
            var target = reimbursementConverter.toItemEntity(item);
            target.setReimbursementId(entity.getId());
            target.setDepartmentId(entity.getDepartmentId());
            target.setAmount(item.getAmount().setScale(2, RoundingMode.HALF_UP));
            target.setTaxAmount(item.getTaxAmount().setScale(2, RoundingMode.HALF_UP));
            target.setInvoiceNo(StringUtils.hasText(item.getInvoiceNo()) ? item.getInvoiceNo().trim() : null);
            return target;
        }).toList();
        entities.forEach(item -> {
            if (itemMapper.insert(item) != 1) {
                throw new DataSaveException("保存报销明细失败");
            }
        });
    }

    /**
     * 更新或推进目标状态（{@code replaceAttachments}）。
     */
    private void replaceAttachments(UUID applicationId, List<ReimbursementAttachmentFrom> attachments) {
        attachmentMapper.selectList(new LambdaQueryWrapper<ApplicationAttachment>().eq(ApplicationAttachment::getApplicationId, applicationId))
                .forEach(old -> fileReferenceService.removeByReference("OA_REIMBURSEMENT_ATTACHMENT", old.getId()));
        attachmentMapper.delete(new LambdaQueryWrapper<ApplicationAttachment>().eq(ApplicationAttachment::getApplicationId, applicationId));
        if (attachments == null || attachments.isEmpty()) {
            return;
        }
        var unique = new LinkedHashMap<UUID, ReimbursementAttachmentFrom>();
        attachments.forEach(item -> unique.put(item.getFileAssetId(), item));
        unique.values().forEach(item -> {
            var file = fileAssetPort.requireReadyForReference(item.getFileAssetId(), securityContextAccessor.currentUserId());
            var attachment = reimbursementConverter.toAttachmentEntity(item);
            attachment.setApplicationId(applicationId);
            if (attachmentMapper.insert(attachment) != 1) {
                throw new DataSaveException("保存报销凭证失败");
            }
            fileReferenceService.register(fileReferenceBinder.content(file.fileAssetId(), OaFileReferenceType.REIMBURSEMENT_ATTACHMENT,
                    attachment.getId(), attachment.getFileName()));
        });
    }

    /**
     * 处理内部业务逻辑（{@code assembleView}）。
     */
    private ReimbursementVO assembleView(Reimbursement entity) {
        var application = applicationService.require(entity.getApplicationId());
        var vo = reimbursementConverter.toVO(entity);
        vo.setApplicationNo(application.getApplicationNo());
        vo.setTitle(application.getTitle());
        vo.setStatus(application.getStatus());
        vo.setApplicantId(application.getApplicantId());
        vo.setProcessInstanceId(application.getProcessInstanceId());
        vo.setRejectReason(application.getRejectReason());
        vo.setPayeeAccountMasked(mask(entity.getPayeeAccount()));
        vo.setItems(itemMapper.selectList(new LambdaQueryWrapper<ReimbursementItem>().eq(ReimbursementItem::getReimbursementId, entity.getId())
                .orderByAsc(ReimbursementItem::getExpenseDate)).stream().map(reimbursementConverter::toItemVO).toList());
        vo.setAttachments(attachmentMapper
                .selectList(new LambdaQueryWrapper<ApplicationAttachment>().eq(ApplicationAttachment::getApplicationId, application.getId()))
                .stream()
                .map(reimbursementConverter::toAttachmentVO)
                .toList());
        return vo;
    }

    /**
     * 校验并确保数据满足当前约束（{@code require}）。
     */
    private Reimbursement require(UUID id) {
        var entity = this.getById(id);
        if (entity == null) {
            throw new DataNotExistException("报销单不存在: " + id);
        }
        return entity;
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireEditableApplication}）。
     */
    private Application requireEditableApplication(Reimbursement entity) {
        var application = workflowSupport.requireApplicantApplication(entity.getApplicationId(), "报销单不存在或无权操作");
        if (!ApplicationStatus.DRAFT.name().equals(application.getStatus()) && !ApplicationStatus.REJECTED.name().equals(application.getStatus())) {
            throw new DataSaveException("当前状态不允许修改或提交报销单");
        }
        return application;
    }

    /**
     * 处理内部业务逻辑（{@code mask}）。
     */
    private String mask(String account) {
        if (!StringUtils.hasText(account)) {
            return null;
        }
        if (account.length() <= 4) {
            return "****";
        }
        return "****" + account.substring(account.length() - 4);
    }
}
