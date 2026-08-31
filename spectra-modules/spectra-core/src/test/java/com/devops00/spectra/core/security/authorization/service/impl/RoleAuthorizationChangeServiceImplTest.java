/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.security.authorization.domain.RoleChangeImpact;
import com.devops00.spectra.core.security.authorization.entity.Permission;
import com.devops00.spectra.core.security.authorization.entity.RoleGrantablePermission;
import com.devops00.spectra.core.security.authorization.entity.RolePermission;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.javabean.from.RoleAuthorizationChangeFrom;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.javabean.vo.RoleAuthorizationStateVO;
import com.devops00.spectra.core.security.authorization.service.GrantBoundaryService;
import com.devops00.spectra.core.security.authorization.service.RoleChangeImpactAnalyzer;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.change.AuthorizationChangeTokenService;
import com.devops00.spectra.security.base.change.AuthorizationEpochGuard;
import com.devops00.spectra.security.base.change.HighRiskApprovalGate;
import com.devops00.spectra.security.base.change.SecurityChangeExecutor;
import com.devops00.spectra.security.base.change.SecuritySessionRevocationPort;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.root.RootAuthorizationPolicy;
import com.devops00.spectra.core.security.authorization.service.AuthorizationSnapshotLoader;
import com.devops00.spectra.core.security.audit.outbox.SecurityChangeOutboxProducer;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Role 目标授权状态查询测试。
 */
@ExtendWith(MockitoExtension.class)
class RoleAuthorizationChangeServiceImplTest {

    @Mock
    private SecurityRoleMapper roleMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Mock
    private RoleGrantablePermissionMapper roleGrantablePermissionMapper;

    @Mock
    private RoleAssignmentMapper roleAssignmentMapper;

    @Mock
    private UserMapper userMapper;

    @Mock
    private AuthorizationSnapshotLoader snapshotLoader;

    @Mock
    private GrantBoundaryService grantBoundaryService;

    @Mock
    private RoleChangeImpactAnalyzer impactAnalyzer;

    @Mock
    private AuthorizationChangeTokenService tokenService;

    @Mock
    private AuthorizationEpochGuard epochGuard;

    @Mock
    private SecuritySessionRevocationPort sessionRevocationPort;

    @Mock
    private SecurityContextAccessor securityContextAccessor;

    @Mock
    private SecurityChangeExecutor securityChangeExecutor;

    @Mock
    private ObjectProvider<RootAuthorizationPolicy> rootPolicyProvider;

    @Mock
    private ObjectProvider<HighRiskApprovalGate> approvalGateProvider;

    @Mock
    private SecurityAuditWriter securityAuditWriter;

    @Mock
    private SecurityChangeOutboxProducer securityChangeOutboxProducer;

    @Mock
    private TimeMapper timeMapper;

    @InjectMocks
    private RoleAuthorizationChangeServiceImpl service;

    @Test
    void currentShouldReadTargetPermissionAndGrantablePermissionCodes() {
        var roleId = UUID.randomUUID();
        var permissionId = UUID.randomUUID();
        var grantableId = UUID.randomUUID();
        var role = role(roleId, 7L);
        var permission = permission(permissionId, "user:read");
        var grantable = permission(grantableId, "user:assign");
        var rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(permissionId);
        var roleGrantable = new RoleGrantablePermission();
        roleGrantable.setRoleId(roleId);
        roleGrantable.setPermissionId(grantableId);

        when(roleMapper.selectById(roleId)).thenReturn(role);
        when(rolePermissionMapper.selectList(any())).thenReturn(List.of(rolePermission));
        when(roleGrantablePermissionMapper.selectList(any())).thenReturn(List.of(roleGrantable));
        when(permissionMapper.selectBatchIds(anyCollection())).thenReturn(List.of(permission), List.of(grantable));

        RoleAuthorizationStateVO result = service.current(roleId);

        assertEquals(roleId, result.getRoleId());
        assertEquals(7L, result.getVersion());
        assertEquals(1, result.getAuthorityLevel());
        assertEquals(List.of("user:read"), result.getPermissionCodes().stream().sorted().toList());
        assertEquals(List.of("user:assign"), result.getGrantablePermissionCodes().stream().sorted().toList());
    }

    @Test
    void currentShouldRejectMissingTargetRole() {
        var roleId = UUID.randomUUID();
        when(roleMapper.selectById(roleId)).thenReturn(null);

        assertThrows(DataNotExistException.class, () -> service.current(roleId));
    }

    @Test
    void currentShouldReadDisabledTargetRole() {
        var roleId = UUID.randomUUID();
        var role = role(roleId, 3L);
        role.setState("DISABLED");
        when(roleMapper.selectById(roleId)).thenReturn(role);
        when(rolePermissionMapper.selectList(any())).thenReturn(List.of());
        when(roleGrantablePermissionMapper.selectList(any())).thenReturn(List.of());

        RoleAuthorizationStateVO result = service.current(roleId);

        assertEquals(roleId, result.getRoleId());
        assertEquals(3L, result.getVersion());
        assertEquals(1, result.getAuthorityLevel());
        assertEquals(Set.of(), result.getPermissionCodes());
        assertEquals(Set.of(), result.getGrantablePermissionCodes());
    }

    @Test
    void previewShouldAllowRoleWithoutPermissions() {
        var roleId = UUID.randomUUID();
        var operatorId = UUID.randomUUID();
        var role = role(roleId, 1L);
        when(securityContextAccessor.currentUserId()).thenReturn(operatorId);
        when(roleMapper.selectById(roleId)).thenReturn(role);
        when(rolePermissionMapper.selectList(any())).thenReturn(List.of());
        when(roleGrantablePermissionMapper.selectList(any())).thenReturn(List.of());
        when(roleAssignmentMapper.selectList(any())).thenReturn(List.of());
        when(impactAnalyzer.analyze(any(), any(), eq(0), eq(0)))
                .thenReturn(new RoleChangeImpact(Set.of(), Set.of(), Set.of(), Set.of(), false, false, 0, 0));
        when(tokenService.issue(any())).thenReturn("preview-token");
        when(timeMapper.toLocalDateTime(any(Instant.class))).thenReturn(LocalDateTime.of(2026, 8, 25, 12, 0));

        var from = new RoleAuthorizationChangeFrom();
        from.setExpectedVersion(1L);
        from.setAuthorityLevel(1);
        from.setPermissionCodes(Set.of());
        from.setGrantablePermissionCodes(Set.of());

        var result = service.preview(roleId, from);

        assertEquals("preview-token", result.getPreviewToken());
        verify(permissionMapper, never()).selectList(any());
    }

    @Test
    void previewShouldRejectBuiltinRoleAuthorizationChange() {
        var roleId = UUID.randomUUID();
        var role = role(roleId, 1L);
        role.setRoleKind("SYSTEM_ADMIN");
        role.setSystemManaged(true);
        when(securityContextAccessor.currentUserId()).thenReturn(UUID.randomUUID());
        when(roleMapper.selectById(roleId)).thenReturn(role);

        var from = new RoleAuthorizationChangeFrom();
        from.setExpectedVersion(1L);
        from.setAuthorityLevel(1);
        from.setPermissionCodes(Set.of());
        from.setGrantablePermissionCodes(Set.of());

        assertThrows(BuiltinDataException.class, () -> service.preview(roleId, from));
    }

    @Test
    void previewShouldRejectBusinessRoleAuthorityLevelAbove999() {
        var roleId = UUID.randomUUID();
        var role = role(roleId, 1L);
        role.setRoleKind("BUSINESS");
        role.setSystemManaged(false);
        when(securityContextAccessor.currentUserId()).thenReturn(UUID.randomUUID());
        when(roleMapper.selectById(roleId)).thenReturn(role);

        var from = new RoleAuthorizationChangeFrom();
        from.setExpectedVersion(1L);
        from.setAuthorityLevel(1000);
        from.setPermissionCodes(Set.of());
        from.setGrantablePermissionCodes(Set.of());

        assertThrows(DataException.class, () -> service.preview(roleId, from));
    }

    private static SecurityRole role(UUID id, long version) {
        var role = new SecurityRole();
        role.setId(id);
        role.setState("ACTIVE");
        role.setRoleKind("BUSINESS");
        role.setAuthorityLevel(1);
        role.setVersion(version);
        return role;
    }

    private static Permission permission(UUID id, String code) {
        var permission = new Permission();
        permission.setId(id);
        permission.setCode(code);
        permission.setState("ACTIVE");
        return permission;
    }
}
