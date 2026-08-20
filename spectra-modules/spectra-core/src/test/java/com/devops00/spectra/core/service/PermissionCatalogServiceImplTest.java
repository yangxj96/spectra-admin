/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.service;

import com.devops00.spectra.core.security.authorization.entity.Permission;
import com.devops00.spectra.core.security.authorization.mapper.PermissionMapper;
import com.devops00.spectra.core.security.authorization.service.impl.PermissionCatalogServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Permission Catalog 展示适配测试。
 */
@ExtendWith(MockitoExtension.class)
class PermissionCatalogServiceImplTest {

    @Mock
    private PermissionMapper permissionMapper;

    @Test
    void treeShouldGroupActivePermissionsByResourceWithoutLegacyAuthorityRows() {
        var userRead = permission("user:read", "用户查询", "user", "read", "ACTIVE");
        var userUpdate = permission("user:update-profile", "用户资料修改", "user", "update-profile", "ACTIVE");
        var old = permission("legacy:authority", "旧权限", "legacy", "authority", "DEPRECATED");
        when(permissionMapper.selectList(any())).thenReturn(List.of(userUpdate, old, userRead));

        var result = new PermissionCatalogServiceImpl(permissionMapper).tree();

        assertEquals(1, result.size());
        assertEquals("user", result.getFirst().getCode());
        assertEquals(List.of("user:read", "user:update-profile"),
                result.getFirst().getChildren().stream().map(child -> child.getCode()).toList());
        assertEquals(result.getFirst().getId(), result.getFirst().getChildren().getFirst().getPid());
        assertEquals(List.of(), result.getFirst().getChildren().getFirst().getAllowedScopeModes());
    }

    @Test
    void treeShouldDeriveResourceWhenCatalogRowDoesNotHaveResourceCode() {
        var permission = permission("menu:read", "菜单查询", null, null, "ACTIVE");
        when(permissionMapper.selectList(any())).thenReturn(List.of(permission));

        var result = new PermissionCatalogServiceImpl(permissionMapper).tree();

        assertEquals("menu", result.getFirst().getCode());
        assertEquals(permission.getId(), result.getFirst().getChildren().getFirst().getId());
    }

    @Test
    void treeShouldExposeAllowedScopeModesFromPermissionCatalog() {
        var permission = permission("document:read", "文档查询", "document", "read", "ACTIVE");
        permission.setAllowedScopeModes("RULES, SELF, RULES");
        when(permissionMapper.selectList(any())).thenReturn(List.of(permission));

        var result = new PermissionCatalogServiceImpl(permissionMapper).tree();

        assertEquals(List.of("RULES", "SELF"), result.getFirst().getChildren().getFirst().getAllowedScopeModes());
    }

    private static Permission permission(String code, String name, String resourceCode, String actionCode, String state) {
        var permission = new Permission();
        permission.setId(UUID.randomUUID());
        permission.setCode(code);
        permission.setName(name);
        permission.setResourceCode(resourceCode);
        permission.setActionCode(actionCode);
        permission.setState(state);
        return permission;
    }
}
