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

package com.devops00.spectra.core.security.authentication.provider;

import com.devops00.spectra.common.exception.KaptchaNotMatchException;
import com.devops00.spectra.core.common.service.KaptchaService;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.security.authentication.service.impl.SecurityUserHelper;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.core.security.authentication.constant.LoginType;
import com.devops00.spectra.core.security.authentication.exception.LoginException;
import com.devops00.spectra.core.security.authentication.strategy.provider.UsernamePasswordAuthenticationProvider;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 用户名密码登录
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/17 23:37
 */
@Service
@NullMarked
public class LoginUsernamePasswordProvider extends UsernamePasswordAuthenticationProvider {

    private final KaptchaService kaptchaService;

    private final UserService userService;

    private final AuthenticationIdentityService authenticationIdentityService;

    private final PasswordCredentialService passwordCredentialService;

    private final PasswordEncoder passwordEncoder;

    private final SecurityUserHelper securityUserHelper;

    public LoginUsernamePasswordProvider(KaptchaService kaptchaService, UserService userService,
                                         AuthenticationIdentityService authenticationIdentityService,
                                         PasswordCredentialService passwordCredentialService,
                                         PasswordEncoder passwordEncoder, SecurityUserHelper securityUserHelper) {
        this.kaptchaService = kaptchaService;
        this.userService = userService;
        this.authenticationIdentityService = authenticationIdentityService;
        this.passwordCredentialService = passwordCredentialService;
        this.passwordEncoder = passwordEncoder;
        this.securityUserHelper = securityUserHelper;
    }

    @Override
    public Authentication login(String username, String password) throws AuthenticationException {
        var identity = authenticationIdentityService.findPasswordIdentity(username);
        if (identity == null) {
            throw new LoginException("账号或密码错误");
        }
        var credential = passwordCredentialService.getByUserId(identity.getUserId());
        if (credential == null || !passwordEncoder.matches(password, credential.getPasswordHash())) {
            throw new LoginException("账号或密码错误");
        }
        var user = userService.getById(identity.getUserId());
        if (user == null) {
            throw new LoginException("账号或密码错误");
        }
        var su = securityUserHelper.toSecurityUser(LoginType.PASSWORD, identity, credential, user);
        return new UsernamePasswordAuthenticationToken(su, null, su.getAuthorities());
    }

    @Override
    public void kaptchaValidate(String kaptcha) {
        if (Boolean.TRUE.equals(kaptchaService.isCheck())) {
            if (!kaptchaService.consumeKaptchaCode(kaptcha)) {
                throw new KaptchaNotMatchException("验证码错误");
            }
        }
    }

}
