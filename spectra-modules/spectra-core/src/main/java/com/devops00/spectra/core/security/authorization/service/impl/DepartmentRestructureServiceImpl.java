/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.security.authorization.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import com.baomidou.mybatisplus.core.toolkit.IdWorker;
import com.devops00.spectra.common.audit.AuditRecord;
import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentMergeApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentMergePreviewFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentRestructureDepartmentFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentSplitApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.DepartmentSplitPreviewFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.DepartmentRestructureApplyVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.DepartmentRestructurePreviewVO;
import com.devops00.spectra.core.security.authorization.service.DepartmentAuthorizationReferenceImpact;
import com.devops00.spectra.core.security.authorization.service.DepartmentAuthorizationReferenceService;
import com.devops00.spectra.core.security.authorization.service.DepartmentRestructureService;
import com.devops00.spectra.core.security.change.AuthorizationChangeToken;
import com.devops00.spectra.core.security.change.AuthorizationChangeTokenService;
import com.devops00.spectra.core.security.change.AuthorizationEpochGuard;
import com.devops00.spectra.core.security.change.HighRiskApprovalGate;
import com.devops00.spectra.core.security.change.SecurityChangeExecutor;
import com.devops00.spectra.core.system.javabean.entity.OrganizationVersion;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentClosureMapper;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.system.mapper.OrganizationVersionMapper;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.core.user.service.DepartmentMembershipRestructureImpact;
import com.devops00.spectra.core.user.service.DepartmentMembershipRestructureService;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 统一编排部门合并和直属成员拆分，保证部门、关系、授权、闭包和版本在一个事务内完成。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
@Service
public class DepartmentRestructureServiceImpl implements DepartmentRestructureService {

    private static final long PREVIEW_TTL_SECONDS = 300;
    private static final String HISTORY_SUMMARY = "OA 历史业务仍保留原部门 ID；历史名称可解析逻辑删除部门。";

    private final DepartmentMapper departmentMapper;
    private final DepartmentClosureMapper departmentClosureMapper;
    private final OrganizationVersionMapper organizationVersionMapper;
    private final DepartmentService departmentService;
    private final DepartmentMembershipRestructureService membershipService;
    private final DepartmentAuthorizationReferenceService authorizationReferenceService;
    private final UserMapper userMapper;
    private final UserDepartmentMembershipMapper membershipMapper;
    private final AuthorizationChangeTokenService tokenService;
    private final SecurityContextAccessor securityContextAccessor;
    private final SecurityChangeExecutor securityChangeExecutor;
    private final AuthorizationEpochGuard epochGuard;
    private final SecuritySessionRevocationPort sessionRevocationPort;
    private final ObjectProvider<HighRiskApprovalGate> approvalGateProvider;
    private final AuditService auditService;
    private final AuditRecordFactory auditRecordFactory;
    private final TimeMapper timeMapper;

    @SuppressWarnings("PMD.ExcessiveParameterList") // EX-B02-PMD-010: 部门重构协作者逐项注入。
    public DepartmentRestructureServiceImpl(DepartmentMapper departmentMapper,
                                            DepartmentClosureMapper departmentClosureMapper,
                                            OrganizationVersionMapper organizationVersionMapper,
                                            DepartmentService departmentService,
                                            DepartmentMembershipRestructureService membershipService,
                                            DepartmentAuthorizationReferenceService authorizationReferenceService,
                                            UserMapper userMapper,
                                            UserDepartmentMembershipMapper membershipMapper,
                                            AuthorizationChangeTokenService tokenService,
                                            SecurityContextAccessor securityContextAccessor,
                                            SecurityChangeExecutor securityChangeExecutor,
                                            AuthorizationEpochGuard epochGuard,
                                            SecuritySessionRevocationPort sessionRevocationPort,
                                            ObjectProvider<HighRiskApprovalGate> approvalGateProvider,
                                            AuditService auditService,
                                            AuditRecordFactory auditRecordFactory,
                                            TimeMapper timeMapper) {
        this.departmentMapper = departmentMapper;
        this.departmentClosureMapper = departmentClosureMapper;
        this.organizationVersionMapper = organizationVersionMapper;
        this.departmentService = departmentService;
        this.membershipService = membershipService;
        this.authorizationReferenceService = authorizationReferenceService;
        this.userMapper = userMapper;
        this.membershipMapper = membershipMapper;
        this.tokenService = tokenService;
        this.securityContextAccessor = securityContextAccessor;
        this.securityChangeExecutor = securityChangeExecutor;
        this.epochGuard = epochGuard;
        this.sessionRevocationPort = sessionRevocationPort;
        this.approvalGateProvider = approvalGateProvider;
        this.auditService = auditService;
        this.auditRecordFactory = auditRecordFactory;
        this.timeMapper = timeMapper;
    }

    @Override
    public DepartmentRestructurePreviewVO previewMerge(DepartmentMergePreviewFrom from) {
        var prepared = inspectMerge(from, false);
        assertHighRiskAllowed(Operation.MERGE, prepared.tokenHash());
        var expiresAt = Instant.now().plusSeconds(PREVIEW_TTL_SECONDS);
        var result = toPreview(prepared, issueToken(Operation.MERGE, prepared.operatorId(),
                prepared.organizationVersion(), prepared.tokenHash(), expiresAt));
        appendPreviewAudit(new PreviewAuditInput(Operation.MERGE, prepared.operatorId(), prepared.sourceIds(),
                prepared.organizationVersion(), prepared.memberImpact().affectedUserCount(),
                prepared.authorizationImpact().assignmentCount()));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentRestructureApplyVO applyMerge(DepartmentMergeApplyFrom from) {
        var encodedToken = requireToken(from == null ? null : from.getPreviewToken());
        var operatorId = currentOperatorId();
        var suppliedToken = verifyTokenEnvelope(encodedToken, Operation.MERGE, operatorId,
                from == null ? null : from.getExpectedOrganizationVersion());
        var prepared = inspectMerge(from, true);
        verifyTokenRequest(suppliedToken, prepared.tokenHash());
        assertHighRiskAllowed(Operation.MERGE, prepared.tokenHash());
        auditService.assertAvailable();
        var startEvent = startApplyEvent(Operation.MERGE, operatorId, prepared.sourceIds(),
                prepared.organizationVersion());
        return securityChangeExecutor.execute(startEvent, () -> executeMerge(prepared));
    }

    @Override
    public DepartmentRestructurePreviewVO previewSplit(DepartmentSplitPreviewFrom from) {
        var prepared = inspectSplit(from, false);
        assertHighRiskAllowed(Operation.SPLIT, prepared.tokenHash());
        var expiresAt = Instant.now().plusSeconds(PREVIEW_TTL_SECONDS);
        var result = toPreview(prepared, issueToken(Operation.SPLIT, prepared.operatorId(),
                prepared.organizationVersion(), prepared.tokenHash(), expiresAt));
        appendPreviewAudit(new PreviewAuditInput(Operation.SPLIT, prepared.operatorId(), List.of(prepared.source().getId()),
                prepared.organizationVersion(), prepared.memberImpact().affectedUserCount(), 0));
        return result;
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public DepartmentRestructureApplyVO applySplit(DepartmentSplitApplyFrom from) {
        var encodedToken = requireToken(from == null ? null : from.getPreviewToken());
        var operatorId = currentOperatorId();
        var suppliedToken = verifyTokenEnvelope(encodedToken, Operation.SPLIT, operatorId,
                from == null ? null : from.getExpectedOrganizationVersion());
        var prepared = inspectSplit(from, true);
        verifyTokenRequest(suppliedToken, prepared.tokenHash());
        assertHighRiskAllowed(Operation.SPLIT, prepared.tokenHash());
        auditService.assertAvailable();
        var startEvent = startApplyEvent(Operation.SPLIT, operatorId, List.of(prepared.source().getId()),
                prepared.organizationVersion());
        return securityChangeExecutor.execute(startEvent, () -> executeSplit(prepared));
    }

    private MergePrepared inspectMerge(DepartmentMergePreviewFrom from, boolean lock) {
        if (from == null || from.getExpectedOrganizationVersion() == null) {
            throw new DataException("部门合并参数不完整");
        }
        var sourceIds = normalizeIds(from.getSourceDepartmentIds(), 2, "至少选择两个不同的源部门");
        var operatorId = currentOperatorId();
        departmentFrom(from.getDepartment(), null);

        var sources = lock
                ? departmentMapper.selectActiveByIdsForUpdate(sourceIds)
                : departmentMapper.selectActiveByIds(sourceIds);
        sources = activeDepartments(sources);
        if (sources.size() != sourceIds.size()) {
            throw new DataException("源部门不存在、已删除或状态已变化，请重新预览");
        }
        sources = sources.stream().sorted(Comparator.comparing(Department::getId)).toList();
        var parentId = sources.getFirst().getPid();
        if (sources.stream().anyMatch(source -> !Objects.equals(parentId, source.getPid()))) {
            throw new DataException("只能合并同一父部门下的同级部门");
        }
        validateActiveParent(parentId);
        var requestDepartment = departmentFrom(from.getDepartment(), parentId);
        var businessHash = requestHash(Operation.MERGE, sourceIds, List.of(), requestDepartment,
                from.getExpectedOrganizationVersion());
        var children = lock
                ? departmentMapper.selectActiveChildrenByParentIdsForUpdate(sourceIds)
                : departmentMapper.selectActiveChildrenByParentIds(sourceIds);
        children = activeDepartments(children);
        var movedDepartmentCount = countMovedDepartments(children);
        var sourceCodes = sources.stream().map(Department::getCode).toList();
        if (sourceCodes.stream().anyMatch(code -> code == null || code.isBlank())
                || new HashSet<>(sourceCodes).size() != sourceCodes.size()) {
            throw new DataException("源部门编码无效，不能安全更新授权引用");
        }

        var organizationVersion = lock
                ? readLockedOrganizationVersion(from.getExpectedOrganizationVersion())
                : readOrganizationVersion(from.getExpectedOrganizationVersion());

        var memberImpact = lockMergeMemberSnapshot(sourceIds, lock);
        var previewTargetId = previewTargetId(Operation.MERGE, businessHash);
        var previewTargetCode = "PREVIEW" + previewTargetId.toString().replace("-", "").toUpperCase();
        var authorizationImpact = authorizationReferenceService.previewMerge(sourceIds, sourceCodes,
                previewTargetId, previewTargetCode);
        var tokenHash = tokenHash(businessHash, memberImpact.stateFingerprint(), authorizationImpact.stateFingerprint());
        return new MergePrepared(operatorId, sourceIds, sourceCodes, sources, children, requestDepartment,
                organizationVersion, movedDepartmentCount, memberImpact, authorizationImpact, businessHash, tokenHash);
    }

    private SplitPrepared inspectSplit(DepartmentSplitPreviewFrom from, boolean lock) {
        if (from == null || from.getExpectedOrganizationVersion() == null || from.getSourceDepartmentId() == null) {
            throw new DataException("部门拆分参数不完整");
        }
        var selectedUserIds = normalizeIds(from.getUserIds(), 1, "请至少选择一名直属成员");
        var operatorId = currentOperatorId();
        var sourceRows = lock
                ? departmentMapper.selectActiveByIdsForUpdate(List.of(from.getSourceDepartmentId()))
                : departmentMapper.selectActiveByIds(List.of(from.getSourceDepartmentId()));
        var source = activeDepartments(sourceRows).stream()
                .findFirst()
                .orElseThrow(() -> new DataNotExistException("源部门不存在或已删除"));
        validateActiveParent(source.getPid());
        var requestedDepartment = departmentFrom(from.getDepartment(), source.getPid());
        var businessHash = requestHash(Operation.SPLIT, List.of(source.getId()), selectedUserIds,
                requestedDepartment, from.getExpectedOrganizationVersion());
        var organizationVersion = lock
                ? readLockedOrganizationVersion(from.getExpectedOrganizationVersion())
                : readOrganizationVersion(from.getExpectedOrganizationVersion());

        var memberImpact = lockSplitMemberSnapshot(source.getId(), selectedUserIds, lock);
        var tokenHash = tokenHash(businessHash, memberImpact.stateFingerprint(), "NO_AUTHORIZATION_BOUNDARY_REWRITE");
        return new SplitPrepared(operatorId, source, selectedUserIds, requestedDepartment, organizationVersion,
                memberImpact, businessHash, tokenHash);
    }

    private DepartmentMembershipRestructureImpact lockMergeMemberSnapshot(List<UUID> sourceIds, boolean lock) {
        var beforeLock = membershipService.previewMerge(sourceIds);
        if (!lock || beforeLock.affectedUserIds().isEmpty()) {
            return beforeLock;
        }
        var userIds = sortedIds(beforeLock.affectedUserIds());
        var lockedUsers = userMapper.selectActiveByIdsForUpdate(userIds);
        if (lockedUsers == null || lockedUsers.size() != userIds.size()) {
            throw new DataException("部门成员已变化，请重新预览");
        }
        membershipMapper.selectActiveByUsersAndDepartmentsForUpdate(userIds, sourceIds);
        var afterLock = membershipService.previewMerge(sourceIds);
        if (!beforeLock.stateFingerprint().equals(afterLock.stateFingerprint())) {
            throw new DataException("部门成员关系在预览后发生变化，请重新预览");
        }
        return afterLock;
    }

    private DepartmentMembershipRestructureImpact lockSplitMemberSnapshot(UUID sourceId,
                                                                          List<UUID> selectedUserIds,
                                                                          boolean lock) {
        var beforeLock = membershipService.previewSplit(sourceId, selectedUserIds);
        if (!lock) {
            return beforeLock;
        }
        var lockedUsers = userMapper.selectActiveByIdsForUpdate(selectedUserIds);
        if (lockedUsers == null || lockedUsers.size() != selectedUserIds.size()) {
            throw new DataException("拆分成员已变化，请重新预览");
        }
        membershipMapper.selectActiveByUsersAndDepartmentsForUpdate(selectedUserIds, List.of(sourceId));
        var afterLock = membershipService.previewSplit(sourceId, selectedUserIds);
        if (!beforeLock.stateFingerprint().equals(afterLock.stateFingerprint())) {
            throw new DataException("拆分成员关系在预览后发生变化，请重新预览");
        }
        return afterLock;
    }

    private DepartmentRestructureApplyVO executeMerge(MergePrepared prepared) {
        var target = prepared.requestedDepartment();
        target.setCode(IdWorker.get32UUID().toUpperCase());
        if (departmentMapper.insert(target) != 1 || target.getId() == null) {
            throw new DataException("创建合并部门失败");
        }
        if (!prepared.children().isEmpty()) {
            var childIds = prepared.children().stream().map(Department::getId).toList();
            int moved = departmentMapper.moveActiveChildrenToParent(childIds, prepared.sourceIds(),
                    target.getId(), prepared.operatorId());
            requireRows(moved, childIds.size(), "移动源部门子树失败");
        }
        var movedUsers = membershipService.applyMerge(prepared.sourceIds(), target.getId(), prepared.operatorId());
        if (!movedUsers.equals(prepared.memberImpact().affectedUserIds())) {
            throw new DataException("部门成员在 Apply 期间发生变化，组织重组已回滚");
        }
        var latestAuthorizationImpact = authorizationReferenceService.previewMerge(prepared.sourceIds(),
                prepared.sourceCodes(), target.getId(), target.getCode());
        if (!prepared.authorizationImpact().stateFingerprint().equals(latestAuthorizationImpact.stateFingerprint())) {
            throw new DataException("授权边界在预览后发生变化，请重新预览");
        }
        var appliedAuthorizationImpact = authorizationReferenceService.applyMerge(prepared.sourceIds(),
                prepared.sourceCodes(), target.getId(), target.getCode(), prepared.operatorId());
        if (!prepared.authorizationImpact().stateFingerprint().equals(appliedAuthorizationImpact.stateFingerprint())) {
            throw new DataException("授权边界在 Apply 期间发生变化，组织重组已回滚");
        }
        int deleted = departmentMapper.softDeleteActiveByIds(prepared.sourceIds(), prepared.operatorId());
        requireRows(deleted, prepared.sourceIds().size(), "逻辑删除源部门失败");
        rebuildOrganization(prepared.organizationVersion(), prepared.operatorId());
        var affectedUserIds = union(movedUsers, appliedAuthorizationImpact.affectedUserIds());
        revokeAffectedUsers(affectedUserIds, prepared.operatorId());
        var response = toApply(new ApplyData(Operation.MERGE, target.getId(), prepared.organizationVersion() + 1,
                prepared.movedDepartmentCount(), prepared.memberImpact(), appliedAuthorizationImpact, affectedUserIds.size()));
        appendApplyAudit(Operation.MERGE, prepared.operatorId(), target.getId(), prepared.sourceIds(), response);
        return response;
    }

    private DepartmentRestructureApplyVO executeSplit(SplitPrepared prepared) {
        var target = prepared.requestedDepartment();
        target.setCode(IdWorker.get32UUID().toUpperCase());
        if (departmentMapper.insert(target) != 1 || target.getId() == null) {
            throw new DataException("创建拆分部门失败");
        }
        var latestMemberImpact = membershipService.previewSplit(prepared.source().getId(), prepared.userIds());
        if (!prepared.memberImpact().stateFingerprint().equals(latestMemberImpact.stateFingerprint())) {
            throw new DataException("拆分成员关系在预览后发生变化，请重新预览");
        }
        var movedUsers = membershipService.applySplit(prepared.source().getId(), prepared.userIds(),
                target.getId(), prepared.operatorId());
        if (!movedUsers.equals(prepared.memberImpact().affectedUserIds())) {
            throw new DataException("拆分成员在 Apply 期间发生变化，组织重组已回滚");
        }
        rebuildOrganization(prepared.organizationVersion(), prepared.operatorId());
        revokeAffectedUsers(movedUsers, prepared.operatorId());
        var response = toApply(new ApplyData(Operation.SPLIT, target.getId(), prepared.organizationVersion() + 1,
                0, prepared.memberImpact(), null, movedUsers.size()));
        appendApplyAudit(Operation.SPLIT, prepared.operatorId(), target.getId(),
                List.of(prepared.source().getId()), response);
        return response;
    }

    private void rebuildOrganization(long expectedVersion, UUID operatorId) {
        departmentClosureMapper.clearClosure();
        if (departmentClosureMapper.rebuildClosure() < 1) {
            throw new DataException("重建部门闭包关系失败");
        }
        if (departmentMapper.rebuildActivePaths(operatorId) < 1) {
            throw new DataException("重建活动部门路径失败");
        }
        var row = organizationVersionMapper.selectSystemForUpdate();
        if (row == null
                || row.getOrganizationVersion() == null
                || row.getOrganizationVersion() != expectedVersion) {
            throw new DataException("organizationVersion 已变化，请重新生成预览");
        }
        var version = row.getVersion() == null ? 0L : row.getVersion();
        var update = new UpdateWrapper<OrganizationVersion>()
                .eq("singleton_key", "SYSTEM")
                .eq("organization_version", expectedVersion)
                .eq("version", version)
                .isNull("deleted")
                .set("organization_version", expectedVersion + 1)
                .set("changed_at", Instant.now())
                .set("updated_at", Instant.now())
                .set("updated_by", operatorId)
                .set("version", version + 1);
        if (organizationVersionMapper.update(null, update) != 1) {
            throw new DataException("organizationVersion 并发变化，组织变更已拒绝");
        }
    }

    private void revokeAffectedUsers(Set<UUID> userIds, UUID operatorId) {
        var currentToken = securityContextAccessor.currentToken();
        if (userIds.contains(operatorId) && (currentToken == null || currentToken.isBlank())) {
            throw new DataException("无法确认当前访问令牌，部门重组已拒绝");
        }
        for (var userId : sortedIds(userIds)) {
            User user = userMapper.selectById(userId);
            if (user == null || user.getDeleted() != null) {
                throw new DataNotExistException("部门重组影响用户不存在");
            }
            long securityVersion = user.getSecurityVersion() == null ? 0L : user.getSecurityVersion();
            epochGuard.assertCurrent(userId, securityVersion);
            epochGuard.advance(userId, securityVersion);
            if (operatorId.equals(userId)) {
                sessionRevocationPort.revokeUserSessionsExceptToken(userId, currentToken);
            } else {
                sessionRevocationPort.revokeUserSessions(userId);
            }
        }
    }

    private DepartmentRestructurePreviewVO toPreview(MergePrepared prepared, String token) {
        var result = basePreview(Operation.MERGE, prepared.requestedDepartment(), prepared.organizationVersion(), token);
        result.setSourceDepartmentCount(prepared.sourceIds().size());
        result.setMovedDepartmentCount(prepared.movedDepartmentCount());
        result.setPrimaryDepartmentCount(prepared.memberImpact().primaryDepartmentCount());
        result.setAssociatedDepartmentCount(prepared.memberImpact().associatedDepartmentCount());
        result.setDeduplicatedAssociatedCount(prepared.memberImpact().deduplicatedAssociatedCount());
        var affectedUsers = union(prepared.memberImpact().affectedUserIds(),
                prepared.authorizationImpact().affectedUserIds());
        result.setAffectedUserCount(affectedUsers.size());
        var authorizationImpact = prepared.authorizationImpact();
        result.setAffectedAssignmentCount(authorizationImpact.assignmentCount());
        result.setAffectedProfileCount(authorizationImpact.profileCount());
        result.setAccessRuleCount(authorizationImpact.accessRuleCount());
        result.setGrantRuleCount(authorizationImpact.grantRuleCount());
        result.setProfileAccessScopeCount(authorizationImpact.profileAccessScopeCount());
        result.setProfileGrantScopeCount(authorizationImpact.profileGrantScopeCount());
        result.setAuthorizationBoundariesChanged(authorizationImpact.accessRuleCount() > 0
                || authorizationImpact.grantRuleCount() > 0
                || authorizationImpact.profileAccessScopeCount() > 0
                || authorizationImpact.profileGrantScopeCount() > 0);
        result.setExpandsEffectiveAuthority(authorizationImpact.expandsEffectiveAuthority());
        result.setEffectiveScopeChangeSummary(authorizationImpact.expandsEffectiveAuthority()
                ? "Access/Grant 规则可能因多个源部门映射到新部门而扩大。"
                : "活动 Access/Grant 规则和未删除方案中的来源部门引用将映射到新部门。");
        return result;
    }

    private DepartmentRestructurePreviewVO toPreview(SplitPrepared prepared, String token) {
        var result = basePreview(Operation.SPLIT, prepared.requestedDepartment(), prepared.organizationVersion(), token);
        result.setSourceDepartmentCount(1);
        result.setAffectedUserCount(prepared.memberImpact().affectedUserCount());
        result.setPrimaryDepartmentCount(prepared.memberImpact().primaryDepartmentCount());
        result.setAssociatedDepartmentCount(prepared.memberImpact().associatedDepartmentCount());
        result.setDeduplicatedAssociatedCount(prepared.memberImpact().deduplicatedAssociatedCount());
        result.setAuthorizationBoundariesChanged(false);
        result.setExpandsEffectiveAuthority(false);
        result.setEffectiveScopeChangeSummary("所选成员将按新部门重新计算 RULES 数据范围；现有角色 Access/Grant 边界和授权方案模板保持不变。");
        return result;
    }

    private DepartmentRestructurePreviewVO basePreview(Operation operation, Department department,
                                                       long organizationVersion, String token) {
        var result = new DepartmentRestructurePreviewVO();
        result.setOperation(operation.name());
        result.setNewDepartmentName(department.getName());
        result.setNewDepartmentType(department.getType());
        result.setNewDepartmentParentId(department.getPid());
        result.setNewDepartmentRegionId(department.getRegionId());
        result.setExpectedOrganizationVersion(organizationVersion);
        result.setAfterOrganizationVersion(organizationVersion + 1);
        result.setExpiresAt(timeMapper.toLocalDateTime(Instant.now().plusSeconds(PREVIEW_TTL_SECONDS)));
        result.setPreviewToken(token);
        result.setHistoricalDataSummary(HISTORY_SUMMARY);
        return result;
    }

    private DepartmentRestructureApplyVO toApply(ApplyData data) {
        var result = new DepartmentRestructureApplyVO();
        result.setOperation(data.operation().name());
        result.setDepartmentId(data.departmentId());
        result.setOrganizationVersion(data.organizationVersion());
        result.setMovedDepartmentCount(data.movedDepartmentCount());
        result.setAffectedUserCount(data.affectedUserCount());
        result.setPrimaryDepartmentCount(data.memberImpact().primaryDepartmentCount());
        result.setAssociatedDepartmentCount(data.memberImpact().associatedDepartmentCount());
        result.setDeduplicatedAssociatedCount(data.memberImpact().deduplicatedAssociatedCount());
        if (data.authorizationImpact() != null) {
            result.setAffectedAssignmentCount(data.authorizationImpact().assignmentCount());
            result.setAffectedProfileCount(data.authorizationImpact().profileCount());
        }
        return result;
    }

    private record ApplyData(Operation operation, UUID departmentId, long organizationVersion,
                             int movedDepartmentCount, DepartmentMembershipRestructureImpact memberImpact,
                             DepartmentAuthorizationReferenceImpact authorizationImpact, int affectedUserCount) {
    }

    private Department departmentFrom(DepartmentRestructureDepartmentFrom from, UUID parentId) {
        if (from == null
                || from.getName() == null
                || from.getName().isBlank()
                || from.getType() == null
                || from.getRegionId() == null) {
            throw new DataException("新部门名称、类型和行政区划不能为空");
        }
        var department = new Department();
        department.setPid(parentId);
        department.setName(from.getName().trim());
        department.setType(from.getType().toString());
        department.setRegionId(from.getRegionId());
        department.setSort(from.getSort());
        department.setRemark(trimToNull(from.getRemark()));
        return department;
    }

    private List<Department> activeDepartments(List<Department> departments) {
        return departments == null
                ? List.of()
                : departments.stream()
                        .filter(department -> department != null && department.getDeleted() == null)
                        .toList();
    }

    private int countMovedDepartments(List<Department> children) {
        int count = 0;
        for (var child : children) {
            count++;
            var descendants = departmentService.getDescendantIds(child.getId());
            if (descendants != null && !descendants.isEmpty()) {
                count += activeDepartments(departmentMapper.selectActiveByIds(sortedIds(descendants))).size();
            }
        }
        return count;
    }

    private void validateActiveParent(UUID parentId) {
        if (parentId == null) {
            return;
        }
        if (activeDepartments(departmentMapper.selectActiveByIds(List.of(parentId))).size() != 1) {
            throw new DataException("共同父部门不存在或已删除");
        }
    }

    private long readOrganizationVersion(long expectedVersion) {
        var row = organizationVersionMapper.selectSystem();
        long actual = row == null || row.getOrganizationVersion() == null ? 0L : row.getOrganizationVersion();
        if (actual != expectedVersion) {
            throw new DataException("organizationVersion 已变化，请重新生成预览");
        }
        return actual;
    }

    private long readLockedOrganizationVersion(long expectedVersion) {
        var row = organizationVersionMapper.selectSystemForUpdate();
        if (row == null || row.getOrganizationVersion() == null) {
            throw new DataException("无法确认当前 organizationVersion");
        }
        if (row.getOrganizationVersion() != expectedVersion) {
            throw new DataException("organizationVersion 已变化，请重新生成预览");
        }
        return row.getOrganizationVersion();
    }

    private List<UUID> normalizeIds(Collection<UUID> ids, int minimum, String message) {
        if (ids == null || ids.size() < minimum) {
            throw new DataException(message);
        }
        var distinct = new LinkedHashSet<UUID>();
        for (var id : ids) {
            if (id == null || !distinct.add(id)) {
                throw new DataException("部门或成员 ID 不能为空且不能重复");
            }
        }
        return sortedIds(distinct);
    }

    private List<UUID> sortedIds(Collection<UUID> ids) {
        return ids.stream().filter(Objects::nonNull).distinct().sorted().toList();
    }

    private UUID currentOperatorId() {
        var operatorId = securityContextAccessor.currentUserId();
        if (operatorId == null) {
            throw new DataException("无法识别当前安全主体");
        }
        return operatorId;
    }

    private String requireToken(String token) {
        if (token == null || token.isBlank()) {
            throw new DataException("部门重组 Preview token 不能为空");
        }
        return token;
    }

    private AuthorizationChangeToken verifyTokenEnvelope(String encoded,
                                                         Operation operation,
                                                         UUID operatorId,
                                                         Long expectedVersion) {
        if (expectedVersion == null) {
            throw new DataException("组织版本不能为空");
        }
        var token = tokenService.verify(encoded);
        if (!operatorId.equals(token.operatorId())
                || !operatorId.equals(token.targetUserId())
                || !operation.marker().equals(token.roleId())
                || !requestMarker(operation, token.requestHash()).equals(token.assignmentId())) {
            throw new DataException("部门重组 token 与操作者或操作类型不匹配");
        }
        if (expectedVersion.longValue() != token.expectedVersion()) {
            throw new DataException("部门重组 token 与 organizationVersion 不匹配");
        }
        return token;
    }

    private void verifyTokenRequest(AuthorizationChangeToken token, String expectedHash) {
        if (!MessageDigest.isEqual(token.requestHash().getBytes(StandardCharsets.UTF_8),
                expectedHash.getBytes(StandardCharsets.UTF_8))) {
            throw new DataException("部门重组请求或影响快照已变化，请重新生成预览");
        }
    }

    private String issueToken(Operation operation, UUID operatorId, long version,
                              String requestHash, Instant expiresAt) {
        var token = new AuthorizationChangeToken(UUID.randomUUID(), operatorId, operatorId,
                operation.marker(), requestMarker(operation, requestHash), version, requestHash, expiresAt);
        var encoded = tokenService.issue(token);
        if (encoded == null || encoded.isBlank()) {
            throw new DataException("部门重组 token 签发失败");
        }
        return encoded;
    }

    private UUID requestMarker(Operation operation, String requestHash) {
        return UUID.nameUUIDFromBytes((operation.name() + ":" + requestHash).getBytes(StandardCharsets.UTF_8));
    }

    private UUID previewTargetId(Operation operation, String businessHash) {
        return UUID.nameUUIDFromBytes(("DEPARTMENT_RESTRUCTURE_TARGET:" + operation.name() + ":" + businessHash)
                .getBytes(StandardCharsets.UTF_8));
    }

    private String requestHash(Operation operation, List<UUID> sourceIds, List<UUID> userIds,
                               Department department, long organizationVersion) {
        var fields = new ArrayList<String>();
        fields.add(operation.name());
        fields.add(Long.toString(organizationVersion));
        sourceIds.forEach(id -> fields.add("source:" + id));
        userIds.forEach(id -> fields.add("user:" + id));
        fields.add("pid:" + department.getPid());
        fields.add("name:" + department.getName());
        fields.add("type:" + department.getType());
        fields.add("region:" + department.getRegionId());
        fields.add("sort:" + department.getSort());
        fields.add("remark:" + department.getRemark());
        return digest(String.join("|", fields));
    }

    private String tokenHash(String businessHash, String membershipFingerprint, String authorizationFingerprint) {
        return digest(businessHash + "|members:" + membershipFingerprint + "|authorization:" + authorizationFingerprint);
    }

    private String digest(String text) {
        try {
            var bytes = MessageDigest.getInstance("SHA-256").digest(text.getBytes(StandardCharsets.UTF_8));
            var result = new StringBuilder(bytes.length * 2);
            for (byte value : bytes) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("计算部门重组请求摘要失败", exception);
        }
    }

    private void assertHighRiskAllowed(Operation operation, String requestHash) {
        var gate = approvalGateProvider.getIfAvailable();
        if (gate != null) {
            gate.assertAllowed("DEPARTMENT_" + operation.name(), requestHash);
        }
    }

    private AuditRecord startApplyEvent(Operation operation, UUID operatorId,
                                        List<UUID> sourceIds, long version) {
        var before = new LinkedHashMap<String, Object>();
        before.put("operation", operation.name());
        before.put("sourceDepartmentIds", sourceIds.stream().map(UUID::toString).toList());
        before.put("organizationVersion", version);
        return auditRecordFactory.create(UUID.randomUUID(), "DEPARTMENT_RESTRUCTURE_APPLY_STARTED",
                operatorId, sourceIds.getFirst(), null, null, null, before, Map.of(),
                "通过 Preview/Apply 执行部门重组", Instant.now(), AuditRecord.Result.STARTED,
                RequestCorrelationContext.current().correlationId());
    }

    private void appendPreviewAudit(PreviewAuditInput input) {
        var details = new LinkedHashMap<String, Object>();
        details.put("operation", input.operation().name());
        details.put("sourceDepartmentIds", input.sourceIds().stream().map(UUID::toString).toList());
        details.put("organizationVersion", input.version());
        details.put("memberCount", input.memberCount());
        details.put("assignmentCount", input.assignmentCount());
        appendAudit(new AuditInput("DEPARTMENT_RESTRUCTURE_PREVIEWED", input.operatorId(), input.sourceIds().getFirst(), Map.of(),
                details, "预览部门重组"));
    }

    private record PreviewAuditInput(Operation operation, UUID operatorId, List<UUID> sourceIds,
                                     long version, int memberCount, int assignmentCount) {
    }

    private void appendApplyAudit(Operation operation, UUID operatorId, UUID targetId,
                                  List<UUID> sourceIds, DepartmentRestructureApplyVO result) {
        var after = new LinkedHashMap<String, Object>();
        after.put("operation", operation.name());
        after.put("sourceDepartmentIds", sourceIds.stream().map(UUID::toString).toList());
        after.put("departmentId", targetId.toString());
        after.put("organizationVersion", result.getOrganizationVersion());
        after.put("movedDepartmentCount", result.getMovedDepartmentCount());
        after.put("affectedUserCount", result.getAffectedUserCount());
        after.put("affectedAssignmentCount", result.getAffectedAssignmentCount());
        after.put("affectedProfileCount", result.getAffectedProfileCount());
        appendAudit(new AuditInput("DEPARTMENT_RESTRUCTURE_APPLIED", operatorId, targetId, Map.of(), after,
                "提交部门重组"));
    }

    private void appendAudit(AuditInput input) {
        var event = auditRecordFactory.create(null, input.eventType(), input.operatorId(), input.targetId(), null, null, null,
                input.before(), input.after(), input.reason(), Instant.now(), AuditRecord.Result.SUCCEEDED,
                RequestCorrelationContext.current().correlationId());
        auditService.record(event);
    }

    private record AuditInput(String eventType, UUID operatorId, UUID targetId, Map<String, Object> before,
                              Map<String, Object> after, String reason) {
    }

    private Set<UUID> union(Collection<UUID> first, Collection<UUID> second) {
        var result = new HashSet<UUID>();
        result.addAll(first);
        result.addAll(second);
        return Set.copyOf(result);
    }

    private String trimToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private void requireRows(int actual, int expected, String message) {
        if (actual != expected) {
            throw new DataException(message);
        }
    }

    private enum Operation {
        MERGE,
        SPLIT;

        UUID marker() {
            return UUID.nameUUIDFromBytes(("DEPARTMENT_RESTRUCTURE:" + name()).getBytes(StandardCharsets.UTF_8));
        }
    }

    private record MergePrepared(UUID operatorId,
                                 List<UUID> sourceIds,
                                 List<String> sourceCodes,
                                 List<Department> sources,
                                 List<Department> children,
                                 Department requestedDepartment,
                                 long organizationVersion,
                                 int movedDepartmentCount,
                                 DepartmentMembershipRestructureImpact memberImpact,
                                 DepartmentAuthorizationReferenceImpact authorizationImpact,
                                 String businessHash,
                                 String tokenHash) {
    }

    private record SplitPrepared(UUID operatorId,
                                 Department source,
                                 List<UUID> userIds,
                                 Department requestedDepartment,
                                 long organizationVersion,
                                 DepartmentMembershipRestructureImpact memberImpact,
                                 String businessHash,
                                 String tokenHash) {
    }
}
