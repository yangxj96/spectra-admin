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

package com.devops00.spectra.common.port.security;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/**
 * 业务可见的安全主体契约。
 *
 * <p>该契约不暴露密码、Spring Security 类型或 Redis 快照，业务模块可以据此读取当前主体的稳定身份和授权摘要。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
@NullMarked
public interface SecurityPrincipal {

    /**
     * 返回安全主体的用户标识。
     *
     * @return 安全主体的用户标识。
     */
    UUID getId();

    /**
     * 返回安全主体的展示名称。
     *
     * @return 安全主体的展示名称。
     */
    String getName();

    /**
     * 返回安全主体的登录用户名。
     *
     * @return 安全主体的登录用户名。
     */
    String getUsername();

    /**
     * 返回安全主体的头像地址。
     *
     * @return 安全主体的头像地址。
     */
    String getAvatar();

    /**
     * 返回安全主体所属组织的标识。
     *
     * @return 安全主体所属组织的标识。
     */
    String getOrganizationId();

    /**
     * 返回安全主体所属部门的标识。
     *
     * @return 安全主体所属部门的标识。
     */
    @Nullable
    UUID getDepartmentId();

    /**
     * 返回安全主体配置的时区。
     *
     * @return 安全主体配置的时区。
     */
    String getTimezone();

    /**
     * 判断安全主体是否处于启用状态。
     *
     * @return 条件判断结果。
     */
    boolean isEnabled();

    /**
     * 判断安全主体的账号是否未过期。
     *
     * @return 条件判断结果。
     */
    boolean isAccountNonExpired();

    /**
     * 判断安全主体的账号是否未锁定。
     *
     * @return 条件判断结果。
     */
    boolean isAccountNonLocked();

    /**
     * 判断安全主体的凭据是否未过期。
     *
     * @return 条件判断结果。
     */
    boolean isCredentialsNonExpired();

    /**
     * 判断安全主体是否需要修改密码。
     *
     * @return 条件判断结果。
     */
    boolean isPasswordChangeRequired();

    /**
     * 返回安全主体拥有的权限名称集合。
     *
     * @return 安全主体拥有的权限名称集合。
     */
    List<String> getAuthorityNames();
}
