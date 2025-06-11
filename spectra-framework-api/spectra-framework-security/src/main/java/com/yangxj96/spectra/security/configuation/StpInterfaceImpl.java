/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.security.configuation;

import cn.dev33.satoken.stp.StpInterface;
import com.yangxj96.spectra.security.entity.dto.Authority;
import com.yangxj96.spectra.security.entity.dto.Role;
import com.yangxj96.spectra.security.service.AuthorityService;
import com.yangxj96.spectra.security.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;

/**
 * 自定义权限加载接口实现类
 *
 * @author 杨新杰
 * @since 2025/5/26 19:07
 */
@Component
public class StpInterfaceImpl implements StpInterface {

    @Resource
    private RoleService roleService;

    @Resource
    private AuthorityService authorityService;

    @Override
    public List<String> getPermissionList(Object loginId, String loginType) {
        List<Role> roles = roleService.getByAccountId(Long.parseLong((String) loginId));
        if (roles.isEmpty()) {
            return Collections.emptyList();
        }
        List<Authority> authorities = authorityService.getByRoleIds(roles.stream().map(Role::getId).toList());
        return authorities.stream().map(Authority::getName).toList();
    }

    @Override
    public List<String> getRoleList(Object loginId, String loginType) {
        List<Role> roles = roleService.getByAccountId(Long.parseLong((String) loginId));
        return roles.stream().map(Role::getName).toList();
    }
}
