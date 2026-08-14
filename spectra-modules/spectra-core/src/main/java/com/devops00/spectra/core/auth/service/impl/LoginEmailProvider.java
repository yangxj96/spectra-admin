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

import com.devops00.spectra.common.constant.RedisCacheKey;
import com.devops00.spectra.common.exception.KaptchaNotMatchException;
import com.devops00.spectra.core.auth.service.AccountService;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.exception.LoginException;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.strategy.provider.EmailAuthenticationProvider;
import com.devops00.spectra.security.base.util.VerificationCodeDigest;
import com.devops00.spectra.security.base.util.VerificationCodeRedisStore;
import org.jspecify.annotations.NullMarked;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

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

    private final RedisTemplate<String, Object> redisTemplate;

    private final UserService userService;

    private final AccountService accountService;

    private final SecurityUserHelper securityUserHelper;

    private final SecurityProperties securityProperties;

    public LoginEmailProvider(@Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redisTemplate, UserService userService,
                              AccountService accountService,
                              SecurityUserHelper securityUserHelper, SecurityProperties securityProperties) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.accountService = accountService;
        this.securityUserHelper = securityUserHelper;
        this.securityProperties = securityProperties;
    }

    @Override
    public Authentication login(String email, String code) throws AuthenticationException {
        var account = accountService.getByEmail(email);
        if (account == null) {
            throw new LoginException("账号或验证码错误");
        }
        var user = userService.getById(account.getUserId());
        if (user == null) {
            throw new LoginException("账号或验证码错误");
        }
        var su = securityUserHelper.toSecurityUser(LoginType.EMAIL, account, user);
        return new UsernamePasswordAuthenticationToken(su, null, su.getAuthorities());
    }

    @Override
    public void kaptchaValidate(String email, String kaptcha) {
        var key = RedisCacheKey.LOGIN_EMAIL_CODE + email;
        var attemptsKey = RedisCacheKey.LOGIN_EMAIL_CODE_ATTEMPTS + email;
        var attempts = redisTemplate.opsForValue().increment(attemptsKey);
        if (attempts != null && attempts == 1L) {
            redisTemplate.expire(attemptsKey, securityProperties.getVerificationCodeExpire(), java.util.concurrent.TimeUnit.SECONDS);
        }
        if (attempts != null && attempts > securityProperties.getVerificationCodeMaxAttempts()) {
            throw new KaptchaNotMatchException("验证码尝试次数过多");
        }
        var digest = VerificationCodeDigest.digest(kaptcha, securityProperties.getVerificationCodeHmacKey());
        if (!VerificationCodeRedisStore.compareAndDelete(redisTemplate, key, digest)) {
            throw new KaptchaNotMatchException("验证码错误");
        }
    }

}
