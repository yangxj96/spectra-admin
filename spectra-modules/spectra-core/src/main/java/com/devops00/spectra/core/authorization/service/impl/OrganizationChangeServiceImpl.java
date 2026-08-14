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

package com.devops00.spectra.core.authorization.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.authorization.AuthorizationSnapshotLoader;
import com.devops00.spectra.core.authorization.domain.OrganizationChangeImpact;
import com.devops00.spectra.core.authorization.javabean.from.OrganizationChangeApplyFrom;
import com.devops00.spectra.core.authorization.javabean.from.OrganizationChangeFrom;
import com.devops00.spectra.core.authorization.javabean.vo.OrganizationChangePreviewVO;
import com.devops00.spectra.core.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.authorization.service.OrganizationChangeService;
import com.devops00.spectra.core.authorization.service.OrganizationImpactAnalyzer;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.javabean.entity.OrganizationVersion;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.system.mapper.OrganizationVersionMapper;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.authorization.AuthorizationGrantRequest;
import com.devops00.spectra.security.base.authorization.AuthorizationScope;
import com.devops00.spectra.security.base.authorization.ScopeMode;
import com.devops00.spectra.security.base.change.AuthorizationChangeToken;
import com.devops00.spectra.security.base.change.AuthorizationChangeTokenService;
import com.devops00.spectra.security.base.change.AuthorizationEpochGuard;
import com.devops00.spectra.security.base.change.HighRiskApprovalGate;
import com.devops00.spectra.security.base.change.SecurityChangeExecutor;
import com.devops00.spectra.security.base.change.SecuritySessionRevocationPort;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.root.RootAuthorizationPolicy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 部门移动的组织版本与授权影响门禁。
 * <p>
 * 组织树变化统一提升 organizationVersion，并对当前可能受层级规则影响的用户执行安全版本
 * 与 Session 撤销；后续 Closure Table 维护在同一事务中接入。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrganizationChangeServiceImpl implements OrganizationChangeService {

    private final DepartmentMapper departmentMapper;
    private final OrganizationVersionMapper organizationVersionMapper;
    private final RoleAssignmentMapper roleAssignmentMapper;
    private final UserMapper userMapper;
    private final DepartmentService departmentService;
    private final AuthorizationSnapshotLoader snapshotLoader;
    private final com.devops00.spectra.core.authorization.service.GrantBoundaryService grantBoundaryService;
    private final OrganizationImpactAnalyzer impactAnalyzer;
    private final AuthorizationChangeTokenService tokenService;
    private final AuthorizationEpochGuard epochGuard;
    private final SecuritySessionRevocationPort sessionRevocationPort;

    private final SecurityContextAccessor securityContextAccessor;
    private final SecurityChangeExecutor securityChangeExecutor;
    private final ObjectProvider<RootAuthorizationPolicy> rootPolicyProvider;
    private final ObjectProvider<HighRiskApprovalGate> approvalGateProvider;

    private final SecurityAuditWriter securityAuditWriter;

    @Override
    public OrganizationChangePreviewVO preview(UUID departmentId, OrganizationChangeFrom from) {
        var prepared = prepare(departmentId, from);
        var expiresAt = Instant.now().plusSeconds(300);
        var token = new AuthorizationChangeToken(UUID.randomUUID(), prepared.operatorId(), prepared.operatorId(),
                departmentId, UUID.randomUUID(), prepared.impact().beforeVersion(), prepared.requestHash(), expiresAt);
        var result = new OrganizationChangePreviewVO();
        result.setDepartmentId(departmentId);
        result.setNewParentId(prepared.newParentId());
        result.setPreviewToken(tokenService.issue(token));
        result.setExpectedOrganizationVersion(prepared.impact().beforeVersion());
        result.setAfterOrganizationVersion(prepared.impact().afterVersion());
        result.setExpiresAt(expiresAt);
        result.setAffectedAssignmentCount(prepared.impact().affectedAssignmentCount());
        result.setAffectedUserCount(prepared.impact().affectedUserCount());
        result.setExpandsEffectiveAuthority(prepared.impact().expandsEffectiveAuthority());
        appendAudit("AUTHORIZATION_IMPACT_PREVIEWED", prepared.operatorId(), departmentId,
                Map.of("organizationVersion", prepared.impact().beforeVersion()), Map.of(), "组织变更预览");
        return result;
    }

    @Override
    @Transactional
    public void apply(UUID departmentId, OrganizationChangeApplyFrom from) {
        var token = tokenService.verify(from.getPreviewToken());
        var operatorId = currentOperatorId();
        if (!operatorId.equals(token.operatorId()) || !operatorId.equals(token.targetUserId())
                || !departmentId.equals(token.roleId())) {
            throw new DataException("组织变更 token 与当前操作者或目标部门不匹配");
        }
        if (!from.getExpectedOrganizationVersion().equals(token.expectedVersion())) {
            throw new DataException("组织变更 token 与 organizationVersion 不匹配");
        }
        var prepared = prepare(departmentId, from);
        if (!prepared.requestHash().equals(token.requestHash())) {
            throw new DataException("组织变更请求已被修改，请重新生成预览");
        }
        var event = new SecurityAuditEvent(UUID.randomUUID(), "AUTHORIZATION_IMPACT_APPLIED", operatorId, departmentId,
                null, null, null, Map.of("organizationVersion", prepared.impact().beforeVersion()),
                Map.of("organizationVersion", prepared.impact().afterVersion(), "newParentId",
                        String.valueOf(prepared.newParentId())),
                "通过组织变更 Preview/Apply 提交", null, AuditResult.STARTED, null);
        securityChangeExecutor.execute(event, () -> {
            persist(prepared);
            appendAudit("ORGANIZATION_NODE_MOVED", operatorId, departmentId,
                    Map.of("organizationVersion", prepared.impact().beforeVersion()),
                    Map.of("organizationVersion", prepared.impact().afterVersion(),
                            "newParentId", String.valueOf(prepared.newParentId())), null);
            return Boolean.TRUE;
        });
    }

    private PreparedChange prepare(UUID departmentId, OrganizationChangeFrom from) {
        if (departmentId == null || from == null || from.getExpectedOrganizationVersion() == null) {
            throw new DataException("组织变更参数不能为空");
        }
        var department = departmentMapper.selectById(departmentId);
        if (department == null) {
            throw new DataNotExistException("目标部门不存在");
        }
        var currentVersion = currentOrganizationVersion();
        if (currentVersion != from.getExpectedOrganizationVersion()) {
            throw new DataException("organizationVersion 已变化，请重新生成预览");
        }
        var newParentId = from.getNewParentId();
        if (Objects.equals(departmentId, newParentId)) {
            throw new DataException("部门不能移动到自身");
        }
        if (newParentId != null) {
            if (departmentMapper.selectById(newParentId) == null) {
                throw new DataNotExistException("新的上级部门不存在");
            }
            if (departmentService.getSelfAndDescendantIds(departmentId).contains(newParentId)) {
                throw new DataException("部门不能移动到自己的下级节点");
            }
        }
        var assignments = roleAssignmentMapper.selectList(new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<RoleAssignment>()
                .eq(RoleAssignment::getState, "ACTIVE"));
        var userIds = assignments.stream().map(RoleAssignment::getUserId).collect(java.util.stream.Collectors.toSet());
        var impact = impactAnalyzer.analyze(currentVersion, assignments.size(), userIds.size(), true);
        var operatorId = currentOperatorId();
        var rootPolicy = rootPolicyProvider.getIfAvailable();
        boolean root = rootPolicy != null && rootPolicy.isRoot(SecurityContextHolder.getContext().getAuthentication());
        grantBoundaryService.evaluate(snapshotLoader.load(operatorId), operatorId, null,
                List.of(new AuthorizationGrantRequest("department:update", AuthorizationScope.of(ScopeMode.NONE),
                        AuthorizationScope.of(ScopeMode.NONE), 1)), root);
        var requestHash = requestHash(departmentId, newParentId, currentVersion);
        var approvalGate = approvalGateProvider.getIfAvailable();
        if (approvalGate != null) {
            approvalGate.assertAllowed("ORGANIZATION_CHANGE", requestHash);
        }
        return new PreparedChange(operatorId, department, newParentId, impact, requestHash, userIds);
    }

    private long currentOrganizationVersion() {
        var row = organizationVersionMapper.selectById("SYSTEM");
        return row == null || row.getVersion() == null ? 0L : row.getVersion();
    }

    private void persist(PreparedChange prepared) {
        var departmentUpdate = new LambdaUpdateWrapper<Department>().eq(Department::getId, prepared.department().getId())
                .set(Department::getPid, prepared.newParentId());
        if (departmentMapper.update(null, departmentUpdate) != 1) {
            throw new DataException("部门移动失败");
        }
        departmentMapper.clearClosure();
        departmentMapper.rebuildClosure();
        var versionUpdate = new LambdaUpdateWrapper<OrganizationVersion>().eq(OrganizationVersion::getSingletonKey, "SYSTEM")
                .eq(OrganizationVersion::getVersion, prepared.impact().beforeVersion())
                .setSql("version = version + 1");
        if (organizationVersionMapper.update(null, versionUpdate) != 1) {
            throw new DataException("organizationVersion 并发变化，组织变更已拒绝");
        }
        for (var userId : prepared.affectedUserIds()) {
            var user = userMapper.selectById(userId);
            if (user == null) {
                throw new DataNotExistException("组织授权影响用户不存在");
            }
            var version = user.getSecurityVersion() == null ? 0L : user.getSecurityVersion();
            epochGuard.assertCurrent(userId, version);
            epochGuard.advance(userId, version);
            sessionRevocationPort.revokeUserSessions(userId);
        }
    }

    private String requestHash(UUID departmentId, UUID newParentId, long version) {
        var canonical = departmentId + "|" + newParentId + "|" + version;
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            var result = new StringBuilder();
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("计算组织变更摘要失败", exception);
        }
    }

    private UUID currentOperatorId() {
        var operatorId = securityContextAccessor.currentUserId();
        if (operatorId == null) {
            throw new DataException("无法识别当前安全主体");
        }
        return operatorId;
    }

    private void appendAudit(String eventType, UUID operatorId, UUID targetId, Map<String, Object> before,
                             Map<String, Object> after, String reason) {
        securityAuditWriter.append(new SecurityAuditEvent(null, eventType, operatorId, targetId, null, null, null,
                before, after, reason, null, AuditResult.SUCCEEDED, null));
    }

    private record PreparedChange(UUID operatorId,
                                  Department department,
                                  UUID newParentId,
                                  OrganizationChangeImpact impact,
                                  String requestHash,
                                  Set<UUID> affectedUserIds) {
    }
}
