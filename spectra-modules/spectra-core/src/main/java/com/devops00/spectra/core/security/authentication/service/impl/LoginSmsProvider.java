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

import com.devops00.spectra.common.constant.RedisCacheKey;
import com.devops00.spectra.common.exception.KaptchaNotMatchException;
import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.exception.LoginException;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.strategy.provider.SmsAuthenticationProvider;
import com.devops00.spectra.security.base.util.VerificationCodeDigest;
import com.devops00.spectra.security.base.util.VerificationCodeRedisStore;
import com.devops00.spectra.security.base.util.SecurityRedisExecutor;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/**
 * 短信验证码登录
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/6/28
 */
@Service
@NullMarked
public class LoginSmsProvider extends SmsAuthenticationProvider {

    private final RedisTemplate<String, Object> redisTemplate;

    private final UserService userService;

    private final AuthenticationIdentityService identityService;

    private final PasswordCredentialService passwordCredentialService;

    private final SecurityUserHelper securityUserHelper;

    private final SecurityProperties securityProperties;

    public LoginSmsProvider(@Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redisTemplate, UserService userService,
                            AuthenticationIdentityService identityService,
                            PasswordCredentialService passwordCredentialService,
                            SecurityUserHelper securityUserHelper, SecurityProperties securityProperties) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.identityService = identityService;
        this.passwordCredentialService = passwordCredentialService;
        this.securityUserHelper = securityUserHelper;
        this.securityProperties = securityProperties;
    }

    @Override
    public Authentication login(String phone, String code) throws AuthenticationException {
        AuthenticationIdentity identity = identityService.findIdentity(LoginType.SMS.name(), phone);
        if (identity == null) {
            throw new LoginException("账号或验证码错误");
        }
        var user = userService.getById(identity.getUserId());
        var credential = passwordCredentialService.getByUserId(identity.getUserId());
        if (user == null || credential == null) {
            throw new LoginException("账号或验证码错误");
        }
        var su = securityUserHelper.toSecurityUser(LoginType.SMS, identity, credential, user);
        return new UsernamePasswordAuthenticationToken(su, null, su.getAuthorities());
    }

    @Override
    public void kaptchaValidate(String phone, String kaptcha) {
        SecurityRedisExecutor.run("校验短信登录验证码", () -> {
            var key = RedisCacheKey.LOGIN_SMS_CODE + phone;
            var attemptsKey = RedisCacheKey.LOGIN_SMS_CODE_ATTEMPTS + phone;
            var attempts = SecurityRedisExecutor.require("记录短信验证码失败次数",
                    () -> redisTemplate.opsForValue().increment(attemptsKey));
            if (attempts == 1L) {
                redisTemplate.expire(attemptsKey, securityProperties.getVerificationCodeExpire(), java.util.concurrent.TimeUnit.SECONDS);
            }
            if (attempts > securityProperties.getVerificationCodeMaxAttempts()) {
                throw new KaptchaNotMatchException("验证码尝试次数过多");
            }
            var digest = VerificationCodeDigest.digest(kaptcha, securityProperties.getVerificationCodeHmacKey());
            if (!VerificationCodeRedisStore.compareAndDelete(redisTemplate, key, digest)) {
                throw new KaptchaNotMatchException("验证码错误");
            }
        });
    }

}
