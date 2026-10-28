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

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.javabean.entity.SecurityRole;
import com.devops00.spectra.core.security.authorization.javabean.entity.SecurityRoleMenu;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMenuMapper;
import com.devops00.spectra.core.security.authorization.service.SecurityMenuGrantQuery;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

/**
 * 角色菜单授权读取实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
@Component
@RequiredArgsConstructor
public class SecurityMenuGrantQueryImpl implements SecurityMenuGrantQuery {

    private final SecurityRoleMapper securityRoleMapper;

    private final SecurityRoleMenuMapper securityRoleMenuMapper;

    @Override
    public List<UUID> findActiveMenuIdsByRoleCodes(List<String> roleCodes) {
        if (roleCodes == null || roleCodes.isEmpty()) {
            return List.of();
        }
        var roles = securityRoleMapper.selectList(new QueryWrapper<SecurityRole>()
                .select("id", "code")
                .in("code", roleCodes)
                .eq("state", SecurityAuthorizationState.ACTIVE.name()));
        if (roles.isEmpty()) {
            return List.of();
        }
        var roleIds = roles.stream().map(SecurityRole::getId).toList();
        return securityRoleMenuMapper.selectList(new QueryWrapper<SecurityRoleMenu>().in("role_id", roleIds))
                .stream()
                .map(SecurityRoleMenu::getMenuId)
                .distinct()
                .toList();
    }

    @Override
    public List<UUID> findMenuIdsByRoleId(UUID roleId) {
        if (roleId == null) {
            return List.of();
        }
        return securityRoleMenuMapper.selectList(new QueryWrapper<SecurityRoleMenu>().eq("role_id", roleId))
                .stream()
                .map(SecurityRoleMenu::getMenuId)
                .distinct()
                .toList();
    }
}
