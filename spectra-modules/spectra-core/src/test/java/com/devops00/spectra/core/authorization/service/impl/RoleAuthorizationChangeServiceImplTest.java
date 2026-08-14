/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.authorization.service.impl;

import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.authorization.entity.Permission;
import com.devops00.spectra.core.authorization.entity.RoleGrantablePermission;
import com.devops00.spectra.core.authorization.entity.RolePermission;
import com.devops00.spectra.core.authorization.entity.SecurityRole;
import com.devops00.spectra.core.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.authorization.javabean.vo.RoleAuthorizationStateVO;
import com.devops00.spectra.core.authorization.service.GrantBoundaryService;
import com.devops00.spectra.core.authorization.service.RoleChangeImpactAnalyzer;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.change.AuthorizationChangeTokenService;
import com.devops00.spectra.security.base.change.AuthorizationEpochGuard;
import com.devops00.spectra.security.base.change.HighRiskApprovalGate;
import com.devops00.spectra.security.base.change.SecurityChangeExecutor;
import com.devops00.spectra.security.base.change.SecuritySessionRevocationPort;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.root.RootAuthorizationPolicy;
import com.devops00.spectra.core.authorization.AuthorizationSnapshotLoader;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
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
    void currentShouldRejectMissingOrDisabledTargetRole() {
        var roleId = UUID.randomUUID();
        when(roleMapper.selectById(roleId)).thenReturn(null);

        assertThrows(DataNotExistException.class, () -> service.current(roleId));
    }

    private static SecurityRole role(UUID id, long version) {
        var role = new SecurityRole();
        role.setId(id);
        role.setState("ACTIVE");
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
