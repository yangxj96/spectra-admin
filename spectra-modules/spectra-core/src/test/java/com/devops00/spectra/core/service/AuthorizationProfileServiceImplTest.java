/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.service;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.security.authorization.entity.AuthorizationProfile;
import com.devops00.spectra.core.security.authorization.entity.AuthorizationProfileAssignment;
import com.devops00.spectra.core.security.authorization.entity.RolePermission;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationProfileAssignmentFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationProfileBoundaryFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationProfileSaveFrom;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileBoundaryMapper;
import com.devops00.spectra.core.security.authorization.mapper.AuthorizationProfileMapper;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.service.impl.AuthorizationProfileServiceImpl;
import com.devops00.spectra.core.system.service.DepartmentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 授权方案服务测试。
 */
@ExtendWith(MockitoExtension.class)
class AuthorizationProfileServiceImplTest {

    @Mock
    private AuthorizationProfileMapper profileMapper;

    @Mock
    private AuthorizationProfileAssignmentMapper assignmentMapper;

    @Mock
    private AuthorizationProfileBoundaryMapper boundaryMapper;

    @Mock
    private SecurityRoleMapper roleMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Mock
    private RoleGrantablePermissionMapper roleGrantablePermissionMapper;

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private DepartmentService departmentService;

    private AuthorizationProfileServiceImpl service;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        service = new AuthorizationProfileServiceImpl(
                assignmentMapper,
                boundaryMapper,
                roleMapper,
                rolePermissionMapper,
                roleGrantablePermissionMapper,
                permissionMapper,
                departmentService);
        Field baseMapper = CrudRepository.class.getDeclaredField("baseMapper");
        baseMapper.setAccessible(true);
        baseMapper.set(service, profileMapper);
    }

    @Test
    void deleteShouldRemoveProfileTemplateAndItsDetails() {
        var profileId = UUID.randomUUID();
        var assignment = new AuthorizationProfileAssignment();
        assignment.setId(UUID.randomUUID());
        assignment.setProfileId(profileId);
        var profile = new AuthorizationProfile();
        profile.setId(profileId);
        profile.setCode("PROFILE_DEMO");
        when(profileMapper.selectById(profileId)).thenReturn(profile);
        when(assignmentMapper.selectList(any())).thenReturn(List.of(assignment));
        when(profileMapper.deleteById(profileId)).thenReturn(1);

        service.deleteById(profileId);

        verify(boundaryMapper).delete(any());
        verify(assignmentMapper).delete(any());
        verify(profileMapper).deleteById(profileId);
    }

    @Test
    void deleteShouldRejectMissingProfile() {
        var profileId = UUID.randomUUID();
        when(profileMapper.selectById(profileId)).thenReturn(null);

        assertThrows(DataNotExistException.class, () -> service.deleteById(profileId));

        verify(profileMapper, never()).deleteById(profileId);
        verify(assignmentMapper, never()).selectList(any());
    }

    @Test
    void createdShouldAllowRoleWithoutPermissionsAndBoundaries() {
        var roleId = UUID.randomUUID();
        var profileId = UUID.randomUUID();
        var assignmentId = UUID.randomUUID();
        var role = role(roleId, "ROLE_BASIC", "普通角色", 1L);
        when(roleMapper.selectOne(any())).thenReturn(role);
        when(rolePermissionMapper.selectList(any())).thenReturn(List.of());
        when(roleGrantablePermissionMapper.selectList(any())).thenReturn(List.of());
        when(profileMapper.selectOne(any(), eq(true))).thenReturn(null);
        when(assignmentMapper.selectList(any())).thenReturn(List.of());
        doAnswer(invocation -> {
            ((AuthorizationProfile) invocation.getArgument(0)).setId(profileId);
            return 1;
        }).when(profileMapper).insert(any(AuthorizationProfile.class));
        doAnswer(invocation -> {
            ((AuthorizationProfileAssignment) invocation.getArgument(0)).setId(assignmentId);
            return 1;
        }).when(assignmentMapper).insert((AuthorizationProfileAssignment) any());

        assertDoesNotThrow(() -> service.created(saveFrom(role.getCode(), role.getVersion(), List.of())));

        verifyNoInteractions(boundaryMapper);
    }

    @Test
    void createdShouldUseRoleNameWhenPermissionBoundaryIsRequired() {
        var roleId = UUID.randomUUID();
        var role = role(roleId, "ROLE_BASIC", "普通角色", 1L);
        var rolePermission = new RolePermission();
        rolePermission.setRoleId(roleId);
        rolePermission.setPermissionId(UUID.randomUUID());
        when(roleMapper.selectOne(any())).thenReturn(role);
        when(rolePermissionMapper.selectList(any())).thenReturn(List.of(rolePermission));
        when(roleGrantablePermissionMapper.selectList(any())).thenReturn(List.of());

        var exception = assertThrows(DataException.class,
                () -> service.created(saveFrom(role.getCode(), role.getVersion(), List.of())));

        assertEquals("角色「普通角色」至少需要一个权限访问范围", exception.getMessage());
    }

    private static AuthorizationProfileSaveFrom saveFrom(String roleCode, long roleVersion,
                                                          List<AuthorizationProfileBoundaryFrom> boundaries) {
        var assignment = new AuthorizationProfileAssignmentFrom();
        assignment.setRoleCode(roleCode);
        assignment.setRoleVersion(roleVersion);
        assignment.setBoundaries(boundaries);
        var params = new AuthorizationProfileSaveFrom();
        params.setCode("PROFILE_BASIC");
        params.setName("基础授权方案");
        params.setAssignments(List.of(assignment));
        return params;
    }

    private static SecurityRole role(UUID id, String code, String name, long version) {
        var role = new SecurityRole();
        role.setId(id);
        role.setCode(code);
        role.setName(name);
        role.setState("ACTIVE");
        role.setRoleKind("BUSINESS");
        role.setVersion(version);
        return role;
    }
}
