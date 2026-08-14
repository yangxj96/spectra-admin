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

package com.devops00.spectra.core.auth.service.impl;

import com.devops00.spectra.core.auth.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.auth.javabean.entity.PasswordCredential;
import com.devops00.spectra.core.auth.service.AuthenticationIdentityService;
import com.devops00.spectra.core.auth.service.PasswordCredentialService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.holder.SecurityUserLoader;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

import java.util.UUID;

/**
 * 基于当前数据库身份源加载安全主体。
 */
@Component
@NullMarked
@RequiredArgsConstructor
public class DatabaseSecurityUserLoader implements SecurityUserLoader {

    private final UserService userService;

    private final AuthenticationIdentityService authenticationIdentityService;

    private final PasswordCredentialService passwordCredentialService;

    private final SecurityUserHelper securityUserHelper;

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
