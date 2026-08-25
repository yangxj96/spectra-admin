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

package com.devops00.spectra.core.security.authorization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.security.authorization.domain.RoleAuthorizationState;
import com.devops00.spectra.core.security.authorization.domain.RoleChangeImpact;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.entity.Permission;
import com.devops00.spectra.core.security.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.entity.RoleGrantablePermission;
import com.devops00.spectra.core.security.authorization.entity.RolePermission;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.javabean.from.RoleAuthorizationApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.RoleAuthorizationChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.RoleAuthorizationChangePreviewVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.RoleAuthorizationStateVO;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.service.GrantBoundaryService;
import com.devops00.spectra.core.security.authorization.service.RoleAuthorizationChangeService;
import com.devops00.spectra.core.security.authorization.service.RoleChangeImpactAnalyzer;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.authorization.AuthorizationGrantRequest;
import com.devops00.spectra.security.base.authorization.AuthorizationScope;
import com.devops00.spectra.security.base.authorization.ScopeMode;
import com.devops00.spectra.core.security.authorization.service.AuthorizationSnapshotLoader;
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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Role 授权能力变更实现。
 * <p>
 * Role 的 Permission、GrantablePermission 和 authorityLevel 必须以同一 Preview/Apply 命令提交；
 * Apply 成功后所有受影响用户递增 securityVersion 并撤销 Session。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class RoleAuthorizationChangeServiceImpl implements RoleAuthorizationChangeService {

    private final SecurityRoleMapper roleMapper;
    private final PermissionMapper permissionMapper;
    private final RolePermissionMapper rolePermissionMapper;
    private final RoleGrantablePermissionMapper roleGrantablePermissionMapper;
    private final RoleAssignmentMapper roleAssignmentMapper;
    private final UserMapper userMapper;
    private final AuthorizationSnapshotLoader snapshotLoader;
    private final GrantBoundaryService grantBoundaryService;
    private final RoleChangeImpactAnalyzer impactAnalyzer;
    private final AuthorizationChangeTokenService tokenService;
    private final AuthorizationEpochGuard epochGuard;
    private final SecuritySessionRevocationPort sessionRevocationPort;

    private final SecurityContextAccessor securityContextAccessor;
    private final SecurityChangeExecutor securityChangeExecutor;
    private final ObjectProvider<RootAuthorizationPolicy> rootPolicyProvider;
    private final ObjectProvider<HighRiskApprovalGate> approvalGateProvider;

    private final SecurityAuditWriter securityAuditWriter;

    private final TimeMapper timeMapper;

    @Override
    public RoleAuthorizationStateVO current(UUID roleId) {
        var role = roleMapper.selectById(roleId);
        if (role == null) {
            throw new DataNotExistException("目标 Role 不存在");
        }
        var state = currentState(roleId, role);
        var result = new RoleAuthorizationStateVO();
        result.setRoleId(roleId);
        result.setVersion(role.getVersion() == null ? Long.valueOf(0L) : role.getVersion());
        result.setAuthorityLevel(state.authorityLevel());
        result.setPermissionCodes(state.permissions());
        result.setGrantablePermissionCodes(state.grantablePermissions());
        return result;
    }

    @Override
    public RoleAuthorizationChangePreviewVO preview(UUID roleId, RoleAuthorizationChangeFrom from) {
        var prepared = prepare(roleId, from);
        var expiresAt = Instant.now().plusSeconds(300);
        var token = new AuthorizationChangeToken(UUID.randomUUID(), prepared.operatorId(), prepared.operatorId(), roleId,
                UUID.randomUUID(), prepared.expectedVersion(), prepared.requestHash(), expiresAt);
        var result = new RoleAuthorizationChangePreviewVO();
        result.setRoleId(roleId);
        result.setPreviewToken(tokenService.issue(token));
        result.setExpectedVersion(prepared.expectedVersion());
        result.setExpiresAt(timeMapper.toLocalDateTime(expiresAt));
        result.setAffectedAssignmentCount(prepared.impact().affectedAssignmentCount());
        result.setAffectedUserCount(prepared.impact().affectedUserCount());
        result.setExpandsEffectiveAuthority(prepared.impact().expandsEffectiveAuthority());
        appendAudit("AUTHORIZATION_IMPACT_PREVIEWED", prepared.operatorId(), roleId,
                Map.of("affectedUserCount", prepared.impact().affectedUserCount()), Map.of(), "Role 授权变更预览");
        return result;
    }

    @Override
    @Transactional
    public void apply(UUID roleId, RoleAuthorizationApplyFrom from) {
        var token = tokenService.verify(from.getPreviewToken());
        var operatorId = currentOperatorId();
        if (!operatorId.equals(token.operatorId())
                || !operatorId.equals(token.targetUserId())
                || !roleId.equals(token.roleId())) {
            throw new DataException("Role 授权变更 token 与当前操作者或目标 Role 不匹配");
        }
        if (!from.getExpectedVersion().equals(token.expectedVersion())) {
            throw new DataException("Role 授权变更 token 与 Role version 不匹配");
        }
        var prepared = prepare(roleId, from);
        if (!prepared.requestHash().equals(token.requestHash())) {
            throw new DataException("Role 授权变更请求已被修改，请重新生成预览");
        }
        var event = new SecurityAuditEvent(UUID.randomUUID(), "AUTHORIZATION_IMPACT_APPLIED", operatorId, roleId,
                null, null, null, Map.of("roleId", roleId.toString()),
                Map.of("affectedUserCount", prepared.impact().affectedUserCount(),
                        "expandsEffectiveAuthority", prepared.impact().expandsEffectiveAuthority()),
                "通过 Role Authorization Preview/Apply 提交", null, AuditResult.STARTED, null);
        securityChangeExecutor.execute(event, () -> {
            persist(prepared);
            recordRoleAuthorizationEvents(prepared, roleId);
            return Boolean.TRUE;
        });
    }

    /**
     * 创建或构建目标数据（{@code prepare}）。
     */
    private PreparedChange prepare(UUID roleId, RoleAuthorizationChangeFrom from) {
        if (roleId == null
                || from == null
                || from.getExpectedVersion() == null
                || from.getAuthorityLevel() == null
                || from.getPermissionCodes() == null
                || from.getGrantablePermissionCodes() == null) {
            throw new DataException("Role 授权变更参数不能为空");
        }
        var operatorId = currentOperatorId();
        var role = roleMapper.selectById(roleId);
        if (role == null || !SecurityAuthorizationState.ACTIVE.name().equals(role.getState())) {
            throw new DataNotExistException("目标 Role 不存在或已停用");
        }
        if (Boolean.TRUE.equals(role.getSystemManaged()) || !"BUSINESS".equals(role.getRoleKind())) {
            throw new BuiltinDataException("内置角色不可修改授权");
        }
        if (from.getAuthorityLevel() > 999) {
            throw new DataException("普通角色授权管理等级必须在 1 到 999 之间");
        }
        long expectedVersion = role.getVersion() == null ? 0L : role.getVersion();
        if (expectedVersion != from.getExpectedVersion()) {
            throw new DataException("Role version 已变化，请重新生成授权变更预览");
        }
        var before = currentState(roleId, role);
        var after = requestedState(from);
        if (!after.grantablePermissions().stream().allMatch(after.permissions()::contains)) {
            throw new DataException("GrantablePermission 必须同时存在于 Role Permission 集合");
        }
        var impact = impactAnalyzer.analyze(before, after, activeAssignments(roleId).size(), affectedUserIds(roleId).size());
        var requests = requestsForAddedCapabilities(before, after);
        var rootPolicy = rootPolicyProvider.getIfAvailable();
        boolean root = rootPolicy != null && rootPolicy.isRoot(SecurityContextHolder.getContext().getAuthentication());
        if (!requests.isEmpty()) {
            grantBoundaryService.evaluate(snapshotLoader.load(operatorId), operatorId, null, requests, root);
        }
        var requestHash = requestHash(roleId, expectedVersion, after);
        var approvalGate = approvalGateProvider.getIfAvailable();
        if (approvalGate != null) {
            approvalGate.assertAllowed("ROLE_AUTHORIZATION_CHANGE", requestHash);
        }
        return new PreparedChange(operatorId, role, expectedVersion, before, after, impact, requestHash);
    }

    /**
     * 查询或获取目标数据（{@code currentState}）。
     */
    private RoleAuthorizationState currentState(UUID roleId, SecurityRole role) {
        var permissionIds = rolePermissionMapper.selectList(new LambdaQueryWrapper<RolePermission>()
                .eq(RolePermission::getRoleId, roleId)).stream().map(RolePermission::getPermissionId).collect(Collectors.toSet());
        var grantableIds = roleGrantablePermissionMapper.selectList(new LambdaQueryWrapper<RoleGrantablePermission>()
                .eq(RoleGrantablePermission::getRoleId, roleId))
                .stream()
                .map(RoleGrantablePermission::getPermissionId)
                .collect(Collectors.toSet());
        return new RoleAuthorizationState(role.getAuthorityLevel() == null ? 1 : role.getAuthorityLevel(),
                permissionCodes(permissionIds), permissionCodes(grantableIds));
    }

    /**
     * 处理内部业务逻辑（{@code requestedState}）。
     */
    private RoleAuthorizationState requestedState(RoleAuthorizationChangeFrom from) {
        if (from.getAuthorityLevel() <= 0) {
            throw new DataException("authorityLevel 必须大于 0");
        }
        validatePermissionCodes(from.getPermissionCodes());
        validatePermissionCodes(from.getGrantablePermissionCodes());
        return new RoleAuthorizationState(from.getAuthorityLevel(), from.getPermissionCodes(),
                from.getGrantablePermissionCodes());
    }

    /**
     * 处理内部业务逻辑（{@code permissionCodes}）。
     */
    private Set<String> permissionCodes(Set<UUID> ids) {
        if (ids.isEmpty()) {
            return Set.of();
        }
        return permissionMapper.selectBatchIds(ids).stream().map(Permission::getCode).collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 校验并确保数据满足当前约束（{@code validatePermissionCodes}）。
     */
    private void validatePermissionCodes(Set<String> codes) {
        if (codes.isEmpty()) {
            return;
        }
        var rows = permissionMapper.selectList(new LambdaQueryWrapper<Permission>().in(Permission::getCode, codes));
        var active = rows.stream()
                .filter(permission -> SecurityAuthorizationState.ACTIVE.name().equals(permission.getState()))
                .map(Permission::getCode)
                .collect(Collectors.toSet());
        if (active.size() != codes.size() || !active.containsAll(codes)) {
            throw new DataNotExistException("Role Permission Catalog 中存在无效或停用 Permission");
        }
    }

    /**
     * 处理内部业务逻辑（{@code requestsForAddedCapabilities}）。
     */
    private List<AuthorizationGrantRequest> requestsForAddedCapabilities(RoleAuthorizationState before,
                                                                         RoleAuthorizationState after) {
        var added = new HashSet<>(after.permissions());
        added.removeAll(before.permissions());
        added.addAll(after.grantablePermissions());
        added.removeAll(before.grantablePermissions());
        if (after.authorityLevel() != before.authorityLevel()) {
            added.add("role:authority-level:update");
        }
        return added.stream()
                .sorted()
                .map(permission -> new AuthorizationGrantRequest(permission, AuthorizationScope.of(
                        ScopeMode.NONE),
                        AuthorizationScope.of(ScopeMode.NONE),
                        after.authorityLevel()))
                .toList();
    }

    /**
     * 处理内部业务逻辑（{@code activeAssignments}）。
     */
    private List<RoleAssignment> activeAssignments(UUID roleId) {
        return roleAssignmentMapper.selectList(new LambdaQueryWrapper<RoleAssignment>()
                .eq(RoleAssignment::getRoleId, roleId)
                .eq(RoleAssignment::getState, SecurityAuthorizationState.ACTIVE.name()));
    }

    /**
     * 处理内部业务逻辑（{@code affectedUserIds}）。
     */
    private Set<UUID> affectedUserIds(UUID roleId) {
        return activeAssignments(roleId).stream().map(RoleAssignment::getUserId).collect(Collectors.toSet());
    }

    /**
     * 处理内部业务逻辑（{@code persist}）。
     */
    private void persist(PreparedChange prepared) {
        var role = prepared.role();
        var roleUpdate = new LambdaUpdateWrapper<SecurityRole>().eq(SecurityRole::getId, role.getId())
                .eq(SecurityRole::getVersion, prepared.expectedVersion())
                .set(SecurityRole::getAuthorityLevel, prepared.after().authorityLevel())
                .set(SecurityRole::getVersion, prepared.expectedVersion() + 1);
        if (roleMapper.update(null, roleUpdate) != 1) {
            throw new DataException("Role version 并发变化，授权变更已拒绝");
        }
        var permissionIds = permissionIds(prepared.after().permissions());
        var grantableIds = permissionIds(prepared.after().grantablePermissions());
        rolePermissionMapper.delete(new LambdaQueryWrapper<RolePermission>().eq(RolePermission::getRoleId, role.getId()));
        roleGrantablePermissionMapper.delete(new LambdaQueryWrapper<RoleGrantablePermission>()
                .eq(RoleGrantablePermission::getRoleId, role.getId()));
        insertRolePermissions(role.getId(), permissionIds, false);
        insertRolePermissions(role.getId(), grantableIds, true);
        for (var userId : affectedUserIds(role.getId())) {
            var user = userMapper.selectById(userId);
            if (user == null) {
                throw new DataNotExistException("RoleAssignment 目标用户不存在");
            }
            var version = user.getSecurityVersion() == null ? 0L : user.getSecurityVersion();
            epochGuard.assertCurrent(userId, version);
            epochGuard.advance(userId, version);
            sessionRevocationPort.revokeUserSessions(userId);
        }
    }

    /**
     * 处理内部业务逻辑（{@code permissionIds}）。
     */
    private Set<UUID> permissionIds(Set<String> codes) {
        if (codes.isEmpty()) {
            return Set.of();
        }
        return permissionMapper.selectList(new LambdaQueryWrapper<Permission>().in(Permission::getCode, codes))
                .stream()
                .collect(Collectors.toMap(Permission::getCode, Function.identity()))
                .values()
                .stream()
                .map(Permission::getId)
                .collect(Collectors.toUnmodifiableSet());
    }

    /**
     * 处理内部业务逻辑（{@code insertRolePermissions}）。
     */
    private void insertRolePermissions(UUID roleId, Set<UUID> permissionIds, boolean grantable) {
        for (var permissionId : permissionIds) {
            if (grantable) {
                var row = new RoleGrantablePermission();
                row.setRoleId(roleId);
                row.setPermissionId(permissionId);
                roleGrantablePermissionMapper.insert(row);
            } else {
                var row = new RolePermission();
                row.setRoleId(roleId);
                row.setPermissionId(permissionId);
                rolePermissionMapper.insert(row);
            }
        }
    }

    /**
     * 处理内部业务逻辑（{@code requestHash}）。
     */
    private String requestHash(UUID roleId, long expectedVersion, RoleAuthorizationState state) {
        var canonical = roleId + "|" + expectedVersion + "|" + state.authorityLevel() + "|"
                + state.permissions().stream().sorted().collect(Collectors.joining(",")) + "|"
                + state.grantablePermissions().stream().sorted().collect(Collectors.joining(","));
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(canonical.getBytes(StandardCharsets.UTF_8));
            var result = new StringBuilder();
            for (byte value : digest) {
                result.append(String.format("%02x", value));
            }
            return result.toString();
        } catch (Exception exception) {
            throw new IllegalStateException("计算 Role 授权变更摘要失败", exception);
        }
    }

    /**
     * 查询或获取目标数据（{@code currentOperatorId}）。
     */
    private UUID currentOperatorId() {
        var operatorId = securityContextAccessor.currentUserId();
        if (operatorId == null) {
            throw new DataException("无法识别当前安全主体");
        }
        return operatorId;
    }

    /**
     * 更新或推进目标状态（{@code recordRoleAuthorizationEvents}）。
     */
    private void recordRoleAuthorizationEvents(PreparedChange prepared, UUID roleId) {
        if (!prepared.before().permissions().equals(prepared.after().permissions())) {
            appendAudit("ROLE_PERMISSION_CHANGED", prepared.operatorId(), roleId,
                    Map.of("permissions", prepared.before().permissions()),
                    Map.of("permissions", prepared.after().permissions()), null);
        }
        if (!prepared.before().grantablePermissions().equals(prepared.after().grantablePermissions())) {
            appendAudit("ROLE_GRANTABLE_PERMISSION_CHANGED", prepared.operatorId(), roleId,
                    Map.of("grantablePermissions", prepared.before().grantablePermissions()),
                    Map.of("grantablePermissions", prepared.after().grantablePermissions()), null);
        }
        if (prepared.before().authorityLevel() != prepared.after().authorityLevel()) {
            appendAudit("ROLE_AUTHORITY_LEVEL_CHANGED", prepared.operatorId(), roleId,
                    Map.of("authorityLevel", prepared.before().authorityLevel()),
                    Map.of("authorityLevel", prepared.after().authorityLevel()), null);
        }
    }

    /**
     * 更新或推进目标状态（{@code appendAudit}）。
     */
    private void appendAudit(String eventType, UUID operatorId, UUID targetId, Map<String, Object> before,
                             Map<String, Object> after, String reason) {
        securityAuditWriter.append(new SecurityAuditEvent(null, eventType, operatorId, targetId, null, null, null,
                before, after, reason, null, AuditResult.SUCCEEDED, null));
    }

    private record PreparedChange(UUID operatorId,
                                  SecurityRole role,
                                  long expectedVersion,
                                  RoleAuthorizationState before,
                                  RoleAuthorizationState after,
                                  RoleChangeImpact impact,
                                  String requestHash) {
    }
}
