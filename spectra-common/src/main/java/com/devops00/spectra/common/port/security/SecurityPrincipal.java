/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
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

    /** @return 用户 ID */
    UUID getId();

    /** @return 用户姓名 */
    String getName();

    /** @return 登录用户名 */
    String getUsername();

    /** @return 用户头像 */
    String getAvatar();

    /** @return 所属组织 ID */
    String getOrganizationId();

    /** @return 所属部门 ID */
    @Nullable
    UUID getDepartmentId();

    /** @return 用户时区 */
    String getTimezone();

    /** @return 用户是否启用 */
    boolean isEnabled();

    /** @return 账户是否未过期 */
    boolean isAccountNonExpired();

    /** @return 账户是否未锁定 */
    boolean isAccountNonLocked();

    /** @return 凭证是否未过期 */
    boolean isCredentialsNonExpired();

    /** @return 是否必须先修改密码 */
    boolean isPasswordChangeRequired();

    /** @return 当前主体可用的稳定权限编码 */
    List<String> getAuthorityNames();
}
