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

package com.devops00.spectra.oa.contract.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.common.notification.NotificationTemplateCode;
import com.devops00.spectra.core.scheduler.service.SchedulerTimeZoneResolver;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.oa.contract.javabean.converter.ContractConverter;
import com.devops00.spectra.oa.contract.javabean.constant.ContractMilestoneStatus;
import com.devops00.spectra.oa.contract.javabean.constant.ContractStatus;
import com.devops00.spectra.oa.contract.javabean.entity.Contract;
import com.devops00.spectra.oa.contract.javabean.entity.ContractMilestone;
import com.devops00.spectra.oa.contract.javabean.entity.ContractVersion;
import com.devops00.spectra.oa.contract.javabean.from.ContractMilestoneSaveFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractMilestoneUpdateFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractPageFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractSaveFrom;
import com.devops00.spectra.oa.contract.javabean.from.ContractVersionFrom;
import com.devops00.spectra.oa.contract.javabean.vo.ContractMilestoneVO;
import com.devops00.spectra.oa.contract.javabean.vo.ContractVO;
import com.devops00.spectra.oa.contract.javabean.vo.ContractVersionVO;
import com.devops00.spectra.oa.contract.mapper.ContractMapper;
import com.devops00.spectra.oa.contract.mapper.ContractMilestoneMapper;
import com.devops00.spectra.oa.contract.mapper.ContractVersionMapper;
import com.devops00.spectra.oa.contract.service.ContractService;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.port.file.FileAssetPort;
import com.devops00.spectra.common.port.file.FileReferenceService;
import com.devops00.spectra.oa.support.OaFileReferenceBinder;
import com.devops00.spectra.oa.support.OaFileReferenceType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

/**
 * 合同台账服务实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/8
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ContractServiceImpl extends BaseServiceImpl<ContractMapper, Contract> implements ContractService {

    private static final DateTimeFormatter CONTRACT_NO_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String SIGNING_UNSIGNED = "UNSIGNED";
    private static final String SIGNING_SIGNED = "SIGNED";

    private final ContractVersionMapper versionMapper;
    private final ContractMilestoneMapper milestoneMapper;
    private final FileAssetPort fileAssetPort;
    private final FileReferenceService fileReferenceService;
    private final OaFileReferenceBinder fileReferenceBinder;
    private final NotificationService notificationService;
    private final ContractConverter contractConverter;
    private final TimeMapper timeMapper;
    private final SecurityContextAccessor securityContextAccessor;
    private final SchedulerTimeZoneResolver schedulerTimeZoneResolver;

    @Override
    public IPage<ContractVO> page(PageFrom page, ContractPageFrom params) {
        var wrapper = new LambdaQueryWrapper<Contract>();
        var user = securityContextAccessor.currentUser();
        if (user == null || user.getId() == null) {
            return new Page<>(page.getPageNum(), page.getPageSize(), 0);
        }
        wrapper.and(query -> {
            query.eq(Contract::getOwnerId, user.getId()).or().eq(Contract::getVisibility, "PUBLIC");
            if (user.getDepartmentId() != null) {
                query.or(q -> q.eq(Contract::getVisibility, "DEPARTMENT").eq(Contract::getDepartmentId, user.getDepartmentId()));
            }
        });
        if (params != null && StringUtils.hasText(params.getKeyword())) {
            wrapper.and(query -> query.like(Contract::getContractNo, params.getKeyword())
                    .or()
                    .like(Contract::getTitle, params.getKeyword())
                    .or()
                    .like(Contract::getCounterpartyName, params.getKeyword()));
        }
        if (params != null && StringUtils.hasText(params.getStatus())) {
            wrapper.eq(Contract::getStatus, params.getStatus());
        }
        if (params != null && StringUtils.hasText(params.getContractType())) {
            wrapper.eq(Contract::getContractType, params.getContractType());
        }
        if (params != null && StringUtils.hasText(params.getSigningStatus())) {
            wrapper.eq(Contract::getSigningStatus, params.getSigningStatus());
        }
        wrapper.orderByDesc(Contract::getUpdatedAt);
        var result = this.page(page.toPage(), wrapper);
        var voPage = new Page<ContractVO>(result.getCurrent(), result.getSize(), result.getTotal());
        voPage.setRecords(result.getRecords().stream().map(contractConverter::toVO).toList());
        return voPage;
    }

    @Override
    public ContractVO get(UUID id) {
        var contract = requireAccessible(id);
        var vo = contractConverter.toVO(contract);
        var current = currentVersion(contract.getId());
        vo.setCurrentVersion(current == null ? null : contractConverter.toVersionVO(current));
        vo.setVersions(versions(id));
        vo.setMilestones(milestones(id));
        return vo;
    }

    @Override
    @Transactional
    public UUID created(ContractSaveFrom from) {
        var user = requireCurrentUser();
        var entity = contractConverter.toEntity(from);
        validateDates(entity.getStartDate(), entity.getEndDate());
        entity.setContractNo(generateContractNo());
        entity.setTitle(from.getTitle().trim());
        entity.setContractType(normalize(from.getContractType(), "OTHER"));
        entity.setCounterpartyName(from.getCounterpartyName().trim());
        entity.setCounterpartyContact(trimToNull(from.getCounterpartyContact()));
        entity.setOwnerId(user.getId());
        entity.setDepartmentId(user.getDepartmentId());
        entity.setAmount(normalizeAmount(from.getAmount()));
        entity.setCurrency(normalize(from.getCurrency(), "CNY"));
        entity.setStatus(ContractStatus.DRAFT.getValue());
        entity.setSigningStatus(SIGNING_UNSIGNED);
        entity.setVisibility(normalizeVisibility(from.getVisibility()));
        entity.setSummary(trimToNull(from.getSummary()));
        if (!save(entity)) {
            throw new DataSaveException("保存合同台账失败");
        }
        log.info("创建合同台账成功: id={}, contractNo={}", entity.getId(), entity.getContractNo());
        return entity.getId();
    }

    @Override
    @Transactional
    public void modify(UUID id, ContractSaveFrom from) {
        var entity = requireOwner(id);
        if (!ContractStatus.DRAFT.getValue().equals(entity.getStatus())) {
            throw new DataSaveException("只有草稿合同可以修改");
        }
        contractConverter.updateEntity(from, entity);
        validateDates(entity.getStartDate(), entity.getEndDate());
        entity.setTitle(from.getTitle().trim());
        entity.setContractType(normalize(from.getContractType(), "OTHER"));
        entity.setCounterpartyName(from.getCounterpartyName().trim());
        entity.setCounterpartyContact(trimToNull(from.getCounterpartyContact()));
        entity.setAmount(normalizeAmount(from.getAmount()));
        entity.setCurrency(normalize(from.getCurrency(), "CNY"));
        entity.setVisibility(normalizeVisibility(from.getVisibility()));
        entity.setSummary(trimToNull(from.getSummary()));
        if (!updateById(entity)) {
            throw new DataSaveException("更新合同台账失败");
        }
    }

    @Override
    @Transactional
    public void deleteById(UUID id) {
        var entity = requireOwner(id);
        if (!ContractStatus.DRAFT.getValue().equals(entity.getStatus())) {
            throw new DataSaveException("只有草稿合同可以删除");
        }
        if (!removeById(entity)) {
            throw new DataSaveException("删除合同台账失败");
        }
    }

    @Override
    @Transactional
    public UUID addVersion(UUID id, ContractVersionFrom from) {
        var contract = requireOwner(id);
        if (ContractStatus.TERMINATED.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("已终止合同不能新增版本");
        }
        if (ContractStatus.ARCHIVED.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("已归档合同不能新增版本");
        }
        var file = fileAssetPort.requireReadyForReference(from.getFileAssetId(), securityContextAccessor.currentUserId());
        var latest = versionMapper.selectOne(new LambdaQueryWrapper<ContractVersion>().eq(ContractVersion::getContractId, contract.getId())
                .orderByDesc(ContractVersion::getVersionNo)
                .last("limit 1"));
        var version = new ContractVersion();
        version.setContractId(contract.getId());
        version.setVersionNo(latest == null ? 1 : latest.getVersionNo() + 1);
        version.setFileAssetId(file.fileAssetId());
        version.setFileName(StringUtils.hasText(from.getFileName()) ? from.getFileName() : file.originalName());
        version.setFileSize(Objects.requireNonNullElse(from.getFileSize(), file.size()));
        version.setContentType(StringUtils.hasText(from.getContentType()) ? from.getContentType() : file.contentType());
        version.setVersionNote(trimToNull(from.getVersionNote()));
        version.setCurrentVersion(true);
        versionMapper.update(null, new LambdaUpdateWrapper<ContractVersion>().eq(ContractVersion::getContractId, contract.getId())
                .eq(ContractVersion::getCurrentVersion, true)
                .set(ContractVersion::getCurrentVersion, false));
        if (versionMapper.insert(version) != 1) {
            throw new DataSaveException("保存合同版本失败");
        }
        fileReferenceService.register(fileReferenceBinder.content(file.fileAssetId(), OaFileReferenceType.CONTRACT_VERSION,
                version.getId(), version.getFileName()));
        return version.getId();
    }

    @Override
    public List<ContractVersionVO> versions(UUID id) {
        var contract = requireAccessible(id);
        return versionMapper.selectList(new LambdaQueryWrapper<ContractVersion>().eq(ContractVersion::getContractId, contract.getId())
                .orderByDesc(ContractVersion::getVersionNo)).stream().map(contractConverter::toVersionVO).toList();
    }

    @Override
    @Transactional
    public UUID createMilestone(UUID id, ContractMilestoneSaveFrom from) {
        var contract = requireOwner(id);
        if (ContractStatus.ARCHIVED.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("已归档合同不能新增履约节点");
        }
        if (ContractStatus.TERMINATED.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("已终止合同不能新增履约节点");
        }
        var milestone = new ContractMilestone();
        milestone.setContractId(contract.getId());
        milestone.setName(from.getName().trim());
        milestone.setMilestoneType(normalize(from.getMilestoneType(), "OTHER"));
        milestone.setDueDate(timeMapper.toInstant(from.getDueDate()));
        milestone.setStatus(ContractMilestoneStatus.PENDING.getValue());
        milestone.setAssigneeId(from.getAssigneeId());
        milestone.setRemark(trimToNull(from.getRemark()));
        if (milestoneMapper.insert(milestone) != 1) {
            throw new DataSaveException("保存合同履约节点失败");
        }
        return milestone.getId();
    }

    @Override
    public List<ContractMilestoneVO> milestones(UUID id) {
        var contract = requireAccessible(id);
        return milestoneMapper
                .selectList(new LambdaQueryWrapper<ContractMilestone>().eq(ContractMilestone::getContractId, contract.getId())
                        .orderByAsc(ContractMilestone::getDueDate)
                        .orderByAsc(ContractMilestone::getCreatedAt))
                .stream()
                .map(contractConverter::toMilestoneVO)
                .toList();
    }

    @Override
    @Transactional
    public void updateMilestone(UUID id, UUID milestoneId, ContractMilestoneUpdateFrom from) {
        requireOwner(id);
        var milestone = requireMilestone(id, milestoneId);
        var status = normalize(from.getStatus(), ContractMilestoneStatus.PENDING.getValue());
        if (!List.of(ContractMilestoneStatus.PENDING.getValue(), ContractMilestoneStatus.DONE.getValue(), ContractMilestoneStatus.SKIPPED.getValue())
                .contains(status)) {
            throw new DataSaveException("履约节点状态不合法");
        }
        milestone.setStatus(status);
        milestone.setCompletedAt(
                ContractMilestoneStatus.DONE.getValue().equals(status)
                        ? (from.getCompletedAt() == null ? Instant.now() : timeMapper.toInstant(from.getCompletedAt()))
                        : null);
        milestone.setRemark(trimToNull(from.getRemark()));
        if (milestoneMapper.updateById(milestone) != 1) {
            throw new DataSaveException("更新合同履约节点失败");
        }
    }

    @Override
    @Transactional
    public void sign(UUID id) {
        var contract = requireOwner(id);
        if (ContractStatus.ARCHIVED.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("已归档合同不能标记签署");
        }
        if (!ContractStatus.DRAFT.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("只有草稿合同可以标记签署");
        }
        contract.setSigningStatus(SIGNING_SIGNED);
        contract.setSignedAt(Instant.now());
        if (!updateById(contract)) {
            throw new DataSaveException("更新合同签署状态失败");
        }
    }

    @Override
    @Transactional
    public void activate(UUID id) {
        var contract = requireOwner(id);
        if (ContractStatus.ARCHIVED.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("已归档合同不能生效");
        }
        if (!SIGNING_SIGNED.equals(contract.getSigningStatus())) {
            throw new DataSaveException("合同签署后才能生效");
        }
        if (currentVersion(contract.getId()) == null) {
            throw new DataSaveException("合同生效前必须上传合同文件");
        }
        contract.setStatus(ContractStatus.ACTIVE.getValue());
        if (!updateById(contract)) {
            throw new DataSaveException("合同生效失败");
        }
    }

    @Override
    @Transactional
    public void terminate(UUID id) {
        var contract = requireOwner(id);
        if (ContractStatus.ARCHIVED.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("已归档合同不能终止");
        }
        if (!ContractStatus.ACTIVE.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("只有生效合同可以终止");
        }
        contract.setStatus(ContractStatus.TERMINATED.getValue());
        if (!updateById(contract)) {
            throw new DataSaveException("终止合同失败");
        }
    }

    @Override
    @Transactional
    public void archive(UUID id) {
        var contract = requireOwner(id);
        if (ContractStatus.ARCHIVED.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("合同已经归档");
        }
        if (ContractStatus.DRAFT.getValue().equals(contract.getStatus())) {
            throw new DataSaveException("草稿合同不能归档");
        }
        contract.setStatus(ContractStatus.ARCHIVED.getValue());
        if (!updateById(contract)) {
            throw new DataSaveException("归档合同失败");
        }
    }

    @Override
    @Transactional
    public int sendDueMilestoneReminders() {
        var systemZone = schedulerTimeZoneResolver.resolve();
        var deadline = LocalDate.now(systemZone).plusDays(3).atStartOfDay(systemZone).toInstant();
        var milestones = milestoneMapper.selectList(new LambdaQueryWrapper<ContractMilestone>().le(ContractMilestone::getDueDate, deadline)
                .eq(ContractMilestone::getStatus, ContractMilestoneStatus.PENDING.getValue())
                .isNull(ContractMilestone::getReminderSentAt));
        var sent = 0;
        for (var milestone : milestones) {
            var contract = getById(milestone.getContractId());
            if (contract == null || ContractStatus.ARCHIVED.getValue().equals(contract.getStatus())) {
                continue;
            }
            var receiverId = milestone.getAssigneeId() == null ? contract.getOwnerId() : milestone.getAssigneeId();
            if (receiverId == null) {
                continue;
            }
            var claimed = milestoneMapper.update(null, new LambdaUpdateWrapper<ContractMilestone>().eq(ContractMilestone::getId, milestone.getId())
                    .isNull(ContractMilestone::getReminderSentAt)
                    .set(ContractMilestone::getReminderSentAt, Instant.now()));
            if (claimed != 1) {
                continue;
            }
            try {
                notificationService.send(NotificationSendRequest.inApp("oa:contract-milestone:" + milestone.getId(),
                        NotificationPurpose.OA_REMINDER, List.of(receiverId), NotificationTemplateCode.OA_CONTRACT_MILESTONE_REMINDER)
                        .parameter("contract_title", Objects.toString(contract.getTitle(), ""))
                        .parameter("milestone_name", Objects.toString(milestone.getName(), ""))
                        .parameter("due_date", Objects.toString(milestone.getDueDate(), ""))
                        .businessReference("OA_CONTRACT_MILESTONE", milestone.getId().toString())
                        .sourceModule("OA")
                        .link("/oa/contract/" + contract.getId())
                        .build());
                sent++;
            } catch (RuntimeException exception) {
                milestoneMapper.update(null, new LambdaUpdateWrapper<ContractMilestone>().eq(ContractMilestone::getId, milestone.getId())
                        .set(ContractMilestone::getReminderSentAt, null));
                log.warn("合同履约节点提醒发送失败: milestoneId={}", milestone.getId(), exception);
            }
        }
        return sent;
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireMilestone}）。
     */
    private ContractMilestone requireMilestone(UUID id, UUID milestoneId) {
        var milestone = milestoneMapper.selectOne(
                new LambdaQueryWrapper<ContractMilestone>().eq(ContractMilestone::getId, milestoneId).eq(ContractMilestone::getContractId, id));
        if (milestone == null) {
            throw new DataNotExistException("合同履约节点不存在");
        }
        return milestone;
    }

    /**
     * 查询或获取目标数据（{@code currentVersion}）。
     */
    private ContractVersion currentVersion(UUID id) {
        return versionMapper.selectOne(new LambdaQueryWrapper<ContractVersion>().eq(ContractVersion::getContractId, id)
                .eq(ContractVersion::getCurrentVersion, true)
                .last("limit 1"));
    }

    /**
     * 校验并确保数据满足当前约束（{@code require}）。
     */
    private Contract require(UUID id) {
        var entity = getById(id);
        if (entity == null) {
            throw new DataNotExistException("合同不存在: " + id);
        }
        return entity;
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireAccessible}）。
     */
    private Contract requireAccessible(UUID id) {
        var entity = require(id);
        var user = requireCurrentUser();
        if ("PRIVATE".equals(entity.getVisibility()) && !Objects.equals(entity.getOwnerId(), user.getId())) {
            throw new DataNotExistException("合同不存在或无权访问");
        }
        if ("DEPARTMENT".equals(entity.getVisibility()) && !Objects.equals(entity.getDepartmentId(), user.getDepartmentId())) {
            throw new DataNotExistException("合同不存在或无权访问");
        }
        return entity;
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireOwner}）。
     */
    private Contract requireOwner(UUID id) {
        var entity = require(id);
        var user = requireCurrentUser();
        if (!Objects.equals(entity.getOwnerId(), user.getId())) {
            throw new DataNotExistException("合同不存在或无权操作");
        }
        return entity;
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireCurrentUser}）。
     */
    private SecurityPrincipal requireCurrentUser() {
        var user = securityContextAccessor.currentUser();
        if (user == null || user.getId() == null || user.getDepartmentId() == null) {
            throw new DataSaveException("当前用户组织信息不可用");
        }
        return user;
    }

    /**
     * 创建或构建目标数据（{@code generateContractNo}）。
     */
    private String generateContractNo() {
        return "HT" + LocalDate.now(ZoneOffset.UTC).format(CONTRACT_NO_DATE)
                + UUID.randomUUID().toString().replace("-", "").substring(0, 8).toUpperCase();
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateDates}）。
     */
    private void validateDates(Instant startDate, Instant endDate) {
        if (startDate != null && endDate != null && endDate.isBefore(startDate)) {
            throw new DataSaveException("合同到期日期不能早于生效日期");
        }
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeAmount}）。
     */
    private BigDecimal normalizeAmount(BigDecimal amount) {
        return amount == null ? BigDecimal.ZERO.setScale(2) : amount.setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * 转换、解析或规范化数据（{@code normalizeVisibility}）。
     */
    private String normalizeVisibility(String value) {
        var normalized = normalize(value, "DEPARTMENT");
        if (!List.of("PUBLIC", "DEPARTMENT", "PRIVATE").contains(normalized)) {
            throw new DataSaveException("合同可见范围不合法");
        }
        return normalized;
    }

    /**
     * 转换、解析或规范化数据（{@code normalize}）。
     */
    private String normalize(String value, String defaultValue) {
        return StringUtils.hasText(value) ? value.trim().toUpperCase() : defaultValue;
    }

    /**
     * 处理内部业务逻辑（{@code trimToNull}）。
     */
    private String trimToNull(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }
}
