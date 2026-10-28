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
package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.common.exception.BusinessConflictException;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.core.security.authorization.service.SecurityMenuGrantQuery;
import com.devops00.spectra.core.security.authorization.service.SecurityRoleMenuStore;
import com.devops00.spectra.core.system.javabean.converter.MenuConverter;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.mapper.MenuMapper;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MenuDeletePolicyTest {

    private final MenuMapper menuMapper = mock(MenuMapper.class);
    private final MenuConverter menuConverter = mock(MenuConverter.class);
    private final AuthorizationSnapshotProvider snapshotProvider = mock(AuthorizationSnapshotProvider.class);
    private final SecurityMenuGrantQuery securityMenuGrantQuery = mock(SecurityMenuGrantQuery.class);
    private final SecurityRoleMenuStore securityRoleMenuStore = mock(SecurityRoleMenuStore.class);

    @Test
    void directoryWithActiveChildrenIsRejected() {
        var menu = menu();
        when(menuMapper.selectOne(any())).thenReturn(menu);
        when(menuMapper.selectCount(any())).thenReturn(1L);

        assertThrows(BusinessConflictException.class, () -> service().deleteById(menu.getId()));

        verify(securityRoleMenuStore, never()).deleteByMenuId(any());
        verify(menuMapper, never()).deleteById(any(Menu.class));
    }

    @Test
    void leafMenuRemovesRoleRelationsInSameTransaction() {
        var menu = menu();
        when(menuMapper.selectOne(any())).thenReturn(menu);
        when(menuMapper.selectCount(any())).thenReturn(0L);
        when(menuMapper.deleteById(any(Menu.class))).thenReturn(1);

        assertDoesNotThrow(() -> service().deleteById(menu.getId()));

        verify(securityRoleMenuStore).deleteByMenuId(any());
        verify(menuMapper).deleteById(menu);
    }

    private MenuServiceImpl service() {
        var service = new MenuServiceImpl(menuMapper, menuConverter, snapshotProvider, securityMenuGrantQuery,
                securityRoleMenuStore);
        ReflectionTestUtils.setField(service, "baseMapper", menuMapper);
        return service;
    }

    private static Menu menu() {
        var menu = new Menu();
        menu.setId(UUID.randomUUID());
        return menu;
    }
}
