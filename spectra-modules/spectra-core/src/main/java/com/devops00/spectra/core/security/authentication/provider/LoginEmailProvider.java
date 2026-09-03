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

import com.devops00.spectra.core.common.constant.RedisCacheKey;
import com.devops00.spectra.common.exception.KaptchaNotMatchException;
import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.security.authentication.service.impl.SecurityUserHelper;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.core.security.authentication.constant.LoginType;
import com.devops00.spectra.core.security.authentication.exception.LoginException;
import com.devops00.spectra.framework.configure.security.properties.SecurityProperties;
import com.devops00.spectra.core.security.authentication.strategy.provider.EmailAuthenticationProvider;
import com.devops00.spectra.common.security.crypto.VerificationCodeDigest;
import com.devops00.spectra.common.port.security.SecurityVerificationAttemptStore;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import org.jspecify.annotations.NullMarked;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.time.Duration;

/**
 * 邮箱验证码登录
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/28
 */
@Service
@NullMarked
public class LoginEmailProvider extends EmailAuthenticationProvider {

    private final SecurityVerificationCodeStore verificationCodeStore;

    private final SecurityVerificationAttemptStore verificationAttemptStore;

    private final UserService userService;

    private final AuthenticationIdentityService identityService;

    private final PasswordCredentialService passwordCredentialService;

    private final SecurityUserHelper securityUserHelper;

    private final SecurityProperties securityProperties;

    public LoginEmailProvider(SecurityVerificationCodeStore verificationCodeStore,
                              SecurityVerificationAttemptStore verificationAttemptStore,
                              UserService userService,
                              AuthenticationIdentityService identityService,
                              PasswordCredentialService passwordCredentialService,
                              SecurityUserHelper securityUserHelper, SecurityProperties securityProperties) {
        this.verificationCodeStore = verificationCodeStore;
        this.verificationAttemptStore = verificationAttemptStore;
        this.userService = userService;
        this.identityService = identityService;
        this.passwordCredentialService = passwordCredentialService;
        this.securityUserHelper = securityUserHelper;
        this.securityProperties = securityProperties;
    }

    @Override
    public Authentication login(String email, String code) throws AuthenticationException {
        AuthenticationIdentity identity = identityService.findIdentity(LoginType.EMAIL.name(), email);
        if (identity == null) {
            throw new LoginException("账号或验证码错误");
        }
        var user = userService.getById(identity.getUserId());
        var credential = passwordCredentialService.getByUserId(identity.getUserId());
        if (user == null || credential == null) {
            throw new LoginException("账号或验证码错误");
        }
        var su = securityUserHelper.toSecurityUser(LoginType.EMAIL, identity, credential, user);
        return new UsernamePasswordAuthenticationToken(su, null, su.getAuthorities());
    }

    @Override
    public void kaptchaValidate(String email, String kaptcha) {
        var key = RedisCacheKey.LOGIN_EMAIL_CODE + email;
        var attemptsKey = RedisCacheKey.LOGIN_EMAIL_CODE_ATTEMPTS + email;
        var attempts = verificationAttemptStore.increment(attemptsKey,
                Duration.ofSeconds(securityProperties.getVerificationCodeExpire()));
        if (attempts > securityProperties.getVerificationCodeMaxAttempts()) {
            throw new KaptchaNotMatchException("验证码尝试次数过多");
        }
        var digest = VerificationCodeDigest.digest(kaptcha, securityProperties.getVerificationCodeHmacKey());
        if (!verificationCodeStore.compareAndDelete(key, digest)) {
            throw new KaptchaNotMatchException("验证码错误");
        }
    }

}
