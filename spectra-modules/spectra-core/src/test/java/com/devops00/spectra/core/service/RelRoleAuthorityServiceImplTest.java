/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.service;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.authorization.entity.Permission;
import com.devops00.spectra.core.authorization.entity.RoleGrantablePermission;
import com.devops00.spectra.core.authorization.entity.RolePermission;
import com.devops00.spectra.core.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.authorization.mapper.RoleGrantablePermissionMapper;
import com.devops00.spectra.core.authorization.mapper.RolePermissionMapper;
import com.devops00.spectra.core.user.javabean.from.RoleAuthorityFrom;
import com.devops00.spectra.core.user.service.impl.RelRoleAuthorityServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 角色 Permission 目标关系读取测试。
 */
@ExtendWith(MockitoExtension.class)
class RelRoleAuthorityServiceImplTest {

    @Mock
    private PermissionMapper permissionMapper;

    @Mock
    private RolePermissionMapper rolePermissionMapper;

    @Mock
    private RoleGrantablePermissionMapper roleGrantablePermissionMapper;

    @Test
    void getShouldReadTargetRolePermissionAndReturnPermissionView() {
        var roleId = UUID.randomUUID();
        var permissionId = UUID.randomUUID();
        var row = new RolePermission();
        row.setRoleId(roleId);
        row.setPermissionId(permissionId);
        var permission = new Permission();
        permission.setId(permissionId);
        permission.setCode("user:read");
        permission.setName("用户查询");
        permission.setState("ACTIVE");
        when(rolePermissionMapper.selectList(any())).thenReturn(List.of(row));
        when(permissionMapper.selectList(any())).thenReturn(List.of(permission));

        var result = new RelRoleAuthorityServiceImpl(permissionMapper, rolePermissionMapper,
                roleGrantablePermissionMapper).get(roleId);

        assertEquals(1, result.size());
        assertEquals(permissionId, result.getFirst().getId());
        assertEquals("user:read", result.getFirst().getCode());
        assertEquals(null, result.getFirst().getPid());
    }

    @Test
    void grantShouldKeepLegacyWriteEntryFrozen() {
        var service = new RelRoleAuthorityServiceImpl(permissionMapper, rolePermissionMapper,
                roleGrantablePermissionMapper);

        assertThrows(DataException.class, () -> service.grant(UUID.randomUUID(), new RoleAuthorityFrom()));
    }

    @Test
    void revokeShouldRemoveTargetPermissionAndGrantableRelations() {
        var roleId = UUID.randomUUID();

        new RelRoleAuthorityServiceImpl(permissionMapper, rolePermissionMapper,
                roleGrantablePermissionMapper).revoke(roleId);

        verify(rolePermissionMapper).delete(any());
        verify(roleGrantablePermissionMapper).delete(any());
    }
}
