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

package com.devops00.spectra.security.base.strategy.tokens;

import lombok.Getter;
import org.springframework.security.authentication.AbstractAuthenticationToken;

import java.util.Collections;

/**
 * 用户名密码+验证码登录
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/29 10:55
 */
public class UsernamePasswordCaptchaAuthenticationToken extends AbstractAuthenticationToken {

    /**
     * 用户名
     */
    private final String username;

    /**
     * 密码
     */
    private final String password;

    /**
     * 验证码
     */
    @Getter
    private final String captcha;

    public UsernamePasswordCaptchaAuthenticationToken(String username, String password, String captcha) {
        super(Collections.emptyList());
        this.username = username;
        this.password = password;
        this.captcha = captcha;
        setAuthenticated(false);
    }

    @Override
    public Object getCredentials() {
        return password;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }
}
