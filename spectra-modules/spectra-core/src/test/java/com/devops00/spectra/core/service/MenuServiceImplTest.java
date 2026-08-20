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

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.authorization.entity.SecurityRole;
import com.devops00.spectra.core.authorization.entity.SecurityRoleMenu;
import com.devops00.spectra.core.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.authorization.mapper.SecurityRoleMenuMapper;
import com.devops00.spectra.core.system.javabean.converter.MenuConverter;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.javabean.enums.MenuType;
import com.devops00.spectra.core.system.javabean.from.MenuSaveFrom;
import com.devops00.spectra.core.system.javabean.vo.MenuTreeVO;
import com.devops00.spectra.core.system.mapper.MenuMapper;
import com.devops00.spectra.core.system.service.impl.MenuServiceImpl;
import com.devops00.spectra.security.base.authorization.AuthorizationAssignment;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshot;
import com.devops00.spectra.security.base.authorization.AuthorizationSnapshotProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

/**
 * 菜单服务测试
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/30
 */
@ExtendWith(MockitoExtension.class)
class MenuServiceImplTest {

    @Mock
    private MenuMapper menuMapper;

    @Mock
    private MenuConverter menuConverter;

    @Mock
    private SecurityRoleMapper securityRoleMapper;

    @Mock
    private SecurityRoleMenuMapper securityRoleMenuMapper;

    @Mock
    private AuthorizationSnapshotProvider authorizationSnapshotProvider;

    private MenuServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new MenuServiceImpl(menuMapper, menuConverter, authorizationSnapshotProvider,
                securityRoleMapper, securityRoleMenuMapper);
    }

    @Test
    void currentShouldMergeRolesIncludeAncestorsPruneEmptyDirectoriesAndSort() {
        var userId = UUID.randomUUID();
        var activeRoleA = securityRole("ROLE_A");
        var activeRoleB = securityRole("ROLE_B");
        var system = menu(null, MenuType.DIRECTORY, null, 2);
        var emptyRoot = menu(null, MenuType.DIRECTORY, null, 1);
        var workflow = menu(system.getId(), MenuType.DIRECTORY, null, 2);
        var workflowMenu = menu(workflow.getId(), MenuType.MENU, "SystemWorkflow", 2);
        var userMenu = menu(system.getId(), MenuType.MENU, "SystemUser", 1);

        when(authorizationSnapshotProvider.load(userId)).thenReturn(AuthorizationSnapshot.of(List.of(
                new AuthorizationAssignment(UUID.randomUUID(), activeRoleA.getCode(), 1, Map.of(), Map.of()),
                new AuthorizationAssignment(UUID.randomUUID(), "ROLE_DISABLED", 1, Map.of(), Map.of()),
                new AuthorizationAssignment(UUID.randomUUID(), activeRoleB.getCode(), 1, Map.of(), Map.of()))));
        when(securityRoleMapper.selectList(any())).thenReturn(List.of(activeRoleA, activeRoleB));
        when(securityRoleMenuMapper.selectList(any())).thenReturn(List.of(relation(activeRoleA.getId(), workflowMenu.getId()),
                relation(activeRoleB.getId(), workflowMenu.getId()), relation(activeRoleB.getId(), userMenu.getId())));
        when(menuMapper.selectList(any())).thenReturn(List.of(system, emptyRoot, workflow, workflowMenu, userMenu));
        when(menuConverter.toTreeVOList(any())).thenAnswer(invocation -> {
            List<Menu> menus = invocation.getArgument(0);
            return menus.stream().map(MenuServiceImplTest::toTreeVO).toList();
        });

        var result = service.current(userId);

        assertEquals(List.of(system.getId()), result.stream().map(MenuTreeVO::getId).toList());
        assertEquals(List.of(userMenu.getId(), workflow.getId()), result.getFirst().getChildren().stream().map(MenuTreeVO::getId).toList());
        assertEquals(List.of(workflowMenu.getId()), result.getFirst().getChildren().get(1).getChildren().stream().map(MenuTreeVO::getId).toList());
        verify(securityRoleMenuMapper).selectList(any());
        verify(menuMapper).selectList(argThat(wrapper -> wrapper.getSqlSegment().contains("deleted")));
    }

    @Test
    void currentShouldReturnEmptyWhenUserHasNoEnabledRole() {
        var userId = UUID.randomUUID();
        when(authorizationSnapshotProvider.load(userId)).thenReturn(AuthorizationSnapshot.of(List.of(
                new AuthorizationAssignment(UUID.randomUUID(), "ROLE_DISABLED", 1, Map.of(), Map.of()))));
        when(securityRoleMapper.selectList(any())).thenReturn(List.of());

        var result = service.current(userId);

        assertEquals(List.of(), result);
        verify(securityRoleMenuMapper, never()).selectList(any());
        verify(menuMapper, never()).selectList(any());
    }

    @Test
    void currentShouldUseSecurityRoleAssignmentAndRoleMenu() {
        var userId = UUID.randomUUID();
        var roleId = UUID.randomUUID();
        var securityRole = new SecurityRole();
        securityRole.setId(roleId);
        securityRole.setCode("ROLE_MANAGER");
        securityRole.setState("ACTIVE");
        var system = menu(null, MenuType.DIRECTORY, null, 1);
        var userMenu = menu(system.getId(), MenuType.MENU, "SystemUser", 1);
        var relation = new SecurityRoleMenu();
        relation.setRoleId(roleId);
        relation.setMenuId(userMenu.getId());
        AuthorizationSnapshotProvider snapshotProvider = ignored -> AuthorizationSnapshot.of(List.of(
                new AuthorizationAssignment(UUID.randomUUID(), "ROLE_MANAGER", 1, Map.of(), Map.of())));
        var securityService = new MenuServiceImpl(menuMapper, menuConverter, snapshotProvider,
                securityRoleMapper, securityRoleMenuMapper);

        when(securityRoleMapper.selectList(any())).thenReturn(List.of(securityRole));
        when(securityRoleMenuMapper.selectList(any())).thenReturn(List.of(relation));
        when(menuMapper.selectList(any())).thenReturn(List.of(system, userMenu));
        when(menuConverter.toTreeVOList(any())).thenAnswer(invocation -> {
            List<Menu> menus = invocation.getArgument(0);
            return menus.stream().map(MenuServiceImplTest::toTreeVO).toList();
        });

        var result = securityService.current(userId);

        assertEquals(List.of(system.getId()), result.stream().map(MenuTreeVO::getId).toList());
        assertEquals(List.of(userMenu.getId()), result.getFirst().getChildren().stream().map(MenuTreeVO::getId).toList());
        verify(securityRoleMenuMapper).selectList(any());
        verifyNoInteractions(authorizationSnapshotProvider);
    }

    @Test
    void currentShouldReturnAllMenusForRootWithoutRoleMenuRelations() {
        var userId = UUID.randomUUID();
        var system = menu(null, MenuType.DIRECTORY, null, 1);
        var userMenu = menu(system.getId(), MenuType.MENU, "SystemUser", 1);
        var workflowMenu = menu(system.getId(), MenuType.MENU, "SystemWorkflow", 2);

        when(authorizationSnapshotProvider.load(userId)).thenReturn(AuthorizationSnapshot.of(List.of(
                new AuthorizationAssignment(UUID.randomUUID(), "ROLE_DEV_OPS", 1, Map.of(), Map.of()))));
        when(menuMapper.selectList(any())).thenReturn(List.of(system, userMenu, workflowMenu));
        when(menuConverter.toTreeVOList(any())).thenAnswer(invocation -> {
            List<Menu> menus = invocation.getArgument(0);
            return menus.stream().map(MenuServiceImplTest::toTreeVO).toList();
        });

        var result = service.current(userId);

        assertEquals(List.of(system.getId()), result.stream().map(MenuTreeVO::getId).toList());
        assertEquals(List.of(userMenu.getId(), workflowMenu.getId()),
                result.getFirst().getChildren().stream().map(MenuTreeVO::getId).toList());
        verify(securityRoleMenuMapper, never()).selectList(any());
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
        when(securityRoleMenuMapper.selectList(any()))
                .thenReturn(List.of(relation(roleId, directory.getId()), relation(roleId, leaf.getId())));
        when(menuMapper.selectList(any())).thenReturn(List.of(directory, leaf));

        assertEquals(List.of(leaf), service.getByRelRoleId(roleId));
    }

    private static SecurityRole securityRole(String code) {
        var role = new SecurityRole();
        role.setId(UUID.randomUUID());
        role.setCode(code);
        role.setState("ACTIVE");
        return role;
    }

    private static SecurityRoleMenu relation(UUID roleId, UUID menuId) {
        var relation = new SecurityRoleMenu();
        relation.setRoleId(roleId);
        relation.setMenuId(menuId);
        return relation;
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
