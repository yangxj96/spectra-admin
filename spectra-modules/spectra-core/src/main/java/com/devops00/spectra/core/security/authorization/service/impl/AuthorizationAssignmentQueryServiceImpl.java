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
import com.devops00.spectra.core.security.authorization.entity.AuthorizationScope;
import com.devops00.spectra.core.security.authorization.entity.Permission;
import com.devops00.spectra.core.security.authorization.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.entity.RolePermission;
import com.devops00.spectra.core.security.authorization.entity.ScopeRule;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.ScopeRuleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentQueryService;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationAssignmentView;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationBoundaryView;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthorizationAssignmentQueryServiceImpl implements AuthorizationAssignmentQueryService {

    private final RoleAssignmentMapper roleAssignmentMapper;

    private final RolePermissionMapper rolePermissionMapper;

    private final SecurityRoleMapper securityRoleMapper;

    private final AssignmentPermissionBoundaryMapper permissionBoundaryMapper;

    private final AssignmentGrantBoundaryMapper grantBoundaryMapper;

    private final PermissionMapper permissionMapper;

    private final AuthorizationScopeMapper authorizationScopeMapper;

    private final ScopeRuleMapper scopeRuleMapper;

    private final TimeMapper timeMapper;

    @Override
    @Transactional(readOnly = true)
    public List<AuthorizationAssignmentView> findByUserId(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        var assignments = roleAssignmentMapper.selectList(
                new LambdaQueryWrapper<RoleAssignment>().eq(RoleAssignment::getUserId, userId));
        if (assignments.isEmpty()) {
            return List.of();
        }

        var roleIds = assignments.stream().map(RoleAssignment::getRoleId).collect(Collectors.toSet());
        var roles = securityRoleMapper.selectBatchIds(roleIds)
                .stream()
                .collect(Collectors.toMap(SecurityRole::getId, Function.identity()));
        var validAssignments = assignments.stream()
                .filter(assignment -> {
                    var role = roles.get(assignment.getRoleId());
                    if (role == null || role.getCode() == null) {
                        log.warn(
                                "忽略引用已删除角色的 RoleAssignment: assignmentId={}, userId={}, roleId={}",
                                assignment.getId(),
                                assignment.getUserId(),
                                assignment.getRoleId());
                        return false;
                    }
                    return true;
                })
                .toList();
        if (validAssignments.isEmpty()) {
            return List.of();
        }

        roleIds = validAssignments.stream().map(RoleAssignment::getRoleId).collect(Collectors.toSet());
        var rolePermissionCounts = rolePermissionMapper.selectList(
                new LambdaQueryWrapper<RolePermission>().in(RolePermission::getRoleId, roleIds))
                .stream()
                .collect(Collectors.groupingBy(RolePermission::getRoleId, Collectors.counting()));
        var assignmentIds = validAssignments.stream().map(RoleAssignment::getId).collect(Collectors.toSet());
        var accessRows = permissionBoundaryMapper.selectList(
                new LambdaQueryWrapper<AssignmentPermissionBoundary>()
                        .in(AssignmentPermissionBoundary::getAssignmentId, assignmentIds));
        var grantRows = grantBoundaryMapper.selectList(
                new LambdaQueryWrapper<AssignmentGrantBoundary>()
                        .in(AssignmentGrantBoundary::getAssignmentId, assignmentIds));

        var permissionIds = new HashSet<UUID>();
        accessRows.forEach(row -> permissionIds.add(row.getPermissionId()));
        grantRows.forEach(row -> permissionIds.add(row.getPermissionId()));
        var permissions = permissionIds.isEmpty()
                ? Map.<UUID, Permission>of()
                : permissionMapper.selectBatchIds(permissionIds)
                        .stream()
                        .collect(Collectors.toMap(Permission::getId, Function.identity()));

        var scopeIds = accessRows.stream().map(AssignmentPermissionBoundary::getScopeId).collect(Collectors.toSet());
        scopeIds.addAll(grantRows.stream().map(AssignmentGrantBoundary::getScopeId).collect(Collectors.toSet()));
        var scopes = scopeIds.isEmpty()
                ? Map.<UUID, AuthorizationScope>of()
                : authorizationScopeMapper.selectBatchIds(scopeIds)
                        .stream()
                        .collect(Collectors.toMap(
                                AuthorizationScope::getId,
                                Function.identity()));
        var rules = scopeIds.isEmpty()
                ? List.<ScopeRule>of()
                : scopeRuleMapper.selectList(
                        new LambdaQueryWrapper<ScopeRule>().in(ScopeRule::getScopeId, scopeIds));
        var rulesByScope = rules.stream().collect(Collectors.groupingBy(ScopeRule::getScopeId));
        var accessByAssignment = accessRows.stream()
                .collect(Collectors.groupingBy(AssignmentPermissionBoundary::getAssignmentId));
        var grantByAssignment = grantRows.stream()
                .collect(Collectors.groupingBy(AssignmentGrantBoundary::getAssignmentId));

        return validAssignments.stream().map(assignment -> {
            var role = roles.get(assignment.getRoleId());
            return new AuthorizationAssignmentView(
                    assignment.getId(),
                    assignment.getUserId(),
                    assignment.getRoleId(),
                    role.getCode(),
                    role.getRoleKind(),
                    role.getName(),
                    role.getSystemManaged(),
                    role.getState(),
                    role.getVersion() == null ? Long.valueOf(0L) : role.getVersion(),
                    rolePermissionCounts.getOrDefault(assignment.getRoleId(), 0L),
                    assignment.getVersion() == null ? Long.valueOf(0L) : assignment.getVersion(),
                    assignment.getState(),
                    timeMapper.toLocalDateTime(assignment.getValidFrom()),
                    timeMapper.toLocalDateTime(assignment.getValidUntil()),
                    accessByAssignment.getOrDefault(assignment.getId(), List.of())
                            .stream()
                            .map(row -> toBoundary(row.getPermissionId(), row.getScopeId(), permissions, scopes, rulesByScope))
                            .toList(),
                    grantByAssignment.getOrDefault(assignment.getId(), List.of())
                            .stream()
                            .map(row -> toBoundary(row.getPermissionId(), row.getScopeId(), permissions, scopes, rulesByScope))
                            .toList());
        }).toList();
    }

    /**
     * 转换、解析或规范化数据（{@code toBoundary}）。
     */
    private AuthorizationBoundaryView toBoundary(
                                                 UUID permissionId,
                                                 UUID scopeId,
                                                 Map<UUID, Permission> permissions,
                                                 Map<UUID, AuthorizationScope> scopes,
                                                 Map<UUID, List<ScopeRule>> rulesByScope) {
        var permission = permissions.get(permissionId);
        var scope = scopes.get(scopeId);
        if (permission == null || permission.getCode() == null) {
            throw new IllegalStateException("Boundary 引用了不存在的 Permission: " + permissionId);
        }
        if (scope == null || scope.getScopeMode() == null) {
            throw new IllegalStateException("Boundary 引用了不存在的 Scope: " + scopeId);
        }
        var ruleViews = rulesByScope.getOrDefault(scopeId, List.of())
                .stream()
                .map(rule -> new AuthorizationBoundaryView.ScopeRuleView(
                        rule.getRuleType(),
                        rule.getDepartmentId(),
                        Boolean.TRUE.equals(rule.getIncludeDescendants())))
                .toList();
        return new AuthorizationBoundaryView(
                permission.getCode(),
                scope.getScopeMode(),
                scope.getResourceCode(),
                ruleViews);
    }
}
