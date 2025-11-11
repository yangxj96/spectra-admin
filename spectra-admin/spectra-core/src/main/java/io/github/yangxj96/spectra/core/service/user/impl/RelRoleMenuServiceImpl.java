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

package io.github.yangxj96.spectra.core.service.user.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.github.yangxj96.spectra.core.javabean.system.converter.MenuConverter;
import io.github.yangxj96.spectra.core.javabean.system.entity.Menu;
import io.github.yangxj96.spectra.core.javabean.system.vo.MenuVO;
import io.github.yangxj96.spectra.core.service.system.MenuService;
import io.github.yangxj96.spectra.core.javabean.user.entity.RelRoleMenu;
import io.github.yangxj96.spectra.core.javabean.user.from.RoleMenuFrom;
import io.github.yangxj96.spectra.core.mapper.user.RelRoleMenuMapper;
import io.github.yangxj96.spectra.core.service.user.RelRoleMenuService;
import jakarta.annotation.Resource;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 关联服务-角色和菜单
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-11-11
 */
@Service
public class RelRoleMenuServiceImpl implements RelRoleMenuService {

    @Resource
    private RelRoleMenuMapper relRoleMenuMapper;

    @Resource
    private MenuService menuService;

    @Resource
    private MenuConverter menuConverter;

    @Override
    @Transactional
    public void grant(Long roleId, RoleMenuFrom from) {
        // 当前角色关联的菜单信息
        var currentIds = relRoleMenuMapper.getByRoleId(roleId)
                .stream().map(RelRoleMenu::getMenuId).collect(Collectors.toSet());

        var targetIds = new HashSet<>(from.getMenuIds());
        // 计算删除且删除
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds); // current - target = 删除
        if (CollectionUtils.isNotEmpty(removeIds)) {
            var wrapper = new LambdaQueryWrapper<RelRoleMenu>()
                    .eq(RelRoleMenu::getRoleId, roleId)
                    .in(RelRoleMenu::getRoleId, removeIds);
            relRoleMenuMapper.delete(wrapper);
        }
        // 计算新增且插入
        var addIds = new HashSet<>(targetIds);
        addIds.removeAll(currentIds);  // target - current = 新增
        if (CollectionUtils.isNotEmpty(addIds)) {
            List<RelRoleMenu> newMenu = addIds.stream()
                    .map(addId -> RelRoleMenu.builder()
                            .roleId(roleId)
                            .menuId(addId)
                            .build())
                    .collect(Collectors.toList());
            relRoleMenuMapper.insert(newMenu);
        }
    }

    @Override
    @Transactional
    public void revoke(Long roleId) {
        // 删除角色关联的菜单
        var wrapper = new LambdaQueryWrapper<RelRoleMenu>().eq(RelRoleMenu::getRoleId, roleId);
        relRoleMenuMapper.delete(wrapper);
    }

    @Override
    public List<MenuVO> get(Long roleId) {
        List<Menu> menus = menuService.getByRelRoleId(roleId);
        return menuConverter.toVOS(menus);
    }

}
