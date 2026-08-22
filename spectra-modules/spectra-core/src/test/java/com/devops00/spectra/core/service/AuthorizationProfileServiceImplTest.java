/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.service;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.security.authorization.entity.AuthorizationProfile;
import com.devops00.spectra.core.security.authorization.entity.AuthorizationProfileAssignment;
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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
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
}
