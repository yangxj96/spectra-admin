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
import com.devops00.spectra.core.security.authorization.javabean.entity.SecurityRoleMenu;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMenuMapper;
import com.devops00.spectra.core.security.authorization.service.SecurityRoleMenuStore;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 角色菜单关系写入实现。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/29
 */
@Component
@RequiredArgsConstructor
public class SecurityRoleMenuStoreImpl implements SecurityRoleMenuStore {

    private final SecurityRoleMenuMapper securityRoleMenuMapper;

    @Override
    public void deleteByMenuId(UUID menuId) {
        if (menuId == null) {
            return;
        }
        securityRoleMenuMapper.delete(new QueryWrapper<SecurityRoleMenu>().eq("menu_id", menuId));
    }
}
