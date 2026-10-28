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
package com.devops00.spectra.core.security.authorization.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.BuiltinDataException;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.javabean.entity.SecurityRoleMenu;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMenuMapper;
import com.devops00.spectra.core.security.authorization.service.SecurityRoleMenuGrantService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * 角色菜单授权写入实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
@Component
@RequiredArgsConstructor
public class SecurityRoleMenuGrantServiceImpl implements SecurityRoleMenuGrantService {

    private final SecurityRoleMapper securityRoleMapper;

    private final SecurityRoleMenuMapper securityRoleMenuMapper;

    @Override
    public void replace(UUID roleId, Set<UUID> menuIds) {
        requireEditableRole(roleId);
        var currentIds = findMenuIds(roleId);
        var targetIds = menuIds == null ? Set.<UUID>of() : Set.copyOf(menuIds);
        var removeIds = new HashSet<>(currentIds);
        removeIds.removeAll(targetIds);
        if (!removeIds.isEmpty()) {
            securityRoleMenuMapper.delete(new LambdaQueryWrapper<SecurityRoleMenu>()
                    .eq(SecurityRoleMenu::getRoleId, roleId)
                    .in(SecurityRoleMenu::getMenuId, removeIds));
        }
        var addIds = new HashSet<>(targetIds);
        addIds.removeAll(currentIds);
        for (UUID menuId : addIds) {
            var relation = new SecurityRoleMenu();
            relation.setRoleId(roleId);
            relation.setMenuId(menuId);
            securityRoleMenuMapper.insert(relation);
        }
    }

    @Override
    public void revoke(UUID roleId) {
        requireEditableRole(roleId);
        securityRoleMenuMapper.delete(new LambdaQueryWrapper<SecurityRoleMenu>()
                .eq(SecurityRoleMenu::getRoleId, roleId));
    }

    @Override
    public Set<UUID> findMenuIds(UUID roleId) {
        if (roleId == null) {
            return Set.of();
        }
        return securityRoleMenuMapper.selectList(new LambdaQueryWrapper<SecurityRoleMenu>()
                .eq(SecurityRoleMenu::getRoleId, roleId))
                .stream()
                .map(SecurityRoleMenu::getMenuId)
                .filter(Objects::nonNull)
                .collect(java.util.stream.Collectors.toSet());
    }

    private void requireEditableRole(UUID roleId) {
        var role = securityRoleMapper.selectById(roleId);
        if (role == null) {
            throw new DataNotExistException("角色不存在");
        }
        if (!SecurityAuthorizationState.ACTIVE.name().equals(role.getState())) {
            throw new DataException("停用角色不能配置菜单");
        }
        if (Boolean.TRUE.equals(role.getSystemManaged()) || !Objects.equals("BUSINESS", role.getRoleKind())) {
            throw new BuiltinDataException("内置角色不可配置菜单");
        }
    }
}
