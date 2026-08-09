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

package com.devops00.spectra.core.system.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.constant.Common;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.common.utils.TreeBuilder;
import com.devops00.spectra.core.system.javabean.converter.MenuConverter;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.javabean.enums.MenuType;
import com.devops00.spectra.core.system.javabean.from.MenuSaveFrom;
import com.devops00.spectra.core.system.javabean.vo.MenuTreeVO;
import com.devops00.spectra.core.system.mapper.MenuMapper;
import com.devops00.spectra.core.system.service.MenuService;
import com.devops00.spectra.core.user.javabean.entity.RelRoleMenu;
import com.devops00.spectra.core.user.javabean.entity.Role;
import com.devops00.spectra.core.user.mapper.RelRoleMenuMapper;
import com.devops00.spectra.core.user.service.RelUserRoleService;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/// 菜单service层-实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Slf4j
@Service
public class MenuServiceImpl extends BaseServiceImpl<MenuMapper, Menu> implements MenuService {

    private final MenuConverter menuConverter;

    private final MenuMapper menuMapper;

    private final RelRoleMenuMapper roleMenuMapper;

    private final RelUserRoleService relUserRoleService;

    public MenuServiceImpl(MenuMapper menuMapper, MenuConverter menuConverter, RelRoleMenuMapper roleMenuMapper,
            RelUserRoleService relUserRoleService) {
        this.menuMapper = menuMapper;
        this.menuConverter = menuConverter;
        this.roleMenuMapper = roleMenuMapper;
        this.relUserRoleService = relUserRoleService;
    }

    @Override
    @Transactional
    public void created(MenuSaveFrom params) {
        validateRouteBinding(params);
        validateTreeStructure(params);
        var menu = menuConverter.toEntity(params);
        this.save(menu);
    }

    @Override
    @Transactional
    public void modify(MenuSaveFrom params) {
        validateRouteBinding(params);
        var existing = getActiveMenu(params.getId());
        if (existing == null) {
            throw new DataNotExistException("[" + params.getId() + "]不存在");
        }
        validateTreeStructure(params);
        var menu = menuConverter.toEntity(params);
        menuMapper.updateById(menu);
    }

    private void validateRouteBinding(MenuSaveFrom params) {
        if (params.getMenuType() == MenuType.DIRECTORY && StrUtils.isNotBlank(params.getRouteName())) {
            throw new DataException("目录不能配置路由名称");
        }
        if (params.getMenuType() == MenuType.MENU && StrUtils.isBlank(params.getRouteName())) {
            throw new DataException("菜单路由名称不能为空");
        }
    }

    private void validateTreeStructure(MenuSaveFrom params) {
        var parentId = params.getPid();
        var visited = new HashSet<UUID>();
        while (parentId != null) {
            if (parentId.equals(params.getId()) || !visited.add(parentId)) {
                throw new DataException("上级菜单不能是自身或后代");
            }
            var parent = getActiveMenu(parentId);
            if (parent == null) {
                throw new DataNotExistException("上级菜单不存在");
            }
            if (parent.getMenuType() != MenuType.DIRECTORY) {
                throw new DataException("上级菜单必须是目录");
            }
            parentId = parent.getPid();
        }
        if (params.getId() != null && params.getMenuType() == MenuType.MENU) {
            var childCount = menuMapper.selectCount(
                    new QueryWrapper<Menu>()
                            .eq("pid", params.getId())
                            .isNull("deleted")
            );
            if (childCount > 0) {
                throw new DataException("菜单节点不能拥有子节点");
            }
        }
    }

    @Override
    public @Nullable List<MenuTreeVO> tree() {
        // 先转树形VO
        var db = menuMapper.selectList(new QueryWrapper<Menu>().isNull("deleted"));
        if (CollUtils.isEmpty(db)) {
            return Collections.emptyList();
        }
        var vos = menuConverter.toTreeVOList(db);
        return new TreeBuilder<>(vos).buildTree(Common.PID);
    }

    @Override
    public List<MenuTreeVO> current(UUID userId) {
        var roleIds = relUserRoleService.getRoles(userId)
                .stream()
                .filter(role -> Boolean.TRUE.equals(role.getState()))
                .map(Role::getId)
                .toList();
        if (CollUtils.isEmpty(roleIds)) {
            return Collections.emptyList();
        }

        var relations = roleMenuMapper.selectList(
                new QueryWrapper<RelRoleMenu>()
                        .in("role_id", roleIds)
                        .isNull("deleted")
        );
        if (CollUtils.isEmpty(relations)) {
            return Collections.emptyList();
        }

        var menus = menuMapper.selectList(
                new QueryWrapper<Menu>()
                        .isNotNull("menu_type")
                        .isNull("deleted")
        );
        if (CollUtils.isEmpty(menus)) {
            return Collections.emptyList();
        }

        var menuMap = menus.stream().collect(Collectors.toMap(Menu::getId, Function.identity()));
        var includedIds = new HashSet<UUID>();
        for (RelRoleMenu relation : relations) {
            var menu = menuMap.get(relation.getMenuId());
            if (menu == null || menu.getMenuType() != MenuType.MENU) {
                continue;
            }
            while (menu != null && includedIds.add(menu.getId())) {
                menu = menuMap.get(menu.getPid());
            }
        }

        var authorizedMenus = menus.stream()
                .filter(menu -> includedIds.contains(menu.getId()))
                .toList();
        if (CollUtils.isEmpty(authorizedMenus)) {
            return Collections.emptyList();
        }
        var vos = menuConverter.toTreeVOList(authorizedMenus);
        var tree = new TreeBuilder<>(vos).buildTree(Common.PID);
        return tree == null ? Collections.emptyList() : tree;
    }

    @Override
    public List<Menu> getByRelRoleId(UUID id) {
        var relRoleMenus = roleMenuMapper.getByRoleId(id);
        if (CollUtils.isEmpty(relRoleMenus)) {
            return Collections.emptyList();
        }
        return menuMapper.selectList(
                        new QueryWrapper<Menu>()
                                .in("id", relRoleMenus.stream().map(RelRoleMenu::getMenuId).toList())
                                .isNull("deleted")
                )
                .stream()
                .filter(menu -> menu.getMenuType() == MenuType.MENU)
                .toList();
    }

    @Override
    public void deleteById(UUID id) {
        var menu = getActiveMenu(id);
        if (menu == null) {
            throw new DataNotExistException("[" + id + "]不存在");
        }
        this.removeById(menu);
    }

    private @Nullable Menu getActiveMenu(UUID id) {
        return menuMapper.selectOne(
                new QueryWrapper<Menu>()
                        .eq("id", id)
                        .isNull("deleted")
        );
    }
}
