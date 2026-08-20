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
import com.devops00.spectra.core.security.authorization.entity.AssignmentGrantBoundary;
import com.devops00.spectra.core.security.authorization.entity.AssignmentPermissionBoundary;
import com.devops00.spectra.core.security.authorization.entity.Permission;
import com.devops00.spectra.core.security.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.entity.RoleGrantablePermission;
import com.devops00.spectra.core.security.authorization.entity.RolePermission;
import com.devops00.spectra.core.security.authorization.entity.ScopeRule;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.ScopeRuleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.service.AuthorizationSnapshotLoader;
import com.devops00.spectra.security.base.authorization.AuthorizationAssignment;
import com.devops00.spectra.security.base.authorization.AuthorizationScope;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshot;
import com.devops00.spectra.security.base.authorization.PermissionBoundary;
import com.devops00.spectra.security.base.authorization.ScopeMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 从目标 security schema 读取授权快照。任何结构缺失都会 fail-closed，绝不补成 ALL。
 */
@Service
@RequiredArgsConstructor
public class JdbcAuthorizationSnapshotLoader implements AuthorizationSnapshotLoader {

    private static final String ACTIVE = "ACTIVE";

    private final RoleAssignmentMapper roleAssignmentMapper;

    private final SecurityRoleMapper securityRoleMapper;

    private final RolePermissionMapper rolePermissionMapper;

    private final RoleGrantablePermissionMapper roleGrantablePermissionMapper;

    private final PermissionMapper permissionMapper;

    private final AuthorizationScopeMapper authorizationScopeMapper;

    private final ScopeRuleMapper scopeRuleMapper;

    private final AssignmentPermissionBoundaryMapper permissionBoundaryMapper;

    private final AssignmentGrantBoundaryMapper grantBoundaryMapper;

    @Override
    public AuthorizationSnapshot load(UUID userId) {
        if (userId == null) {
            return AuthorizationSnapshot.of(List.of());
        }

        var now = Instant.now();
        var assignmentQuery = new LambdaQueryWrapper<RoleAssignment>()
                .eq(RoleAssignment::getUserId, userId)
                .eq(RoleAssignment::getState, ACTIVE);
        var assignments = roleAssignmentMapper.selectList(assignmentQuery)
                .stream()
                .filter(assignment -> assignment.getValidFrom() == null
                        || !assignment.getValidFrom().isAfter(now))
                .filter(assignment -> assignment.getValidUntil() == null
                        || assignment.getValidUntil().isAfter(now))
                .toList();
        if (assignments.isEmpty()) {
            return AuthorizationSnapshot.of(List.of());
        }

        var roleIds = assignments.stream()
                .map(RoleAssignment::getRoleId)
                .collect(Collectors.toSet());
        var roleRows = securityRoleMapper.selectBatchIds(roleIds);
        var roles = roleRows.stream()
                .filter(role -> ACTIVE.equals(role.getState()) && role.getCode() != null)
                .collect(Collectors.toMap(SecurityRole::getId, Function.identity()));
        var rolePermissions = loadRolePermissions(roleIds);
        var roleGrantablePermissions = loadRoleGrantablePermissions(roleIds);

        var assignmentIds = assignments.stream()
                .map(RoleAssignment::getId)
                .collect(Collectors.toSet());
        var accessRows = permissionBoundaryMapper.selectList(
                new LambdaQueryWrapper<AssignmentPermissionBoundary>()
                        .in(AssignmentPermissionBoundary::getAssignmentId, assignmentIds));
        var grantRows = grantBoundaryMapper.selectList(
                new LambdaQueryWrapper<AssignmentGrantBoundary>()
                        .in(AssignmentGrantBoundary::getAssignmentId, assignmentIds));

        var permissionIds = new HashSet<UUID>();
        accessRows.forEach(row -> permissionIds.add(row.getPermissionId()));
        grantRows.forEach(row -> permissionIds.add(row.getPermissionId()));
        var permissionRows = permissionIds.isEmpty()
                ? List.<Permission>of()
                : permissionMapper.selectBatchIds(permissionIds);
        var permissions = permissionRows.stream()
                .filter(permission -> ACTIVE.equals(permission.getState())
                        && permission.getCode() != null)
                .collect(Collectors.toMap(Permission::getId, Function.identity()));

        var scopeIds = accessRows.stream()
                .map(AssignmentPermissionBoundary::getScopeId)
                .collect(Collectors.toSet());
        scopeIds.addAll(grantRows.stream()
                .map(AssignmentGrantBoundary::getScopeId)
                .collect(Collectors.toSet()));
        var scopeRows = scopeIds.isEmpty()
                ? List.<com.devops00.spectra.core.security.authorization.entity.AuthorizationScope>of()
                : authorizationScopeMapper.selectBatchIds(scopeIds);
        var scopes = scopeRows.stream()
                .collect(Collectors.toMap(
                        com.devops00.spectra.core.security.authorization.entity.AuthorizationScope::getId,
                        Function.identity()));
        var rules = scopeIds.isEmpty()
                ? List.<ScopeRule>of()
                : scopeRuleMapper.selectList(
                        new LambdaQueryWrapper<ScopeRule>().in(ScopeRule::getScopeId, scopeIds));

        var accessByAssignment = accessRows.stream()
                .collect(Collectors.groupingBy(AssignmentPermissionBoundary::getAssignmentId));
        var grantByAssignment = grantRows.stream()
                .collect(Collectors.groupingBy(AssignmentGrantBoundary::getAssignmentId));
        var result = assignments.stream()
                .map(assignment -> {
                    var roleId = assignment.getRoleId();
                    var role = requireRole(roles, roleId);
                    return new AuthorizationAssignment(
                            assignment.getId(),
                            role.getCode(),
                            role.getAuthorityLevel() == null ? 1 : role.getAuthorityLevel(),
                            toAccessBoundaries(
                                    accessByAssignment.getOrDefault(assignment.getId(), List.of()),
                                    permissions,
                                    scopes,
                                    rules,
                                    rolePermissions.getOrDefault(roleId, Set.of())),
                            toGrantBoundaries(
                                    grantByAssignment.getOrDefault(assignment.getId(), List.of()),
                                    permissions,
                                    scopes,
                                    rules,
                                    roleGrantablePermissions.getOrDefault(roleId, Set.of())));
                })
                .toList();
        return AuthorizationSnapshot.of(result);
    }

    private Map<UUID, Set<UUID>> loadRolePermissions(Set<UUID> roleIds) {
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        return rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds))
                .stream()
                .collect(Collectors.groupingBy(
                        RolePermission::getRoleId,
                        Collectors.mapping(RolePermission::getPermissionId, Collectors.toSet())));
    }

    private Map<UUID, Set<UUID>> loadRoleGrantablePermissions(Set<UUID> roleIds) {
        if (roleIds.isEmpty()) {
            return Map.of();
        }
        return roleGrantablePermissionMapper.selectList(
                new LambdaQueryWrapper<RoleGrantablePermission>()
                        .in(RoleGrantablePermission::getRoleId, roleIds))
                .stream()
                .collect(Collectors.groupingBy(
                        RoleGrantablePermission::getRoleId,
                        Collectors.mapping(RoleGrantablePermission::getPermissionId, Collectors.toSet())));
    }

    private Map<String, PermissionBoundary> toAccessBoundaries(
                                                               List<AssignmentPermissionBoundary> rows,
                                                               Map<UUID, Permission> permissions,
                                                               Map<UUID, com.devops00.spectra.core.security.authorization.entity.AuthorizationScope> scopes,
                                                               List<ScopeRule> rules,
                                                               Set<UUID> rolePermissionIds) {
        var result = new HashMap<String, PermissionBoundary>();
        for (var row : rows) {
            requireRolePermission(rolePermissionIds, row.getPermissionId());
            var permission = requirePermission(permissions, row.getPermissionId());
            result.put(permission.getCode(), new PermissionBoundary(
                    permission.getCode(), toScope(scopes, rules, row.getScopeId())));
        }
        return result;
    }

    private Map<String, PermissionBoundary> toGrantBoundaries(
                                                              List<AssignmentGrantBoundary> rows,
                                                              Map<UUID, Permission> permissions,
                                                              Map<UUID, com.devops00.spectra.core.security.authorization.entity.AuthorizationScope> scopes,
                                                              List<ScopeRule> rules,
                                                              Set<UUID> roleGrantablePermissionIds) {
        var result = new HashMap<String, PermissionBoundary>();
        for (var row : rows) {
            requireRolePermission(roleGrantablePermissionIds, row.getPermissionId());
            var permission = requirePermission(permissions, row.getPermissionId());
            result.put(permission.getCode(), new PermissionBoundary(
                    permission.getCode(), toScope(scopes, rules, row.getScopeId())));
        }
        return result;
    }

    private AuthorizationScope toScope(
                                       Map<UUID, com.devops00.spectra.core.security.authorization.entity.AuthorizationScope> scopes,
                                       List<ScopeRule> rules,
                                       UUID scopeId) {
        var scope = scopes.get(scopeId);
        if (scope == null || scope.getScopeMode() == null) {
            throw new IllegalStateException("授权边界引用了不存在的 scope: " + scopeId);
        }
        ScopeMode mode;
        try {
            mode = ScopeMode.valueOf(scope.getScopeMode());
        } catch (IllegalArgumentException exception) {
            throw new IllegalStateException("不支持的 scope mode: " + scope.getScopeMode(), exception);
        }
        var scopeRules = rules.stream().filter(rule -> scopeId.equals(rule.getScopeId())).toList();
        if (mode != ScopeMode.RULES && !scopeRules.isEmpty()) {
            throw new IllegalStateException("非 RULES scope 不应包含 scope_rule: " + scopeId);
        }
        var departments = scopeRules.stream()
                .filter(rule -> "DEPARTMENT".equals(rule.getRuleType())
                        && rule.getDepartmentId() != null)
                .map(ScopeRule::getDepartmentId)
                .collect(Collectors.toSet());
        if (mode == ScopeMode.RULES && departments.isEmpty()) {
            throw new IllegalStateException("RULES scope 缺少部门规则: " + scopeId);
        }
        var includeDescendants = scopeRules.stream()
                .anyMatch(rule -> Boolean.TRUE.equals(rule.getIncludeDescendants()));
        return new AuthorizationScope(mode, departments, includeDescendants);
    }

    private SecurityRole requireRole(Map<UUID, SecurityRole> roles, UUID roleId) {
        var role = roles.get(roleId);
        if (role == null) {
            throw new IllegalStateException("RoleAssignment 引用了不存在或停用的 Role: " + roleId);
        }
        return role;
    }

    private Permission requirePermission(Map<UUID, Permission> permissions, UUID permissionId) {
        var permission = permissions.get(permissionId);
        if (permission == null) {
            throw new IllegalStateException("授权边界引用了不存在或停用的 Permission: " + permissionId);
        }
        return permission;
    }

    private void requireRolePermission(Set<UUID> rolePermissionIds, UUID permissionId) {
        if (!rolePermissionIds.contains(permissionId)) {
            throw new IllegalStateException("授权边界引用了 Role 未声明的 Permission: " + permissionId);
        }
    }
}
