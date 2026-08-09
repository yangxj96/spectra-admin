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

import com.devops00.spectra.core.system.javabean.converter.MenuConverter;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.javabean.enums.MenuType;
import com.devops00.spectra.core.system.javabean.from.MenuSaveFrom;
import com.devops00.spectra.core.system.javabean.vo.MenuTreeVO;
import com.devops00.spectra.core.system.mapper.MenuMapper;
import com.devops00.spectra.core.system.service.impl.MenuServiceImpl;
import com.devops00.spectra.core.user.javabean.entity.RelRoleMenu;
import com.devops00.spectra.core.user.javabean.entity.Role;
import com.devops00.spectra.core.user.mapper.RelRoleMenuMapper;
import com.devops00.spectra.core.user.service.RelUserRoleService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/// 菜单服务测试
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/30
@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private MenuMapper menuMapper;

    @Mock
    private MenuConverter menuConverter;

    @Mock
    private RelRoleMenuMapper relRoleMenuMapper;

    @Mock
    private RelUserRoleService relUserRoleService;

    private MenuServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MenuServiceImpl(menuMapper, menuConverter, relRoleMenuMapper, relUserRoleService);
    }

    @Test
    void currentShouldMergeRolesIncludeAncestorsPruneEmptyDirectoriesAndSort() {
        var userId = UUID.randomUUID();
        var activeRoleA = role(true);
        var activeRoleB = role(true);
        var inactiveRole = role(false);
        var system = menu(null, MenuType.DIRECTORY, null, 2);
        var emptyRoot = menu(null, MenuType.DIRECTORY, null, 1);
        var workflow = menu(system.getId(), MenuType.DIRECTORY, null, 2);
        var workflowMenu = menu(workflow.getId(), MenuType.MENU, "SystemWorkflow", 2);
        var userMenu = menu(system.getId(), MenuType.MENU, "SystemUser", 1);

        when(relUserRoleService.getRoles(userId)).thenReturn(List.of(activeRoleA, inactiveRole, activeRoleB));
        when(relRoleMenuMapper.selectList(any())).thenReturn(List.of(new RelRoleMenu(activeRoleA.getId(), workflowMenu.getId()),
                new RelRoleMenu(activeRoleB.getId(), workflowMenu.getId()), new RelRoleMenu(activeRoleB.getId(), userMenu.getId())));
        when(menuMapper.selectList(any())).thenReturn(List.of(system, emptyRoot, workflow, workflowMenu, userMenu));
        when(menuConverter.toTreeVOList(any())).thenAnswer(invocation -> {
            List<Menu> menus = invocation.getArgument(0);
            return menus.stream().map(MenuServiceImplTest::toTreeVO).toList();
        });

        var result = service.current(userId);

        assertEquals(List.of(system.getId()), result.stream().map(MenuTreeVO::getId).toList());
        assertEquals(List.of(userMenu.getId(), workflow.getId()), result.getFirst().getChildren().stream().map(MenuTreeVO::getId).toList());
        assertEquals(List.of(workflowMenu.getId()), result.getFirst().getChildren().get(1).getChildren().stream().map(MenuTreeVO::getId).toList());
        verify(relRoleMenuMapper).selectList(argThat(wrapper -> wrapper.getSqlSegment().contains("deleted")));
        verify(menuMapper).selectList(argThat(wrapper -> wrapper.getSqlSegment().contains("deleted")));
    }

    @Test
    void currentShouldReturnEmptyWhenUserHasNoEnabledRole() {
        var userId = UUID.randomUUID();
        when(relUserRoleService.getRoles(userId)).thenReturn(List.of(role(false)));

        var result = service.current(userId);

        assertEquals(List.of(), result);
        verify(relRoleMenuMapper, never()).selectList(any());
        verify(menuMapper, never()).selectList(any());
    }

    @Test
    void createdShouldRejectDirectoryWithRouteName() {
        var from = new MenuSaveFrom();
        from.setMenuType(MenuType.DIRECTORY);
        from.setRouteName("System");

        assertThrows(DataException.class, () -> service.created(from));
    }

    @Test
    void createdShouldRejectMenuWithoutRouteName() {
        var from = new MenuSaveFrom();
        from.setMenuType(MenuType.MENU);

        assertThrows(DataException.class, () -> service.created(from));
    }

    @Test
    void createdShouldRejectMenuParent() {
        var parent = menu(null, MenuType.MENU, "SystemUser", 0);
        var from = new MenuSaveFrom();
        from.setPid(parent.getId());
        from.setMenuType(MenuType.DIRECTORY);
        when(menuMapper.selectOne(any())).thenReturn(parent);

        assertThrows(DataException.class, () -> service.created(from));
    }

    @Test
    void createdShouldRejectDeletedParent() {
        var parent = menu(null, MenuType.DIRECTORY, null, 0);
        parent.setDeleted(Instant.now());
        var from = new MenuSaveFrom();
        from.setPid(parent.getId());
        from.setMenuType(MenuType.DIRECTORY);
        when(menuMapper.selectOne(any())).thenReturn(null);

        assertThrows(DataNotExistException.class, () -> service.created(from));
        verify(menuMapper).selectOne(argThat(wrapper -> wrapper.getSqlSegment().contains("deleted")));
    }

    @Test
    void modifyShouldRejectMenuWithChildren() {
        var existing = menu(null, MenuType.DIRECTORY, null, 0);
        var from = new MenuSaveFrom();
        from.setId(existing.getId());
        from.setMenuType(MenuType.MENU);
        from.setRouteName("SystemUser");
        when(menuMapper.selectOne(any())).thenReturn(existing);
        when(menuMapper.selectCount(any())).thenReturn(1L);

        assertThrows(DataException.class, () -> service.modify(from));
    }

    @Test
    void modifyShouldRejectDescendantAsParent() {
        var existing = menu(null, MenuType.DIRECTORY, null, 0);
        var descendant = menu(existing.getId(), MenuType.DIRECTORY, null, 0);
        var from = new MenuSaveFrom();
        from.setId(existing.getId());
        from.setPid(descendant.getId());
        from.setMenuType(MenuType.DIRECTORY);
        when(menuMapper.selectOne(any())).thenReturn(existing, descendant);

        assertThrows(DataException.class, () -> service.modify(from));
    }

    @Test
    void modifyShouldRejectDeletedMenu() {
        var existing = menu(null, MenuType.DIRECTORY, null, 0);
        existing.setDeleted(Instant.now());
        var from = new MenuSaveFrom();
        from.setId(existing.getId());
        from.setMenuType(MenuType.DIRECTORY);
        when(menuMapper.selectOne(any())).thenReturn(null);

        assertThrows(DataNotExistException.class, () -> service.modify(from));
        verify(menuMapper).selectOne(argThat(wrapper -> wrapper.getSqlSegment().contains("deleted")));
    }

    @Test
    void modifyShouldIgnoreDeletedChildren() {
        var existing = menu(null, MenuType.DIRECTORY, null, 0);
        var from = new MenuSaveFrom();
        from.setId(existing.getId());
        from.setMenuType(MenuType.MENU);
        from.setRouteName("SystemUser");
        when(menuMapper.selectOne(any())).thenReturn(existing);

        service.modify(from);

        verify(menuMapper).selectCount(argThat(wrapper -> wrapper.getSqlSegment().contains("deleted")));
    }

    @Test
    void deleteShouldRejectDeletedMenu() {
        var existing = menu(null, MenuType.DIRECTORY, null, 0);
        existing.setDeleted(Instant.now());
        when(menuMapper.selectOne(any())).thenReturn(null);

        assertThrows(DataNotExistException.class, () -> service.deleteById(existing.getId()));
        verify(menuMapper).selectOne(argThat(wrapper -> wrapper.getSqlSegment().contains("deleted")));
    }

    @Test
    void getByRelRoleIdShouldReturnMenusOnly() {
        var roleId = UUID.randomUUID();
        var directory = menu(null, MenuType.DIRECTORY, null, 0);
        var leaf = menu(directory.getId(), MenuType.MENU, "SystemUser", 0);
        when(relRoleMenuMapper.getByRoleId(roleId))
                .thenReturn(List.of(new RelRoleMenu(roleId, directory.getId()), new RelRoleMenu(roleId, leaf.getId())));
        when(menuMapper.selectList(any())).thenReturn(List.of(directory, leaf));

        assertEquals(List.of(leaf), service.getByRelRoleId(roleId));
    }

    private static Role role(boolean state) {
        var role = new Role();
        role.setId(UUID.randomUUID());
        role.setState(state);
        return role;
    }

    private static Menu menu(UUID pid, MenuType menuType, String routeName, int sort) {
        var menu = new Menu();
        menu.setId(UUID.randomUUID());
        menu.setPid(pid);
        menu.setMenuType(menuType);
        menu.setRouteName(routeName);
        menu.setSort(sort);
        return menu;
    }

    private static MenuTreeVO toTreeVO(Menu menu) {
        var vo = new MenuTreeVO();
        vo.setId(menu.getId());
        vo.setPid(menu.getPid());
        vo.setName(menu.getName());
        vo.setMenuType(menu.getMenuType());
        vo.setRouteName(menu.getRouteName());
        vo.setSort(menu.getSort());
        vo.setChildren(new ArrayList<>());
        return vo;
    }
}
