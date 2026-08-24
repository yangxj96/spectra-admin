/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.repository.CrudRepository;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.user.javabean.from.RoleFrom;
import com.devops00.spectra.core.user.javabean.from.RolePageFrom;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

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

        var from = new RoleFrom(null, "业务管理员", "ROLE_BUSINESS_ADMIN", "业务角色");
        var result = service.save(from);

        ArgumentCaptor<SecurityRole> captor = ArgumentCaptor.forClass(SecurityRole.class);
        verify(securityRoleMapper).insert(captor.capture());
        var role = captor.getValue();
        assertEquals("ROLE_BUSINESS_ADMIN", role.getCode());
        assertEquals("业务管理员", role.getName());
        assertEquals("ROLE_BUSINESS_ADMIN", result.getCode());
        assertEquals("业务管理员", result.getName());
        assertEquals("ACTIVE", role.getState());
        assertEquals("BUSINESS", role.getRoleKind());
        assertEquals(1, role.getAuthorityLevel());
        assertEquals(false, role.getSystemManaged());
        assertEquals("业务角色", role.getRemark());
    }

    @Test
    void createdShouldRejectDuplicateTargetCode() {
        when(securityRoleMapper.selectCount(any())).thenReturn(0L, 1L);

        assertThrows(DataException.class, () -> service.save(new RoleFrom(null, "重复角色", "ROLE_DUPLICATE", null)));
        verify(securityRoleMapper, never()).insert((SecurityRole) any());
    }

    @Test
    void createdShouldRejectDuplicateTargetName() {
        when(securityRoleMapper.selectCount(any())).thenReturn(1L);

        assertThrows(DataException.class, () -> service.save(new RoleFrom(null, "重复角色", "ROLE_UNIQUE", null)));
        verify(securityRoleMapper, never()).insert((SecurityRole) any());
    }

    @Test
    void createdShouldGenerateShortCodeWhenCodeIsMissing() {
        when(securityRoleMapper.selectCount(any())).thenReturn(0L);
        when(securityRoleMapper.insert(any(SecurityRole.class))).thenReturn(1);

        service.save(new RoleFrom(null, "自动编码角色", null, null));

        ArgumentCaptor<SecurityRole> captor = ArgumentCaptor.forClass(SecurityRole.class);
        verify(securityRoleMapper).insert(captor.capture());
        var code = captor.getValue().getCode();
        assertTrue(code.matches("ROLE_[A-F0-9]{8}"));
    }

    @Test
    void disableShouldDisableUnassignedTargetRole() {
        var roleId = UUID.randomUUID();
        var role = targetRole(roleId, "ROLE_BUSINESS", false);
        when(securityRoleMapper.selectById(roleId)).thenReturn(role);
        when(roleAssignmentMapper.selectCount(any())).thenReturn(0L);
        when(securityRoleMapper.updateById(any(SecurityRole.class))).thenReturn(1);

        service.disable(roleId);

        assertEquals("DISABLED", role.getState());
        verify(securityRoleMapper).updateById(role);
    }

    @Test
    void enableShouldEnableDisabledTargetRole() {
        var roleId = UUID.randomUUID();
        var role = targetRole(roleId, "ROLE_BUSINESS", false);
        role.setState("DISABLED");
        when(securityRoleMapper.selectById(roleId)).thenReturn(role);
        when(securityRoleMapper.updateById(any(SecurityRole.class))).thenReturn(1);

        service.enable(roleId);

        assertEquals("ACTIVE", role.getState());
        verify(securityRoleMapper).updateById(role);
    }

    @Test
    void detailShouldReadTargetRole() {
        var roleId = UUID.randomUUID();
        var role = targetRole(roleId, "ROLE_BUSINESS", false);
        role.setName("业务管理员");
        when(securityRoleMapper.selectById(roleId)).thenReturn(role);

        var result = service.detail(roleId);

        assertEquals(roleId, result.getId());
        assertEquals("业务管理员", result.getName());
        assertEquals("ROLE_BUSINESS", result.getCode());
    }

    @Test
    void deleteShouldSoftDeleteUnassignedTargetRole() {
        var roleId = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(targetRole(roleId, "ROLE_BUSINESS", false));
        when(roleAssignmentMapper.selectCount(any())).thenReturn(0L);
        when(securityRoleMapper.deleteById(roleId)).thenReturn(1);

        service.deleteById(roleId);

        verify(securityRoleMapper).deleteById(roleId);
    }

    @Test
    void modifyShouldRejectSystemManagedRole() {
        var roleId = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(targetRole(roleId, "ROLE_ROOT", true));

        var from = new RoleFrom(roleId, "Root", "ROLE_ROOT", null);
        assertThrows(BuiltinDataException.class, () -> service.save(from));
        verify(securityRoleMapper, never()).updateById((SecurityRole) any());
    }

    @Test
    void modifyShouldRejectProtectedRoleKindEvenWhenNotMarkedSystemManaged() {
        var roleId = UUID.randomUUID();
        var role = targetRole(roleId, "ROLE_ADMIN_SYSTEM", false);
        role.setRoleKind("SYSTEM_ADMIN");
        when(securityRoleMapper.selectById(roleId)).thenReturn(role);

        var from = new RoleFrom(roleId, "系统管理员", "ROLE_ADMIN_SYSTEM", null);
        assertThrows(BuiltinDataException.class, () -> service.save(from));
        verify(securityRoleMapper, never()).updateById((SecurityRole) any());
    }

    @Test
    void modifyShouldRejectDuplicateTargetName() {
        var roleId = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(targetRole(roleId, "ROLE_BUSINESS", false));
        when(securityRoleMapper.selectCount(any())).thenReturn(1L);

        var from = new RoleFrom(roleId, "重复角色", "ROLE_BUSINESS", null);
        assertThrows(DataException.class, () -> service.save(from));
        verify(securityRoleMapper, never()).updateById((SecurityRole) any());
    }

    @Test
    void modifyShouldKeepRoleCodeImmutable() {
        var roleId = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(targetRole(roleId, "ROLE_BUSINESS", false));

        var from = new RoleFrom(roleId, "业务角色", "ROLE_RENAMED", null);
        assertThrows(DataException.class, () -> service.save(from));
        verify(securityRoleMapper, never()).updateById((SecurityRole) any());
    }

    @Test
    void pageShouldKeepRequestedPageAndPageSize() {
        when(securityRoleMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> invocation.getArgument(0));

        var pageFrom = new PageFrom();
        pageFrom.setPageNum(2L);
        pageFrom.setPageSize(15L);
        var result = service.page(pageFrom, new RolePageFrom());

        assertEquals(2L, result.getCurrent());
        assertEquals(15L, result.getSize());

        ArgumentCaptor<Page<SecurityRole>> pageCaptor = ArgumentCaptor.forClass(Page.class);
        verify(securityRoleMapper).selectPage(pageCaptor.capture(), any());
        assertEquals(2L, pageCaptor.getValue().getCurrent());
        assertEquals(15L, pageCaptor.getValue().getSize());
    }

    @Test
    void deleteShouldRejectActiveAssignments() {
        var roleId = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(targetRole(roleId, "ROLE_BUSINESS", false));
        when(roleAssignmentMapper.selectCount(any())).thenReturn(1L);

        assertThrows(DataException.class, () -> service.deleteById(roleId));
        verify(securityRoleMapper, never()).updateById((SecurityRole) any());
        verify(securityRoleMapper, never()).deleteById(roleId);
    }

    private static SecurityRole targetRole(UUID id, String code, boolean systemManaged) {
        var role = new SecurityRole();
        role.setId(id);
        role.setCode(code);
        role.setName(code);
        role.setState("ACTIVE");
        role.setRoleKind("BUSINESS");
        role.setSystemManaged(systemManaged);
        return role;
    }
}
