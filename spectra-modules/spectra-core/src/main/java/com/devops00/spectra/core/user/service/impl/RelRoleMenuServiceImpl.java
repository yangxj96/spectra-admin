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

package com.devops00.spectra.core.user.service.impl;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.foundation.collection.CollUtils;
import com.devops00.spectra.core.security.authorization.service.SecurityRoleMenuGrantService;
import com.devops00.spectra.core.system.javabean.converter.MenuConverter;
import com.devops00.spectra.core.system.javabean.entity.Menu;
import com.devops00.spectra.core.system.javabean.enums.MenuType;
import com.devops00.spectra.core.system.javabean.vo.MenuVO;
import com.devops00.spectra.core.system.service.MenuService;
import com.devops00.spectra.core.user.javabean.from.RoleMenuFrom;
import com.devops00.spectra.core.user.service.RelRoleMenuService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 关联服务-角色和菜单
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/11/11 00:00
 */
@Slf4j
@Service
public class RelRoleMenuServiceImpl implements RelRoleMenuService {

    private final MenuConverter menuConverter;

    private final SecurityRoleMenuGrantService securityRoleMenuGrantService;

    private final MenuService menuService;

    public RelRoleMenuServiceImpl(MenuConverter menuConverter,
                                  SecurityRoleMenuGrantService securityRoleMenuGrantService,
                                  MenuService menuService) {
        this.menuConverter = menuConverter;
        this.securityRoleMenuGrantService = securityRoleMenuGrantService;
        this.menuService = menuService;
    }

    @Override
    @Transactional
    public void grant(UUID roleId, RoleMenuFrom from) {
        if (!Objects.equals(roleId, from.getRoleId())) {
            throw new DataException("路径角色ID与请求角色ID不一致");
        }
        var requestedIds = new HashSet<>(from.getMenuIds());
        Set<UUID> targetIds = new HashSet<>();
        if (CollUtils.isNotEmpty(requestedIds)) {
            var requestedMenus = menuService.listByIds(requestedIds);
            var foundIds = requestedMenus.stream()
                    .filter(menu -> menu.getDeleted() == null)
                    .map(Menu::getId)
                    .collect(Collectors.toSet());
            if (!foundIds.containsAll(requestedIds)) {
                throw new DataNotExistException("存在无效菜单");
            }
            requestedMenus.stream()
                    .filter(menu -> menu.getDeleted() == null)
                    .filter(menu -> menu.getMenuType() == MenuType.MENU)
                    .map(Menu::getId)
                    .forEach(targetIds::add);
        }
        securityRoleMenuGrantService.replace(roleId, targetIds);
    }

    @Override
    @Transactional
    public void revoke(UUID roleId) {
        securityRoleMenuGrantService.revoke(roleId);
    }

    @Override
    public List<MenuVO> get(UUID roleId) {
        List<Menu> menus = menuService.getByRelRoleId(roleId);
        return menuConverter.toVOList(menus);
    }
}
