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

import com.devops00.spectra.common.exception.KaptchaNotMatchException;
import com.devops00.spectra.core.auth.service.AccountService;
import com.devops00.spectra.core.common.service.KaptchaService;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.security.base.exception.LoginException;
import com.devops00.spectra.security.base.strategy.provider.UsernamePasswordAuthenticationProvider;
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

    private final AccountService accountService;

    private final PasswordEncoder passwordEncoder;

    private final SecurityUserHelper securityUserHelper;

    public LoginUsernamePasswordProvider(KaptchaService kaptchaService, UserService userService, AccountService accountService,
            PasswordEncoder passwordEncoder, SecurityUserHelper securityUserHelper) {
        this.kaptchaService = kaptchaService;
        this.userService = userService;
        this.accountService = accountService;
        this.passwordEncoder = passwordEncoder;
        this.securityUserHelper = securityUserHelper;
    }

    @Override
    public Authentication login(String username, String password) throws AuthenticationException {
        var account = accountService.getByLoginName(username);
        if (account == null || !passwordEncoder.matches(password, account.getPassword())) {
            throw new LoginException("账号或密码错误");
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
        if (kaptchaService.isCheck() == Boolean.TRUE) {
            var code = kaptchaService.getKaptchaCode();
            if (!kaptcha.equals(code)) {
                throw new KaptchaNotMatchException("验证码错误");
            }
        }
    }

    @Override
    public void kaptchaDelete() {
        kaptchaService.deleteBySessionId();
    }
}
