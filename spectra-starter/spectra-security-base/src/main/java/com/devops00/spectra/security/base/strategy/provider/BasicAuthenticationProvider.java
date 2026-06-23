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

package com.devops00.spectra.security.base.strategy.provider;


import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.springframework.security.authentication.AuthenticationProvider;

import java.util.List;
import java.util.UUID;

/// 基础的认证适配器
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/2/17 23:47
public interface BasicAuthenticationProvider extends AuthenticationProvider {

    /// 用户对象转SecurityUser对象
    ///
    /// @param user 用户对象
    /// @return SecurityUser对象
    SecurityUser toSecurityUser(Object user);

    /// 根据UserId获取角色列表
    ///
    /// @param userId 用户ID
    /// @return 角色列表
    List<Object> getUserRole(UUID userId);

    /// 根据角色ID列表获取权限列表
    ///
    /// @param roles 角色ID列表
    /// @return 权限列表
    List<Object> getUserAuthority(List<UUID> roles);
}
