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

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.authorization.entity.AssignmentGrantBoundary;
import com.devops00.spectra.core.authorization.entity.AssignmentPermissionBoundary;
import com.devops00.spectra.core.authorization.entity.Permission;
import com.devops00.spectra.core.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.authorization.entity.RoleGrantablePermission;
import com.devops00.spectra.core.authorization.entity.RolePermission;
import com.devops00.spectra.core.authorization.entity.ScopeRule;
import com.devops00.spectra.core.authorization.entity.SecurityRole;
import com.devops00.spectra.core.authorization.javabean.from.AuthorizationAssignmentApplyFrom;
import com.devops00.spectra.core.authorization.javabean.from.AuthorizationAssignmentChangeFrom;
import com.devops00.spectra.core.authorization.javabean.from.AuthorizationBoundaryFrom;
import com.devops00.spectra.core.authorization.javabean.from.AuthorizationScopeFrom;
import com.devops00.spectra.core.authorization.javabean.vo.AuthorizationChangePreviewVO;
import com.devops00.spectra.core.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.authorization.mapper.ScopeRuleMapper;
import com.devops00.spectra.core.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.authorization.service.AuthorizationAssignmentChangeService;
import com.devops00.spectra.core.authorization.service.GrantBoundaryService;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.authorization.AuthorizationGrantRequest;
import com.devops00.spectra.security.base.authorization.AuthorizationScope;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshot;
import com.devops00.spectra.security.base.authorization.ScopeMode;
import com.devops00.spectra.security.base.change.AuthorizationChangeToken;
import com.devops00.spectra.security.base.change.AuthorizationChangeTokenService;
import com.devops00.spectra.security.base.change.AuthorizationEpochGuard;
import com.devops00.spectra.security.base.change.HighRiskApprovalGate;
import com.devops00.spectra.security.base.change.SecuritySessionRevocationPort;
import com.devops00.spectra.security.base.change.SecurityChangeExecutor;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 目标 RoleAssignment 写入实现。
 * <p>
 * 所有写入先执行 Role/Permission 一致性校验和 Grant Boundary，再通过 Audit、Epoch、Session revoke
 * 的统一变更骨架提交；旧用户角色关系表不参与该流程。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthorizationAssignmentChangeServiceImpl implements AuthorizationAssignmentChangeService {

    private final SecurityRoleMapper securityRoleMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final RoleGrantablePermissionMapper roleGrantablePermissionMapper;
    private final PermissionMapper permissionMapper;
    private final RoleAssignmentMapper roleAssignmentMapper;
    private final AssignmentPermissionBoundaryMapper permissionBoundaryMapper;
    private final AssignmentGrantBoundaryMapper grantBoundaryMapper;
    private final AuthorizationScopeMapper authorizationScopeMapper;
    private final ScopeRuleMapper scopeRuleMapper;
    private final UserMapper userMapper;
    private final com.devops00.spectra.core.authorization.AuthorizationSnapshotLoader snapshotLoader;
    private final GrantBoundaryService grantBoundaryService;
    private final AuthorizationChangeTokenService tokenService;
    private final AuthorizationEpochGuard epochGuard;
    private final SecuritySessionRevocationPort sessionRevocationPort;

    private final SecurityContextAccessor securityContextAccessor;
    private final SecurityChangeExecutor securityChangeExecutor;
    private final ObjectProvider<RootAuthorizationPolicy> rootAuthorizationPolicy;
    private final ObjectProvider<HighRiskApprovalGate> approvalGateProvider;

    private final SecurityAuditWriter securityAuditWriter;

    @Override
    public AuthorizationChangePreviewVO preview(UUID targetUserId, AuthorizationAssignmentChangeFrom from) {
        var prepared = prepare(targetUserId, from, from.getAssignmentId());
        var expiresAt = Instant.now().plusSeconds(300);
        var token = new AuthorizationChangeToken(UUID.randomUUID(), prepared.operatorId(), targetUserId,
                prepared.role().getId(), prepared.assignmentId(), from.getExpectedVersion(), prepared.requestHash(), expiresAt);
        var result = new AuthorizationChangePreviewVO();
        result.setPreviewToken(tokenService.issue(token));
        result.setTargetUserId(targetUserId);
        result.setAssignmentId(prepared.assignmentId());
        result.setExpectedVersion(from.getExpectedVersion());
        result.setExpiresAt(expiresAt);
        result.setAffectedAssignmentCount(1);
        result.setAffectedUserCount(1);
        appendAudit("AUTHORIZATION_IMPACT_PREVIEWED", prepared.operatorId(), targetUserId,
                Map.of("assignmentId", prepared.assignmentId().toString()), Map.of(), "Assignment 授权变更预览");
        return result;
    }

    @Override
    @Transactional
    public void apply(UUID targetUserId, AuthorizationAssignmentApplyFrom from) {
        var token = tokenService.verify(from.getPreviewToken());
        var operatorId = currentOperatorId();
        if (!operatorId.equals(token.operatorId()) || !targetUserId.equals(token.targetUserId())) {
            throw new DataException("授权变更 token 与当前操作者或目标不匹配");
        }
        if (!from.getExpectedVersion().equals(token.expectedVersion())) {
            throw new DataException("授权变更 token 与安全版本不匹配");
        }
        UUID assignmentId = from.getAssignmentId() == null ? token.assignmentId() : from.getAssignmentId();
        if (!assignmentId.equals(token.assignmentId())) {
            throw new DataException("授权变更 token 与 Assignment 不匹配");
        }
        var prepared = prepare(targetUserId, from, assignmentId);
        if (!prepared.requestHash().equals(token.requestHash())) {
            throw new DataException("授权变更请求已被修改，请重新生成预览");
        }
        var event = new SecurityAuditEvent(UUID.randomUUID(), "AUTHORIZATION_IMPACT_APPLIED", operatorId, targetUserId,
                null, null, null, Map.of("assignmentId", assignmentId.toString()),
                Map.of("roleId", prepared.role().getId().toString(), "permissionCount", prepared.requests().size()),
                "通过 Grant Boundary Preview/Apply 提交", null, AuditResult.STARTED, null);
        securityChangeExecutor.execute(event, () -> {
            persist(prepared, targetUserId);
            recordAssignmentEvents(prepared, targetUserId);
            epochGuard.advance(targetUserId, prepared.targetSecurityVersion());
            sessionRevocationPort.revokeUserSessions(targetUserId);
            return Boolean.TRUE;
        });
    }

    private PreparedChange prepare(UUID targetUserId, AuthorizationAssignmentChangeFrom from, UUID assignmentId) {
        if (targetUserId == null || from == null || from.getExpectedVersion() == null) {
            throw new DataException("授权变更参数不能为空");
        }
        var operatorId = currentOperatorId();
        var target = userMapper.selectById(targetUserId);
        if (target == null) {
            throw new DataNotExistException("目标用户不存在");
        }
        if (target.getStatus() == null || !"ACTIVE".equals(target.getStatus().getCode())) {
            throw new DataException("只有 ACTIVE 用户可以建立授权 Assignment");
        }
        var assignment = assignmentId == null ? null : roleAssignmentMapper.selectById(assignmentId);
        if (assignment != null && !targetUserId.equals(assignment.getUserId())) {
            throw new DataException("Assignment 不属于目标用户");
        }
        long assignmentVersion = assignment == null || assignment.getVersion() == null ? 0L : assignment.getVersion();
        if (assignmentVersion != from.getExpectedVersion()) {
            throw new DataException("Assignment version 已变化，请重新生成授权变更预览");
        }
        long targetSecurityVersion = target.getSecurityVersion() == null ? 0L : target.getSecurityVersion();
        epochGuard.assertCurrent(targetUserId, targetSecurityVersion);
        var role = requireActiveRole(from.getRoleId());
        var requests = toGrantRequests(role, from.getBoundaries());
        var rootPolicy = rootAuthorizationPolicy.getIfAvailable();
        var root = rootPolicy != null && rootPolicy.isRoot(SecurityContextHolder.getContext().getAuthentication());
        AuthorizationSnapshot snapshot = snapshotLoader.load(operatorId);
        grantBoundaryService.evaluate(snapshot, operatorId, targetUserId, requests, root);
        UUID effectiveAssignmentId = assignmentId == null ? UUID.randomUUID() : assignmentId;
        var requestHash = requestHash(effectiveAssignmentId, role.getId(), from.getExpectedVersion(), targetSecurityVersion,
                requests);
        var approvalGate = approvalGateProvider.getIfAvailable();
        if (approvalGate != null) {
            approvalGate.assertAllowed("ROLE_ASSIGNMENT_CHANGE", requestHash);
        }
        return new PreparedChange(operatorId, role, assignment, effectiveAssignmentId, requests,
                from.getExpectedVersion(), targetSecurityVersion, requestHash);
    }

    private SecurityRole requireActiveRole(UUID roleId) {
        if (roleId == null) {
            throw new DataException("目标 Role 不能为空");
        }
        var role = securityRoleMapper.selectById(roleId);
        if (role == null || !"ACTIVE".equals(role.getState())) {
            throw new DataNotExistException("目标 Role 不存在或已停用");
        }
        if (role.getAuthorityLevel() == null || role.getAuthorityLevel() <= 0) {
            throw new DataException("目标 Role authorityLevel 无效");
        }
        return role;
    }

    private List<AuthorizationGrantRequest> toGrantRequests(SecurityRole role, List<AuthorizationBoundaryFrom> boundaries) {
        if (boundaries == null || boundaries.isEmpty()) {
            throw new DataException("授权 Boundary 不能为空");
        }
        var permissionRows = permissionMapper.selectList(new LambdaQueryWrapper<Permission>()
                .in(Permission::getCode, boundaries.stream().map(AuthorizationBoundaryFrom::getPermission).toList()));
        var permissions = permissionRows.stream().collect(Collectors.toMap(Permission::getCode, value -> value));
        var rolePermissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, role.getId()))
                .stream()
                .map(RolePermission::getPermissionId)
                .collect(Collectors.toSet());
        var grantableIds = roleGrantablePermissionMapper.selectList(new LambdaQueryWrapper<RoleGrantablePermission>()
                .eq(RoleGrantablePermission::getRoleId, role.getId()))
                .stream()
                .map(RoleGrantablePermission::getPermissionId)
                .collect(Collectors.toSet());
        var seen = new HashSet<String>();
        var result = new ArrayList<AuthorizationGrantRequest>();
        for (var boundary : boundaries) {
            if (!seen.add(boundary.getPermission())) {
                throw new DataException("同一 Permission 不能重复提交 Boundary");
            }
            var permission = permissions.get(boundary.getPermission());
            if (permission == null || !"ACTIVE".equals(permission.getState())) {
                throw new DataNotExistException("Permission 不存在或已停用: " + boundary.getPermission());
            }
            if (!rolePermissionIds.contains(permission.getId())) {
                throw new DataException("目标 Role 未声明 Permission: " + boundary.getPermission());
            }
            if (boundary.getGrant() != null && !grantableIds.contains(permission.getId())) {
                throw new DataException("目标 Role 未声明 GrantablePermission: " + boundary.getPermission());
            }
            result.add(new AuthorizationGrantRequest(boundary.getPermission(), toDomainScope(boundary.getAccess()),
                    boundary.getGrant() == null ? null : toDomainScope(boundary.getGrant()), role.getAuthorityLevel()));
        }
        return List.copyOf(result);
    }

    private AuthorizationScope toDomainScope(AuthorizationScopeFrom source) {
        if (source == null || source.getMode() == null) {
            throw new DataException("Scope 参数不能为空");
        }
        return new AuthorizationScope(source.getMode(), source.getDepartmentIds() == null
                ? Set.of()
                : Set.copyOf(source.getDepartmentIds()), source.isIncludeDescendants());
    }

    private void persist(PreparedChange prepared, UUID targetUserId) {
        var assignment = prepared.assignment() == null ? new RoleAssignment() : prepared.assignment();
        assignment.setId(prepared.assignmentId());
        assignment.setUserId(targetUserId);
        assignment.setRoleId(prepared.role().getId());
        assignment.setState("ACTIVE");
        assignment.setValidFrom(assignment.getValidFrom() == null ? Instant.now() : assignment.getValidFrom());
        if (prepared.assignment() == null) {
            assignment.setVersion(prepared.expectedVersion());
            if (roleAssignmentMapper.insert(assignment) != 1) {
                throw new DataException("创建 RoleAssignment 失败");
            }
        } else {
            var update = new LambdaUpdateWrapper<RoleAssignment>().eq(RoleAssignment::getId, prepared.assignmentId())
                    .eq(RoleAssignment::getVersion, prepared.expectedVersion())
                    .set(RoleAssignment::getRoleId, assignment.getRoleId())
                    .set(RoleAssignment::getState, assignment.getState())
                    .set(RoleAssignment::getValidFrom, assignment.getValidFrom())
                    .set(RoleAssignment::getValidUntil, assignment.getValidUntil())
                    .set(RoleAssignment::getVersion, prepared.expectedVersion() + 1);
            if (roleAssignmentMapper.update(null, update) != 1) {
                throw new DataException("Assignment version 并发变化，授权变更已拒绝");
            }
        }
        var oldAccess = permissionBoundaryMapper.selectList(new LambdaQueryWrapper<AssignmentPermissionBoundary>()
                .eq(AssignmentPermissionBoundary::getAssignmentId, prepared.assignmentId()));
        var oldGrant = grantBoundaryMapper.selectList(new LambdaQueryWrapper<AssignmentGrantBoundary>()
                .eq(AssignmentGrantBoundary::getAssignmentId, prepared.assignmentId()));
        var oldScopeIds = new HashSet<UUID>();
        oldAccess.forEach(row -> oldScopeIds.add(row.getScopeId()));
        oldGrant.forEach(row -> oldScopeIds.add(row.getScopeId()));
        permissionBoundaryMapper.delete(new LambdaQueryWrapper<AssignmentPermissionBoundary>()
                .eq(AssignmentPermissionBoundary::getAssignmentId, prepared.assignmentId()));
        grantBoundaryMapper.delete(new LambdaQueryWrapper<AssignmentGrantBoundary>()
                .eq(AssignmentGrantBoundary::getAssignmentId, prepared.assignmentId()));
        if (!oldScopeIds.isEmpty()) {
            scopeRuleMapper.delete(new LambdaQueryWrapper<ScopeRule>().in(ScopeRule::getScopeId, oldScopeIds));
            authorizationScopeMapper.deleteByIds(oldScopeIds);
        }
        for (var request : prepared.requests()) {
            UUID permissionId = permissionMapper.selectOne(new LambdaQueryWrapper<Permission>()
                    .eq(Permission::getCode, request.permission())).getId();
            UUID accessScopeId = saveScope(request.permission(), request.accessScope());
            var access = new AssignmentPermissionBoundary();
            access.setAssignmentId(prepared.assignmentId());
            access.setPermissionId(permissionId);
            access.setScopeId(accessScopeId);
            permissionBoundaryMapper.insert(access);
            if (request.grantScope() != null) {
                UUID grantScopeId = saveScope(request.permission(), request.grantScope());
                var grant = new AssignmentGrantBoundary();
                grant.setAssignmentId(prepared.assignmentId());
                grant.setPermissionId(permissionId);
                grant.setScopeId(grantScopeId);
                grantBoundaryMapper.insert(grant);
            }
        }
    }

    private UUID saveScope(String permission, AuthorizationScope scope) {
        var entity = new com.devops00.spectra.core.authorization.entity.AuthorizationScope();
        entity.setId(UUID.randomUUID());
        entity.setScopeMode(scope.mode().name());
        entity.setResourceCode(permission);
        authorizationScopeMapper.insert(entity);
        for (var departmentId : scope.departmentIds()) {
            var rule = new ScopeRule();
            rule.setId(UUID.randomUUID());
            rule.setScopeId(entity.getId());
            rule.setRuleType("DEPARTMENT");
            rule.setDepartmentId(departmentId);
            rule.setIncludeDescendants(scope.includeDescendants());
            scopeRuleMapper.insert(rule);
        }
        return entity.getId();
    }

    private UUID currentOperatorId() {
        var operatorId = securityContextAccessor.currentUserId();
        if (operatorId == null) {
            throw new DataException("无法识别当前安全主体");
        }
        return operatorId;
    }

    private void recordAssignmentEvents(PreparedChange prepared, UUID targetUserId) {
        appendAudit(prepared.assignment() == null ? "ROLE_ASSIGNMENT_CREATED" : "ROLE_ASSIGNMENT_UPDATED",
                prepared.operatorId(), targetUserId, Map.of("assignmentId", prepared.assignmentId().toString()),
                Map.of("roleId", prepared.role().getId().toString(), "state", "ACTIVE"), null);
        if (!prepared.requests().isEmpty()) {
            appendAudit("ASSIGNMENT_PERMISSION_BOUNDARY_CHANGED", prepared.operatorId(), targetUserId,
                    Map.of("assignmentId", prepared.assignmentId().toString()),
                    Map.of("permissionCount", prepared.requests().size()), null);
        }
        if (prepared.requests().stream().anyMatch(request -> request.grantScope() != null)) {
            appendAudit("ASSIGNMENT_GRANT_BOUNDARY_CHANGED", prepared.operatorId(), targetUserId,
                    Map.of("assignmentId", prepared.assignmentId().toString()),
                    Map.of("grantablePermissionCount", prepared.requests()
                            .stream()
                            .filter(request -> request.grantScope() != null)
                            .count()),
                    null);
        }
    }

    private void appendAudit(String eventType, UUID operatorId, UUID targetId, Map<String, Object> before,
                             Map<String, Object> after, String reason) {
        securityAuditWriter.append(new SecurityAuditEvent(null, eventType, operatorId, targetId, null, null, null,
                before, after, reason, null, AuditResult.SUCCEEDED, null));
    }

    private String requestHash(UUID assignmentId,
                               UUID roleId,
                               long expectedVersion,
                               long targetSecurityVersion,
                               List<AuthorizationGrantRequest> requests) {
        var canonical = new StringBuilder().append(assignmentId)
                .append('|')
                .append(roleId)
                .append('|')
                .append(expectedVersion)
                .append('|')
                .append(targetSecurityVersion);
        requests.stream()
                .sorted(Comparator.comparing(AuthorizationGrantRequest::permission))
                .forEach(request -> canonical
                        .append('|')
                        .append(request.permission())
                        .append(':')
                        .append(scopeText(request.accessScope()))
                        .append(':')
                        .append(scopeText(request.grantScope()))
                        .append(':')
                        .append(request.targetAuthorityLevel()));
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(canonical.toString().getBytes(StandardCharsets.UTF_8));
            var result = new StringBuilder();
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("计算授权变更摘要失败", exception);
        }
    }

    private String scopeText(AuthorizationScope scope) {
        if (scope == null) {
            return "-";
        }
        return scope.mode() + ":" + scope.departmentIds().stream().sorted().map(UUID::toString).collect(Collectors.joining(","))
                + ":" + scope.includeDescendants();
    }

    private record PreparedChange(UUID operatorId,
                                  SecurityRole role,
                                  RoleAssignment assignment,
                                  UUID assignmentId,
                                  List<AuthorizationGrantRequest> requests,
                                  long expectedVersion,
                                  long targetSecurityVersion,
                                  String requestHash) {
    }
}
