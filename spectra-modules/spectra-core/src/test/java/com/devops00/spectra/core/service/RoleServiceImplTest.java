/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.service;

import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.authorization.entity.SecurityRole;
import com.devops00.spectra.core.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.user.javabean.from.RoleFrom;
import com.devops00.spectra.core.user.service.impl.RoleServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Field;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 目标角色目录服务测试。
 */
@ExtendWith(MockitoExtension.class)
class RoleServiceImplTest {

    @Mock
    private SecurityRoleMapper securityRoleMapper;

    @Mock
    private RoleAssignmentMapper roleAssignmentMapper;

    private RoleServiceImpl service;

    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        service = new RoleServiceImpl(roleAssignmentMapper);
        Field baseMapper = CrudRepository.class.getDeclaredField("baseMapper");
        baseMapper.setAccessible(true);
        baseMapper.set(service, securityRoleMapper);
    }

    @Test
    void createdShouldWriteTargetRoleWithStableCodeAndDefaults() {
        when(securityRoleMapper.selectCount(any())).thenReturn(0L);
        when(securityRoleMapper.insert(any(SecurityRole.class))).thenReturn(1);

        var from = new RoleFrom(null, "业务管理员", "ROLE_BUSINESS_ADMIN", true, "业务角色");
        service.created(from);

        ArgumentCaptor<SecurityRole> captor = ArgumentCaptor.forClass(SecurityRole.class);
        verify(securityRoleMapper).insert(captor.capture());
        var role = captor.getValue();
        assertEquals("ROLE_BUSINESS_ADMIN", role.getCode());
        assertEquals("业务管理员", role.getName());
        assertEquals("ACTIVE", role.getState());
        assertEquals("BUSINESS", role.getRoleKind());
        assertEquals(1, role.getAuthorityLevel());
        assertEquals(false, role.getSystemManaged());
        assertEquals("业务角色", role.getRemark());
    }

    @Test
    void createdShouldRejectDuplicateTargetCode() {
        when(securityRoleMapper.selectCount(any())).thenReturn(1L);

        assertThrows(DataException.class, () -> service.created(new RoleFrom(null, "重复角色", "ROLE_DUPLICATE", true, null)));
        verify(securityRoleMapper, never()).insert((SecurityRole) any());
    }

    @Test
    void deleteShouldDisableUnassignedTargetRole() {
        var roleId = UUID.randomUUID();
        var role = targetRole(roleId, "ROLE_BUSINESS", false);
        when(securityRoleMapper.selectById(roleId)).thenReturn(role);
        when(roleAssignmentMapper.selectCount(any())).thenReturn(0L);
        when(securityRoleMapper.updateById(any(SecurityRole.class))).thenReturn(1);

        service.deleteById(roleId);

        assertEquals("DISABLED", role.getState());
        verify(securityRoleMapper).updateById(role);
    }

    @Test
    void modifyShouldRejectSystemManagedRole() {
        var roleId = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(targetRole(roleId, "ROLE_ROOT", true));

        var from = new RoleFrom(roleId, "Root", "ROLE_ROOT", true, null);
        assertThrows(BuiltinDataException.class, () -> service.modify(from));
        verify(securityRoleMapper, never()).updateById((SecurityRole) any());
    }

    @Test
    void deleteShouldRejectActiveAssignments() {
        var roleId = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(targetRole(roleId, "ROLE_BUSINESS", false));
        when(roleAssignmentMapper.selectCount(any())).thenReturn(1L);

        assertThrows(DataException.class, () -> service.deleteById(roleId));
        verify(securityRoleMapper, never()).updateById((SecurityRole) any());
    }

    private static SecurityRole targetRole(UUID id, String code, boolean systemManaged) {
        var role = new SecurityRole();
        role.setId(id);
        role.setCode(code);
        role.setName(code);
        role.setState("ACTIVE");
        role.setSystemManaged(systemManaged);
        return role;
    }
}
