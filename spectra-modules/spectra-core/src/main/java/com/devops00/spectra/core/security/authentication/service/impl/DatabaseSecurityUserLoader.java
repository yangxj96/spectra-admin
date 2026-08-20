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

package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.javabean.entity.PasswordCredential;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.holder.SecurityUserLoader;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 基于当前数据库身份源加载安全主体。
 */
@Component
@NullMarked
public class DatabaseSecurityUserLoader implements SecurityUserLoader {

    private final UserService userService;

    private final AuthenticationIdentityService authenticationIdentityService;

    private final PasswordCredentialService passwordCredentialService;

    private final SecurityUserHelper securityUserHelper;

    /**
     * 延迟解析用户业务服务，避免安全会话仓储初始化时反向创建完整的用户服务依赖图。
     * <p>
     * 安全会话仓储在启动阶段只需要注册用户加载器；真正加载用户发生在请求认证期间，
     * 此时安全上下文和 MyBatis 基础设施已经完成初始化，因此不会改变认证行为。
     *
     * @param userService                   用户业务服务
     * @param authenticationIdentityService 密码身份服务
     * @param passwordCredentialService     密码凭证服务
     * @param securityUserHelper            安全用户转换器
     */
    public DatabaseSecurityUserLoader(
                                      @Lazy UserService userService,
                                      AuthenticationIdentityService authenticationIdentityService,
                                      PasswordCredentialService passwordCredentialService,
                                      SecurityUserHelper securityUserHelper) {
        this.userService = userService;
        this.authenticationIdentityService = authenticationIdentityService;
        this.passwordCredentialService = passwordCredentialService;
        this.securityUserHelper = securityUserHelper;
    }

    @Override
    public @Nullable SecurityUser load(UUID userId) {
        User user = userService.getById(userId);
        if (user == null || user.getEmail() == null) {
            return null;
        }
        AuthenticationIdentity identity = authenticationIdentityService.findPasswordIdentity(user.getEmail());
        PasswordCredential credential = passwordCredentialService.getByUserId(userId);
        if (identity == null || credential == null) {
            return null;
        }
        try {
            return securityUserHelper.toSecurityUser(LoginType.PASSWORD, identity, credential, user);
        } catch (RuntimeException exception) {
            return null;
        }
    }
}
