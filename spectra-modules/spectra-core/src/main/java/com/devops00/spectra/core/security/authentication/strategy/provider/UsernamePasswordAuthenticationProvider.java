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

package com.devops00.spectra.core.security.authentication.strategy.provider;

import com.devops00.spectra.core.security.authentication.strategy.tokens.UsernamePasswordCaptchaAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StringUtils;

/**
 * 用户名密码登录
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/29 10:51
 */
@Slf4j
@NullMarked
public abstract class UsernamePasswordAuthenticationProvider implements BasicAuthenticationProvider {

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof UsernamePasswordCaptchaAuthenticationToken params)) {
            throw new BadCredentialsException("登录失败");
        }
        if (!(params.getPrincipal() instanceof String username)
                || !StringUtils.hasText(username)
                || !(params.getCredentials() instanceof String password)
                || !StringUtils.hasText(password)) {
            throw new BadCredentialsException("用户名或密码不能为空");
        }
        // 验证码校验成功时已在 Redis 中原子消费；错误尝试保留验证码直到过期或达到尝试上限。
        kaptchaValidate(params.getCaptcha());
        return login(username, password);
    }

    /**
     * 处理内部业务逻辑（{@code login}）。
     */
    public abstract Authentication login(String username, String password) throws AuthenticationException;

    /**
     * 处理内部业务逻辑（{@code kaptchaValidate}）。
     */
    public abstract void kaptchaValidate(String kaptcha);

    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordCaptchaAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
