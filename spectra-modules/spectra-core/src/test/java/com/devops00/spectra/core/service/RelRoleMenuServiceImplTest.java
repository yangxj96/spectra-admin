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
import com.devops00.spectra.core.system.javabean.converter.MenuConverter;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.javabean.enums.MenuType;
import com.devops00.spectra.core.system.service.MenuService;
import com.devops00.spectra.core.user.javabean.entity.RelRoleMenu;
import com.devops00.spectra.core.user.javabean.from.RoleMenuFrom;
import com.devops00.spectra.core.user.mapper.RelRoleMenuMapper;
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

import java.util.Collections;
import java.util.List;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyList;
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
    private RelRoleMenuMapper relRoleMenuMapper;

    @Mock
    private MenuService menuService;

    private RelRoleMenuServiceImpl service;

    private Validator validator;

    @BeforeEach
    void setUp() {
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(new MybatisConfiguration(), ""), RelRoleMenu.class);
        service = new RelRoleMenuServiceImpl(menuConverter, relRoleMenuMapper, menuService);
        validator = Validation.buildDefaultValidatorFactory().getValidator();
    }

    @Test
    void grantShouldDeleteRemovedMenuAndInsertNewMenu() {
        var roleId = UUID.randomUUID();
        var menuA = UUID.randomUUID();
        var menuB = UUID.randomUUID();
        var menuC = UUID.randomUUID();
        when(relRoleMenuMapper.getByRoleId(roleId)).thenReturn(List.of(new RelRoleMenu(roleId, menuA), new RelRoleMenu(roleId, menuB)));
        when(menuService.listByIds(anyCollection())).thenReturn(List.of(menu(menuB, MenuType.MENU), menu(menuC, MenuType.MENU)));

        service.grant(roleId, new RoleMenuFrom(roleId, List.of(menuB, menuC)));

        var deleteCaptor = ArgumentCaptor.forClass(LambdaQueryWrapper.class);
        verify(relRoleMenuMapper).delete(deleteCaptor.capture());
        var deleteWrapper = deleteCaptor.getValue();
        assertTrue(deleteWrapper.getTargetSql().contains("role_id"));
        assertTrue(deleteWrapper.getTargetSql().contains("menu_id"));
        assertTrue(deleteWrapper.getParamNameValuePairs().containsValue(roleId));
        assertTrue(deleteWrapper.getParamNameValuePairs().containsValue(menuA));

        ArgumentCaptor<List<RelRoleMenu>> insertCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(relRoleMenuMapper).insert(insertCaptor.capture());
        assertEquals(List.of(menuC), insertCaptor.getValue().stream().map(RelRoleMenu::getMenuId).toList());
    }

    @Test
    void grantShouldAllowClearingAllMenus() {
        var roleId = UUID.randomUUID();
        var menuId = UUID.randomUUID();
        when(relRoleMenuMapper.getByRoleId(roleId)).thenReturn(List.of(new RelRoleMenu(roleId, menuId)));

        service.grant(roleId, new RoleMenuFrom(roleId, List.of()));

        verify(relRoleMenuMapper).delete(org.mockito.ArgumentMatchers.<LambdaQueryWrapper<RelRoleMenu>>any());
        verify(relRoleMenuMapper, never()).insert(anyList());
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
        when(relRoleMenuMapper.getByRoleId(roleId)).thenReturn(List.of());
        when(menuService.listByIds(anyCollection())).thenReturn(List.of(menu(directoryId, MenuType.DIRECTORY), menu(menuId, MenuType.MENU)));

        service.grant(roleId, new RoleMenuFrom(roleId, List.of(directoryId, menuId)));

        ArgumentCaptor<List<RelRoleMenu>> insertCaptor = ArgumentCaptor.forClass((Class) List.class);
        verify(relRoleMenuMapper).insert(insertCaptor.capture());
        assertEquals(List.of(menuId), insertCaptor.getValue().stream().map(RelRoleMenu::getMenuId).toList());
    }

    @Test
    void grantShouldRejectUnknownMenu() {
        var roleId = UUID.randomUUID();
        var knownId = UUID.randomUUID();
        var unknownId = UUID.randomUUID();
        when(menuService.listByIds(anyCollection())).thenReturn(List.of(menu(knownId, MenuType.MENU)));

        assertThrows(DataNotExistException.class, () -> service.grant(roleId, new RoleMenuFrom(roleId, List.of(knownId, unknownId))));
    }

    @Test
    void grantShouldRejectDeletedMenu() {
        var roleId = UUID.randomUUID();
        var deletedMenu = menu(UUID.randomUUID(), MenuType.MENU);
        deletedMenu.setDeleted(Instant.now());
        when(menuService.listByIds(anyCollection())).thenReturn(List.of(deletedMenu));

        assertThrows(DataNotExistException.class, () -> service.grant(roleId, new RoleMenuFrom(roleId, List.of(deletedMenu.getId()))));
    }

    private static Menu menu(UUID id, MenuType type) {
        var menu = new Menu();
        menu.setId(id);
        menu.setMenuType(type);
        return menu;
    }
}
