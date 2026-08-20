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
import com.devops00.spectra.core.security.authorization.entity.ScopeRule;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentGrantBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AssignmentPermissionBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationScopeMapper;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.ScopeRuleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentQueryService;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationAssignmentView;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationBoundaryView;
import lombok.RequiredArgsConstructor;
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
public class AuthorizationAssignmentQueryServiceImpl implements AuthorizationAssignmentQueryService {

    private final RoleAssignmentMapper roleAssignmentMapper;

    private final SecurityRoleMapper securityRoleMapper;

    private final AssignmentPermissionBoundaryMapper permissionBoundaryMapper;

    private final AssignmentGrantBoundaryMapper grantBoundaryMapper;

    private final PermissionMapper permissionMapper;

    private final AuthorizationScopeMapper authorizationScopeMapper;

    private final ScopeRuleMapper scopeRuleMapper;

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
        var assignmentIds = assignments.stream().map(RoleAssignment::getId).collect(Collectors.toSet());
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
                ? Map.<UUID, com.devops00.spectra.core.security.authorization.entity.AuthorizationScope>of()
                : authorizationScopeMapper.selectBatchIds(scopeIds)
                        .stream()
                        .collect(Collectors.toMap(
                                com.devops00.spectra.core.security.authorization.entity.AuthorizationScope::getId,
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

        return assignments.stream().map(assignment -> {
            var role = roles.get(assignment.getRoleId());
            if (role == null || role.getCode() == null) {
                throw new IllegalStateException("RoleAssignment 引用了不存在的 Role: " + assignment.getRoleId());
            }
            return new AuthorizationAssignmentView(
                    assignment.getId(),
                    assignment.getUserId(),
                    assignment.getRoleId(),
                    role.getCode(),
                    role.getRoleKind(),
                    role.getName(),
                    role.getSystemManaged(),
                    role.getVersion() == null ? 0L : role.getVersion(),
                    assignment.getVersion() == null ? 0L : assignment.getVersion(),
                    assignment.getState(),
                    assignment.getValidFrom(),
                    assignment.getValidUntil(),
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

    private AuthorizationBoundaryView toBoundary(
                                                 UUID permissionId,
                                                 UUID scopeId,
                                                 Map<UUID, Permission> permissions,
                                                 Map<UUID, com.devops00.spectra.core.security.authorization.entity.AuthorizationScope> scopes,
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
