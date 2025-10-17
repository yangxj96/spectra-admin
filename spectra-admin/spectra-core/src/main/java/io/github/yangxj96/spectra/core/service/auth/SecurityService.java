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

package io.github.yangxj96.spectra.core.service.auth;

import io.github.yangxj96.spectra.common.enums.AuthScope;
import io.github.yangxj96.spectra.core.javabean.system.entity.Menu;
import io.github.yangxj96.spectra.core.javabean.user.entity.Role;

import java.util.List;

/**
 * 安全服务
 */
public interface SecurityService {

    /**
     * 获取当前用户角色
     *
     * @return 当前用户角色列表
     */
    List<Role> getCurrentRoles();

    /**
     * 获取当前用户菜单
     *
     * @return 当前用户菜单列表
     */
    List<Menu> getCurrentMenus();

    /**
     * 获取当前用户最大权限范围
     *
     * @return 最大权限范围
     */
    AuthScope getCurrentMaxScope();
}
