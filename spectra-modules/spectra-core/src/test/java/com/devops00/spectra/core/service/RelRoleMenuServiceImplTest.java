/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.core.service;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.security.authorization.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.entity.SecurityRoleMenu;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMenuMapper;
import com.devops00.spectra.core.system.javabean.converter.MenuConverter;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.javabean.enums.MenuType;
import com.devops00.spectra.core.system.service.MenuService;
import com.devops00.spectra.core.user.javabean.from.RoleMenuFrom;
import com.devops00.spectra.core.user.service.impl.RelRoleMenuServiceImpl;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 角色菜单关联服务测试
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/30
 */
@ExtendWith(MockitoExtension.class)
@SuppressWarnings({"unchecked", "rawtypes"})
class RelRoleMenuServiceImplTest {

    @Mock
    private MenuConverter menuConverter;

    @Mock
    private SecurityRoleMenuMapper securityRoleMenuMapper;

    @Mock
    private SecurityRoleMapper securityRoleMapper;

    @Mock
    private MenuService menuService;

    private RelRoleMenuServiceImpl service;

    private Validator validator;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), SecurityRoleMenu.class);
        service = new RelRoleMenuServiceImpl(menuConverter, securityRoleMenuMapper, securityRoleMapper, menuService);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void grantShouldDeleteRemovedMenuAndInsertNewMenu() {
        var roleId = UUID.randomUUID();
        var menuA = UUID.randomUUID();
        var menuB = UUID.randomUUID();
        var menuC = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(activeRole(roleId));
        when(securityRoleMenuMapper.selectList(any())).thenReturn(List.of(relation(roleId, menuA), relation(roleId, menuB)));
        when(menuService.listByIds(anyCollection())).thenReturn(List.of(menu(menuB, MenuType.MENU), menu(menuC, MenuType.MENU)));

        service.grant(roleId, new RoleMenuFrom(roleId, List.of(menuB, menuC)));

        var deleteCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(securityRoleMenuMapper).delete(deleteCaptor.capture());
        var deleteWrapper = deleteCaptor.getValue();
        assertTrue(deleteWrapper.getTargetSql().contains("role_id"));
        assertTrue(deleteWrapper.getTargetSql().contains("menu_id"));
        assertTrue(deleteWrapper.getParamNameValuePairs().containsValue(roleId));
        assertTrue(deleteWrapper.getParamNameValuePairs().containsValue(menuA));

        ArgumentCaptor<SecurityRoleMenu> insertCaptor = ArgumentCaptor.forClass(SecurityRoleMenu.class);
        verify(securityRoleMenuMapper).insert(insertCaptor.capture());
        assertEquals(menuC, insertCaptor.getValue().getMenuId());
    }

    @Test
    void grantShouldAllowClearingAllMenus() {
        var roleId = UUID.randomUUID();
        var menuId = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(activeRole(roleId));
        when(securityRoleMenuMapper.selectList(any())).thenReturn(List.of(relation(roleId, menuId)));

        service.grant(roleId, new RoleMenuFrom(roleId, List.of()));

        verify(securityRoleMenuMapper).delete(org.mockito.ArgumentMatchers.<LambdaQueryWrapper<SecurityRoleMenu>>any());
        verify(securityRoleMenuMapper, never()).insert((SecurityRoleMenu) any());
    }

    @Test
    void roleMenuFormShouldAllowEmptyListAndRejectNullElement() {
        var roleId = UUID.randomUUID();

        assertTrue(validator.validate(new RoleMenuFrom(roleId, List.of())).isEmpty());

        var violations = validator.validate(new RoleMenuFrom(roleId, Collections.singletonList(null)));
        assertEquals(1, violations.size());
        assertEquals("菜单ID不能为null", violations.iterator().next().getMessage());
    }

    @Test
    void grantShouldRejectDifferentRoleId() {
        assertThrows(DataException.class, () -> service.grant(UUID.randomUUID(), new RoleMenuFrom(UUID.randomUUID(), List.of())));
    }

    @Test
    void grantShouldIgnoreDirectoryRelations() {
        var roleId = UUID.randomUUID();
        var directoryId = UUID.randomUUID();
        var menuId = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(activeRole(roleId));
        when(securityRoleMenuMapper.selectList(any())).thenReturn(List.of());
        when(menuService.listByIds(anyCollection())).thenReturn(List.of(menu(directoryId, MenuType.DIRECTORY), menu(menuId, MenuType.MENU)));

        service.grant(roleId, new RoleMenuFrom(roleId, List.of(directoryId, menuId)));

        ArgumentCaptor<SecurityRoleMenu> insertCaptor = ArgumentCaptor.forClass(SecurityRoleMenu.class);
        verify(securityRoleMenuMapper).insert(insertCaptor.capture());
        assertEquals(menuId, insertCaptor.getValue().getMenuId());
    }

    @Test
    void grantShouldRejectUnknownMenu() {
        var roleId = UUID.randomUUID();
        var knownId = UUID.randomUUID();
        var unknownId = UUID.randomUUID();
        when(securityRoleMapper.selectById(roleId)).thenReturn(activeRole(roleId));
        when(menuService.listByIds(anyCollection())).thenReturn(List.of(menu(knownId, MenuType.MENU)));

        assertThrows(DataNotExistException.class, () -> service.grant(roleId, new RoleMenuFrom(roleId, List.of(knownId, unknownId))));
    }

    @Test
    void grantShouldRejectDeletedMenu() {
        var roleId = UUID.randomUUID();
        var deletedMenu = menu(UUID.randomUUID(), MenuType.MENU);
        deletedMenu.setDeleted(Instant.now());
        when(securityRoleMapper.selectById(roleId)).thenReturn(activeRole(roleId));
        when(menuService.listByIds(anyCollection())).thenReturn(List.of(deletedMenu));

        assertThrows(DataNotExistException.class, () -> service.grant(roleId, new RoleMenuFrom(roleId, List.of(deletedMenu.getId()))));
    }

    private static Menu menu(UUID id, MenuType type) {
        var menu = new Menu();
        menu.setId(id);
        menu.setMenuType(type);
        return menu;
    }

    private static SecurityRole activeRole(UUID id) {
        var role = new SecurityRole();
        role.setId(id);
        role.setState("ACTIVE");
        role.setRoleKind("BUSINESS");
        return role;
    }

    private static SecurityRoleMenu relation(UUID roleId, UUID menuId) {
        var relation = new SecurityRoleMenu();
        relation.setRoleId(roleId);
        relation.setMenuId(menuId);
        return relation;
    }
}
