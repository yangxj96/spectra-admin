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
import com.devops00.spectra.security.base.exception.LoginException;
import com.devops00.spectra.security.base.strategy.provider.EmailAuthenticationProvider;
import com.devops00.spectra.security.base.strategy.tokens.EmailAuthenticationToken;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

/// 邮箱验证码登录
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/6/28
@Service
@NullMarked
public class LoginEmailProvider extends EmailAuthenticationProvider {

    private final RedisTemplate<String, Object> redisTemplate;

    private final UserService userService;

    private final AccountService accountService;

    private final SecurityUserHelper securityUserHelper;

    private String currentEmail;

    public LoginEmailProvider(RedisTemplate<String, Object> redisTemplate, UserService userService, AccountService accountService,
            SecurityUserHelper securityUserHelper) {
        this.redisTemplate = redisTemplate;
        this.userService = userService;
        this.accountService = accountService;
        this.securityUserHelper = securityUserHelper;
    }

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (authentication instanceof EmailAuthenticationToken token) {
            this.currentEmail = token.getPrincipal().toString();
        }
        return super.authenticate(authentication);
    }

    @Override
    public Authentication login(String email, String code) throws AuthenticationException {
        var account = accountService.getByEmail(email);
        if (account == null) {
            throw new LoginException("该邮箱未注册");
        }
        var user = userService.getById(account.getUserId());
        if (user == null) {
            throw new LoginException("用户不存在");
        }
        var su = securityUserHelper.toSecurityUser(user);
        return new UsernamePasswordAuthenticationToken(su, null, su.getAuthorities());
    }

    @Override
    public void kaptchaValidate(String kaptcha) {
        var key = RedisCacheKey.EMAIL_CODE + currentEmail;
        var val = redisTemplate.opsForValue().get(key);
        if (val == null || !kaptcha.equals(val.toString())) {
            throw new KaptchaNotMatchException("验证码错误");
        }
    }

    @Override
    public void kaptchaDelete() {
        var key = RedisCacheKey.EMAIL_CODE + currentEmail;
        redisTemplate.delete(key);
    }
}
