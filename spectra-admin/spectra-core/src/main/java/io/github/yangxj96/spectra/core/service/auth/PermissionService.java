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

/**
 * 权限类,主要用作在SpEL表达式中进行计算
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/28
 */
public interface PermissionService {

    /**
     * 必须是超级管理员
     */
    void administrators();

    /**
     * 判断是否有指定权限
     *
     * @param permission 指定权限CODE
     */
    void hasPermission(String permission);

    /**
     * 判断是否有指定角色
     *
     * @param role 指定角色CODE
     */
    void hasRole(String role);

}
