/*
 *  Copyright 2018-2025 yangxj96
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

package com.devops00.spectra.core.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.core.javabean.system.converter.MenuConverter;
import com.devops00.spectra.core.javabean.system.entity.Menu;
import com.devops00.spectra.core.javabean.system.vo.MenuVO;
import com.devops00.spectra.core.javabean.user.entity.RelRoleMenu;
import com.devops00.spectra.core.javabean.user.from.RoleMenuFrom;
import com.devops00.spectra.core.mapper.user.RelRoleMenuMapper;
import com.devops00.spectra.core.service.system.MenuService;
import com.devops00.spectra.core.service.user.RelRoleMenuService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/// 关联服务-角色和菜单
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Service
public class RelRoleMenuServiceImpl implements RelRoleMenuService {

    private final MenuConverter menuConverter;

    private final RelRoleMenuMapper relRoleMenuMapper;

    private final MenuService menuService;

    public RelRoleMenuServiceImpl(MenuConverter menuConverter, RelRoleMenuMapper relRoleMenuMapper, MenuService menuService) {
        this.menuConverter = menuConverter;
        this.relRoleMenuMapper = relRoleMenuMapper;
        this.menuService = menuService;
    }


    @Override
    @Transactional
    public void grant(String roleId, RoleMenuFrom from) {
        // 当前角色关联的菜单信息
        var currentIds = relRoleMenuMapper.getByRoleId(roleId)
                .stream()
                .map(RelRoleMenu::getMenuId)
                .collect(Collectors.toSet());

        var targetIds = new HashSet<>(from.getMenuIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleMenu>()
                    .eq(RelRoleMenu::getRoleId, roleId)
                    .in(RelRoleMenu::getRoleId, removeIds);
            relRoleMenuMapper.delete(wrapper);
        }
        // 计算新增且插入
        var addIds = new HashSet<>(targetIds);
        addIds.removeAll(currentIds);  // target - current = 新增
        if (CollUtils.isNotEmpty(addIds)) {
            List<RelRoleMenu> newMenu = addIds.stream()
                    .map(addId -> {
                        var datum = new RelRoleMenu();
                        datum.setRoleId(roleId);
                        datum.setMenuId(addId);
                        return datum;
                    })
                    .collect(Collectors.toList());
            relRoleMenuMapper.insert(newMenu);
        }
    }

    @Override
    @Transactional
    public void revoke(String roleId) {
        // 删除角色关联的菜单
        var wrapper = new LambdaQueryWrapper<RelRoleMenu>().eq(RelRoleMenu::getRoleId, roleId);
        relRoleMenuMapper.delete(wrapper);
    }

    @Override
    public List<MenuVO> get(String roleId) {
        List<Menu> menus = menuService.getByRelRoleId(roleId);
        return menuConverter.toVOList(menus);
    }

}
