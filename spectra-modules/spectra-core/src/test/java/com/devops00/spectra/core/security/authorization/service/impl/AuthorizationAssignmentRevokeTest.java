/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.exception.BusinessConflictException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.core.audit.AuditRecordFactory;
import com.devops00.spectra.core.security.authorization.AuthorizationGrantRequest;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.javabean.entity.AssignmentGrantBoundary;
import com.devops00.spectra.core.security.authorization.javabean.entity.AssignmentPermissionBoundary;
import com.devops00.spectra.core.security.authorization.javabean.entity.Permission;
import com.devops00.spectra.core.security.authorization.javabean.entity.RoleAssignment;
import com.devops00.spectra.core.security.authorization.javabean.entity.ScopeRule;
import com.devops00.spectra.core.security.authorization.javabean.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentRemovalFrom;
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
import com.devops00.spectra.core.security.authorization.service.GrantBoundaryService;
import com.devops00.spectra.core.security.change.AuthorizationChangeTokenService;
import com.devops00.spectra.core.security.change.AuthorizationEpochGuard;
import com.devops00.spectra.core.security.change.HighRiskApprovalGate;
import com.devops00.spectra.core.security.change.SecurityChangeExecutor;
import com.devops00.spectra.core.security.root.LastEffectiveDevOpsGuard;
import com.devops00.spectra.core.user.service.UserSecurityStateService;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthorizationAssignmentRevokeTest {

    private final SecurityRoleMapper securityRoleMapper = mock(SecurityRoleMapper.class);
    private final RolePermissionMapper rolePermissionMapper = mock(RolePermissionMapper.class);
    private final RoleGrantablePermissionMapper roleGrantablePermissionMapper = mock(RoleGrantablePermissionMapper.class);
    private final PermissionMapper permissionMapper = mock(PermissionMapper.class);
    private final RoleAssignmentMapper roleAssignmentMapper = mock(RoleAssignmentMapper.class);
    private final AssignmentPermissionBoundaryMapper permissionBoundaryMapper = mock(
            AssignmentPermissionBoundaryMapper.class);
    private final AssignmentGrantBoundaryMapper grantBoundaryMapper = mock(AssignmentGrantBoundaryMapper.class);
    private final AuthorizationScopeMapper authorizationScopeMapper = mock(AuthorizationScopeMapper.class);
    private final ScopeRuleMapper scopeRuleMapper = mock(ScopeRuleMapper.class);
    private final UserSecurityStateService userSecurityStateService = mock(UserSecurityStateService.class);
    private final AuthorizationSnapshotLoader snapshotLoader = mock(AuthorizationSnapshotLoader.class);
    private final GrantBoundaryService grantBoundaryService = mock(GrantBoundaryService.class);
    private final AuthorizationChangeTokenService tokenService = mock(AuthorizationChangeTokenService.class);
    private final AuthorizationEpochGuard epochGuard = mock(AuthorizationEpochGuard.class);
    private final SecuritySessionRevocationPort sessionRevocationPort = mock(SecuritySessionRevocationPort.class);
    private final SecurityContextAccessor securityContextAccessor = mock(SecurityContextAccessor.class);
    private final SecurityChangeExecutor securityChangeExecutor = mock(SecurityChangeExecutor.class);
    private final ObjectProvider<com.devops00.spectra.common.security.authorization.RootAuthorizationPolicy> rootPolicyProvider = mock(
            ObjectProvider.class);
    private final ObjectProvider<HighRiskApprovalGate> approvalGateProvider = mock(ObjectProvider.class);
    private final AuditService auditService = mock(AuditService.class);
    private final AuditRecordFactory auditRecordFactory = mock(AuditRecordFactory.class);
    private final TimeMapper timeMapper = mock(TimeMapper.class);
    private final LastEffectiveDevOpsGuard lastEffectiveDevOpsGuard = mock(LastEffectiveDevOpsGuard.class);

    @Test
    void revokeEvaluatesPersistedAccessAndGrantBoundaries() {
        var operatorId = UUID.randomUUID();
        var targetId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        var permissionId = UUID.randomUUID();
        var accessScopeId = UUID.randomUUID();
        var grantScopeId = UUID.randomUUID();
        var sourceDepartmentId = UUID.randomUUID();
        var targetDepartmentId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(userSecurityStateService.lockActive(targetId)).thenReturn(snapshot(targetId));
        when(roleAssignmentMapper.selectById(assignmentId)).thenReturn(assignment(assignmentId, targetId, roleId));
        when(securityRoleMapper.selectById(roleId)).thenReturn(role(roleId, false));
        when(securityRoleMapper.selectOne(any())).thenReturn(role(roleId, false));
        when(permissionBoundaryMapper.selectList(any())).thenReturn(List.of(access(assignmentId, permissionId,
                accessScopeId)));
        when(grantBoundaryMapper.selectList(any())).thenReturn(List.of(grant(assignmentId, permissionId,
                grantScopeId)));
        when(permissionMapper.selectBatchIds(any())).thenReturn(List.of(permission(permissionId, "role:read")));
        when(authorizationScopeMapper.selectBatchIds(any())).thenReturn(List.of(scope(accessScopeId, ScopeMode.RULES),
                scope(grantScopeId, ScopeMode.RULES)));
        when(scopeRuleMapper.selectList(any())).thenReturn(List.of(rule(accessScopeId, sourceDepartmentId),
                rule(grantScopeId, targetDepartmentId)));

        service().revoke(targetId, removal(assignmentId));

        var captor = ArgumentCaptor.forClass(List.class);
        verify(grantBoundaryService).evaluate(isNull(), eq(operatorId), eq(targetId), captor.capture(), eq(false));
        var request = (AuthorizationGrantRequest) captor.getValue().getFirst();
        assertEquals("role:read", request.permission());
        assertEquals(Set.of(sourceDepartmentId), request.accessScope().departmentIds());
        assertEquals(Set.of(targetDepartmentId), request.grantScope().departmentIds());
    }

    @Test
    void nonRootCannotRevokeOwnAssignment() {
        var operatorId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(userSecurityStateService.lockActive(operatorId)).thenReturn(snapshot(operatorId));
        when(roleAssignmentMapper.selectById(assignmentId)).thenReturn(assignment(assignmentId, operatorId, roleId));
        when(securityRoleMapper.selectById(roleId)).thenReturn(role(roleId, false));
        when(securityRoleMapper.selectOne(any())).thenReturn(role(roleId, false));

        assertThrows(AccessDeniedException.class, () -> service().revoke(operatorId, removal(assignmentId)));
    }

    @Test
    void nonRootCannotRevokeSystemManagedRole() {
        var operatorId = UUID.randomUUID();
        var targetId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(userSecurityStateService.lockActive(targetId)).thenReturn(snapshot(targetId));
        when(roleAssignmentMapper.selectById(assignmentId)).thenReturn(assignment(assignmentId, targetId, roleId));
        when(securityRoleMapper.selectById(roleId)).thenReturn(role(roleId, true));
        when(securityRoleMapper.selectOne(any())).thenReturn(role(roleId, true));

        assertThrows(BusinessConflictException.class, () -> service().revoke(targetId, removal(assignmentId)));
    }

    private AuthorizationAssignmentChangeServiceSupport service() {
        return new AuthorizationAssignmentChangeServiceSupport(securityRoleMapper, rolePermissionMapper,
                roleGrantablePermissionMapper, permissionMapper, roleAssignmentMapper, permissionBoundaryMapper,
                grantBoundaryMapper, authorizationScopeMapper, scopeRuleMapper, userSecurityStateService, snapshotLoader,
                grantBoundaryService, tokenService, epochGuard, sessionRevocationPort, securityContextAccessor,
                securityChangeExecutor, rootPolicyProvider, approvalGateProvider, auditService, auditRecordFactory,
                timeMapper, lastEffectiveDevOpsGuard);
    }

    private static UserSecurityStateService.UserSecuritySnapshot snapshot(UUID id) {
        return new UserSecurityStateService.UserSecuritySnapshot(id, "ACTIVE", 0L);
    }

    private static RoleAssignment assignment(UUID id, UUID userId, UUID roleId) {
        var assignment = new RoleAssignment();
        assignment.setId(id);
        assignment.setUserId(userId);
        assignment.setRoleId(roleId);
        assignment.setState(SecurityAuthorizationState.ACTIVE.name());
        assignment.setVersion(1L);
        return assignment;
    }

    private static SecurityRole role(UUID id, boolean systemManaged) {
        var role = new SecurityRole();
        role.setId(id);
        role.setCode("BUSINESS_ROLE");
        role.setState(SecurityAuthorizationState.ACTIVE.name());
        role.setRoleKind("BUSINESS");
        role.setAuthorityLevel(5);
        role.setSystemManaged(systemManaged);
        return role;
    }

    private static AssignmentPermissionBoundary access(UUID assignmentId, UUID permissionId, UUID scopeId) {
        var access = new AssignmentPermissionBoundary();
        access.setAssignmentId(assignmentId);
        access.setPermissionId(permissionId);
        access.setScopeId(scopeId);
        return access;
    }

    private static AssignmentGrantBoundary grant(UUID assignmentId, UUID permissionId, UUID scopeId) {
        var grant = new AssignmentGrantBoundary();
        grant.setAssignmentId(assignmentId);
        grant.setPermissionId(permissionId);
        grant.setScopeId(scopeId);
        return grant;
    }

    private static Permission permission(UUID id, String code) {
        var permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        return permission;
    }

    private static com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationScope scope(
                                                                                                             UUID id, ScopeMode mode) {
        var scope = new com.devops00.spectra.core.security.authorization.javabean.entity.AuthorizationScope();
        scope.setId(id);
        scope.setScopeMode(mode.name());
        return scope;
    }

    private static ScopeRule rule(UUID scopeId, UUID departmentId) {
        var rule = new ScopeRule();
        rule.setScopeId(scopeId);
        rule.setRuleType("DEPARTMENT");
        rule.setDepartmentId(departmentId);
        rule.setIncludeDescendants(false);
        return rule;
    }

    private static AuthorizationAssignmentRemovalFrom removal(UUID assignmentId) {
        var from = new AuthorizationAssignmentRemovalFrom();
        from.setAssignmentId(assignmentId);
        from.setExpectedVersion(1L);
        return from;
    }
}
