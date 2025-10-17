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

package io.github.yangxj96.spectra.core.service.auth.impl;

import cn.dev33.satoken.exception.NotPermissionException;
import cn.dev33.satoken.exception.NotRoleException;
import cn.dev33.satoken.stp.StpUtil;
import io.github.yangxj96.spectra.core.service.auth.PermissionService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * 权限类,主要用作在SpEL表达式中进行计算 <br/>
 * 根据查看sa-token的源码,发现他所有的验证都是不通过直接抛出异常 <br/>
 * 否则,就是有权限 <br/>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
@Slf4j
@Service("ss")
public class PermissionServiceImpl implements PermissionService {

    private static final String ADMINISTRATORS = "ROLE_DEV_OPS";

    @Override
    public void administrators() {
        if (absoluteness()) {
            return;
        }
        throw new NotPermissionException("权限不足");
    }

    @Override
    public void hasPermission(String permission) {
        if (absoluteness()) {
            return;
        }
        if (StpUtil.hasPermission(permission)) {
            return;
        }
        throw new NotPermissionException(permission);
    }

    @Override
    public void hasRole(String role) {
        if (absoluteness()) {
            return;
        }
        if (StpUtil.hasRole(role)) {
            return;
        }
        throw new NotRoleException(role);
    }

    /**
     * 内置的无限制通过的范围,在这里可以指定超级管理员的特征 <br/>
     * 比如存在角色CODE为ROLE_ADMIN_OPS的 <br/>
     * 比如存在权限CODE为 * 的 <br/>
     * 等等方式,进行自定义 <br/>
     *
     * @return 是否为无限制
     */
    private boolean absoluteness() {
        return StpUtil.hasRole(ADMINISTRATORS) || StpUtil.hasPermission("*");
    }

}
