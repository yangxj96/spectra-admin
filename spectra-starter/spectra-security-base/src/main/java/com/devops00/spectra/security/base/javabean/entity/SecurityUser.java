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

package com.devops00.spectra.security.base.javabean.entity;

import com.devops00.spectra.common.constant.DataScopeType;
import com.devops00.spectra.common.utils.CollUtils;
import com.devops00.spectra.common.utils.StrUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

/**
 * 用DTO传输类
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/2 17:55
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SecurityUser implements UserDetails {

    /**
     * 用户ID
     */
    private UUID id;

    /**
     * 姓名
     */
    private String name;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 头像
     */
    private String avatar;

    /**
     * 所属组织机构ID
     */
    private String organizationId;

    /**
     * 有效数据范围类型
     */
    @Nullable
    private DataScopeType dataScopeType;

    /**
     * 所属部门ID
     */
    @Nullable
    private UUID departmentId;

    /**
     * 数据范围目标部门（DEPT/DEPT_AND_CHILDREN/CUSTOM 时填充）
     */
    @Nullable
    private List<UUID> dataScopeTargetIds;

    /**
     * 用户密码
     */
    private String password;

    /**
     * 用户状态
     */
    private Short state;

    /**
     * 时区
     */
    private String timezone;

    /**
     * 是否启用。
     */
    private boolean enabled = true;

    /**
     * 账户是否未过期。
     */
    private boolean accountNonExpired = true;

    /**
     * 账户是否未锁定。
     */
    private boolean accountNonLocked = true;

    /**
     * 凭证是否未过期。
     */
    private boolean credentialsNonExpired = true;

    /**
     * 扩展数据字段。
     */
    @Nullable
    private transient Map<String, Object> extraData;

    /**
     * 简易权限处理,主要是为了序列化的问题
     */
    private List<SimpleGrantedAuthority> authorities;

    @Override
    @NullMarked
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return CollUtils.isNotEmpty(authorities) ? authorities : Collections.emptyList();
    }

    @Override
    @NullMarked
    public String getUsername() {
        return StrUtils.isNotBlank(this.getEmail()) ? this.getEmail() : "";
    }

    public void setUsername(String username) {
        this.setEmail(username);
    }
}
