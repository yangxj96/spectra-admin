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

package com.devops00.spectra.oa.application.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.oa.application.javabean.constant.ApplicationStatus;
import com.devops00.spectra.oa.application.javabean.converter.ApplicationConverter;
import com.devops00.spectra.oa.application.javabean.entity.Application;
import com.devops00.spectra.oa.application.javabean.entity.ApplicationType;
import com.devops00.spectra.oa.application.javabean.from.ApplicationPageFrom;
import com.devops00.spectra.oa.application.javabean.from.ApplicationTypeSaveFrom;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationTypeVO;
import com.devops00.spectra.oa.application.javabean.vo.ApplicationVO;
import com.devops00.spectra.oa.application.mapper.ApplicationMapper;
import com.devops00.spectra.oa.application.mapper.ApplicationTypeMapper;
import com.devops00.spectra.oa.application.service.ApplicationService;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.workflow.service.TaskService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

/**
 * OA 通用申请生命周期服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/9
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ApplicationServiceImpl extends BaseServiceImpl<ApplicationMapper, Application> implements ApplicationService {

    private static final DateTimeFormatter APPLICATION_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final ApplicationTypeMapper applicationTypeMapper;
    private final ApplicationMapper applicationMapper;
    private final ApplicationConverter applicationConverter;
    private final TaskService taskService;
    private final SecurityContextAccessor securityContextAccessor;

    @Override
    public IPage<ApplicationVO> page(PageFrom page, ApplicationPageFrom params) {
        var wrapper = new LambdaQueryWrapper<Application>();
        var user = securityContextAccessor.currentUser();
        if (user == null || user.getId() == null || user.getDepartmentId() == null) {
            return new Page<>(page.getPageNum(), page.getPageSize(), 0);
        }
        wrapper.and(query -> query.eq(Application::getApplicantId, user.getId()).or().eq(Application::getDepartmentId, user.getDepartmentId()));
        if (params != null && StringUtils.hasText(params.getTypeCode())) {
            wrapper.eq(Application::getTypeCode, params.getTypeCode());
        }
        if (params != null && StringUtils.hasText(params.getStatus())) {
            wrapper.eq(Application::getStatus, params.getStatus());
        }
        if (params != null && StringUtils.hasText(params.getKeyword())) {
            wrapper.and(
                    query -> query.like(Application::getTitle, params.getKeyword()).or().like(Application::getApplicationNo, params.getKeyword()));
        }
        wrapper.orderByDesc(Application::getCreatedAt);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<ApplicationVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(applicationConverter.toVOList(result.getRecords()));
        return voPage;
    }

    @Override
    public ApplicationVO get(UUID id) {
        return applicationConverter.toVO(requireVisible(id));
    }

    @Override
    public Application requireVisible(UUID id) {
        var entity = require(id);
        var user = securityContextAccessor.currentUser();
        if (user != null
                && user.getId() != null
                && user.getDepartmentId() != null
                && (user.getId().equals(entity.getApplicantId()) || user.getDepartmentId().equals(entity.getDepartmentId()))) {
            return entity;
        }
        String username = securityContextAccessor.currentUsername();
        if (StringUtils.hasText(entity.getProcessInstanceId()) && taskService.canAccessProcess(entity.getProcessInstanceId(), username)) {
            return entity;
        }
        throw new DataNotExistException("OA 申请不存在或无权访问");
    }

    @Override
    public List<ApplicationTypeVO> listTypes() {
        var wrapper = new LambdaQueryWrapper<ApplicationType>().eq(ApplicationType::getEnabled, true).orderByAsc(ApplicationType::getSortOrder);
        return applicationConverter.toTypeVOList(applicationTypeMapper.selectList(wrapper));
    }

    @Override
    public List<ApplicationTypeVO> listAllTypes() {
        return applicationConverter.toTypeVOList(applicationTypeMapper.selectList(
                new LambdaQueryWrapper<ApplicationType>().orderByAsc(ApplicationType::getSortOrder).orderByAsc(ApplicationType::getCode)));
    }

    @Override
    @Transactional
    public UUID createdType(ApplicationTypeSaveFrom from) {
        var code = normalizeTypeCode(from.getCode());
        if (applicationTypeMapper.selectOne(new LambdaQueryWrapper<ApplicationType>().eq(ApplicationType::getCode, code)) != null) {
            throw new DataSaveException("申请类型编码已存在");
        }
        var entity = applicationConverter.toTypeEntity(from);
        entity.setCode(code);
        entity.setName(from.getName().trim());
        entity.setEnabled(from.getEnabled() == null || from.getEnabled());
        entity.setSortOrder(from.getSortOrder() == null ? 0 : from.getSortOrder());
        if (applicationTypeMapper.insert(entity) != 1) {
            throw new DataSaveException("创建申请类型失败");
        }
        return entity.getId();
    }

    @Override
    @Transactional
    public void modifyType(UUID id, ApplicationTypeSaveFrom from) {
        var entity = requireType(id);
        var code = normalizeTypeCode(from.getCode());
        var duplicate = applicationTypeMapper
                .selectOne(new LambdaQueryWrapper<ApplicationType>().eq(ApplicationType::getCode, code).ne(ApplicationType::getId, id));
        if (duplicate != null) {
            throw new DataSaveException("申请类型编码已存在");
        }
        applicationConverter.updateTypeEntity(from, entity);
        entity.setCode(code);
        entity.setName(from.getName().trim());
        entity.setEnabled(from.getEnabled() == null || from.getEnabled());
        entity.setSortOrder(from.getSortOrder() == null ? 0 : from.getSortOrder());
        if (applicationTypeMapper.updateById(entity) != 1) {
            throw new DataSaveException("修改申请类型失败");
        }
    }

    @Override
    @Transactional
    public void deleteType(UUID id) {
        var entity = requireType(id);
        if (applicationMapper.selectCount(new LambdaQueryWrapper<Application>().eq(Application::getTypeCode, entity.getCode())) > 0) {
            throw new DataSaveException("已有业务申请使用该类型，只能停用");
        }
        if (applicationTypeMapper.deleteById(entity) != 1) {
            throw new DataSaveException("删除申请类型失败");
        }
    }

    @Override
    @Transactional
    public Application createDraft(String typeCode, UUID bizId, String title) {
        var type = applicationTypeMapper
                .selectOne(new LambdaQueryWrapper<ApplicationType>().eq(ApplicationType::getCode, typeCode).eq(ApplicationType::getEnabled, true));
        if (type == null) {
            throw new DataNotExistException("申请类型不存在或已停用: " + typeCode);
        }
        var currentUser = securityContextAccessor.currentUser();
        var userId = securityContextAccessor.currentUserId();
        if (currentUser == null || userId == null || currentUser.getDepartmentId() == null) {
            throw new DataSaveException("当前用户没有可用的组织归属");
        }
        var entity = new Application();
        entity.setApplicationNo("OA" + LocalDate.now(ZoneOffset.UTC).format(APPLICATION_NO_DATE)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase());
        entity.setTypeCode(typeCode);
        entity.setBizId(bizId);
        entity.setApplicantId(userId);
        entity.setDepartmentId(currentUser.getDepartmentId());
        entity.setTitle(title);
        entity.setStatus(ApplicationStatus.DRAFT.name());
        if (!this.save(entity)) {
            throw new DataSaveException("保存 OA 申请失败");
        }
        return entity;
    }

    @Override
    @Transactional
    public void bindBizId(UUID id, UUID bizId) {
        var entity = require(id);
        entity.setBizId(bizId);
        if (!this.updateById(entity)) {
            throw new DataSaveException("绑定 OA 业务明细失败");
        }
    }

    @Override
    @Transactional
    public void bindProcessInstance(UUID id, String processInstanceId) {
        var entity = require(id);
        entity.setProcessInstanceId(processInstanceId);
        if (!this.updateById(entity)) {
            throw new DataSaveException("保存 OA 流程实例失败");
        }
    }

    @Override
    public Application require(UUID id) {
        var entity = this.getById(id);
        if (entity == null) {
            throw new DataNotExistException("OA 申请不存在: " + id);
        }
        return entity;
    }

    @Override
    @Transactional
    public void submit(UUID id) {
        updateStatus(id, ApplicationStatus.IN_REVIEW.name(), null);
    }

    @Override
    @Transactional
    public void withdraw(UUID id) {
        var entity = require(id);
        if (!ApplicationStatus.IN_REVIEW.name().equals(entity.getStatus())) {
            throw new DataSaveException("只有审批中的申请可以撤回");
        }
        entity.setStatus(ApplicationStatus.WITHDRAWN.name());
        if (!this.updateById(entity)) {
            throw new DataSaveException("撤回 OA 申请失败");
        }
    }

    @Override
    @Transactional
    public void cancel(UUID id) {
        var entity = require(id);
        if (ApplicationStatus.APPROVED.name().equals(entity.getStatus()) || ApplicationStatus.CANCELLED.name().equals(entity.getStatus())) {
            throw new DataSaveException("当前状态不允许取消");
        }
        updateStatus(id, ApplicationStatus.CANCELLED.name(), null);
    }

    @Override
    @Transactional
    public void updateStatus(UUID id, String status, String reason) {
        var entity = require(id);
        var current = entity.getStatus();
        if (ApplicationStatus.IN_REVIEW.name().equals(status)
                && !(ApplicationStatus.DRAFT.name().equals(current)
                        || ApplicationStatus.REJECTED.name().equals(current)
                        || ApplicationStatus.WITHDRAWN.name().equals(current))) {
            throw new DataSaveException("当前状态不允许提交");
        }
        entity.setStatus(status);
        if (ApplicationStatus.IN_REVIEW.name().equals(status)) {
            entity.setSubmittedAt(Instant.now());
            entity.setRejectReason(null);
        } else if (ApplicationStatus.REJECTED.name().equals(status)) {
            entity.setRejectReason(reason);
            entity.setCompletedAt(Instant.now());
        } else if (ApplicationStatus.APPROVED.name().equals(status) || ApplicationStatus.CANCELLED.name().equals(status)) {
            entity.setCompletedAt(Instant.now());
        }
        if (!this.updateById(entity)) {
            throw new DataSaveException("更新 OA 申请状态失败");
        }
    }

    @Override
    public long countMine(String status) {
        var userId = securityContextAccessor.currentUserId();
        if (userId == null) {
            return 0;
        }
        var wrapper = new LambdaQueryWrapper<Application>().eq(Application::getApplicantId, userId);
        if (StringUtils.hasText(status)) {
            wrapper.eq(Application::getStatus, status);
        }
        return this.count(wrapper);
    }

    private ApplicationType requireType(UUID id) {
        var entity = applicationTypeMapper.selectById(id);
        if (entity == null) {
            throw new DataNotExistException("申请类型不存在");
        }
        return entity;
    }

    private String normalizeTypeCode(String value) {
        if (!StringUtils.hasText(value)) {
            throw new DataSaveException("申请类型编码不能为空");
        }
        var code = value.trim().toLowerCase();
        if (!code.matches("[a-z][a-z0-9_]{1,63}")) {
            throw new DataSaveException("申请类型编码只能包含小写字母、数字和下划线");
        }
        return code;
    }
}
