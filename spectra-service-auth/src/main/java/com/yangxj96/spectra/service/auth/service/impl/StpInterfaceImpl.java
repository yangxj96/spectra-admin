/*
 *  Copyright 2025 yangxj96.com
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
 *
 */
package com.yangxj96.spectra.service.auth.service.impl;

import cn.dev33.satoken.stp.StpInterface;
import cn.dev33.satoken.stp.StpUtil;
import com.yangxj96.spectra.share.javabean.ShareAuthority;
import com.yangxj96.spectra.share.javabean.ShareRole;
import com.yangxj96.spectra.share.service.ShareAuthorityService;
import com.yangxj96.spectra.share.service.ShareRoleService;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private ObjectProvider<ShareRoleService> roleService;

    @Resource
    private ObjectProvider<ShareAuthorityService> authorityService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        Object userId = StpUtil.getTerminalInfo().getExtra("user_id");
        var rService = roleService.getIfUnique();
        var aService = authorityService.getIfUnique();
        if (rService == null || aService == null) {
            return Collections.emptyList();
        }
        if (userId instanceof Long uid) {
            List<ShareRole> roles = rService.getByUserId(uid);
            if (roles.isEmpty()) {
                return Collections.emptyList();
            }
            List<ShareAuthority> authorities = aService.getByRoleIds(roles.stream().map(ShareRole::getId).toList());
            return authorities.stream().map(ShareAuthority::getCode).toList();
        } else {
            return Collections.emptyList();
        }
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        Object userId = StpUtil.getTerminalInfo().getExtra("user_id");
        var rService = roleService.getIfUnique();
        if (rService == null) {
            return Collections.emptyList();
        }

        if (userId instanceof Long uid) {
            List<ShareRole> roles = rService.getByUserId(uid);
            return roles.stream().map(ShareRole::getCode).toList();
        } else {
            return Collections.emptyList();
        }
    }
}
