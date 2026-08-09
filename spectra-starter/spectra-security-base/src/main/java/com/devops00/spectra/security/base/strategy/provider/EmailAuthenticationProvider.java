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

package com.devops00.spectra.security.base.strategy.provider;

import com.devops00.spectra.security.base.strategy.tokens.EmailAuthenticationToken;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

/// 邮箱登录
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/29 10:46
@Slf4j
@NullMarked
public abstract class EmailAuthenticationProvider implements BasicAuthenticationProvider {

    @Override
    public @Nullable Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof EmailAuthenticationToken params)) {
            throw new RuntimeException("登录失败,未知原因");
        }
        try {
            kaptchaValidate(params.getCredentials().toString());

            if (params.getPrincipal() == null || params.getCredentials() == null) {
                throw new BadCredentialsException("邮箱或验证码不能为空");
            }

            return login(params.getPrincipal().toString(), params.getCredentials().toString());
        } finally {
            kaptchaDelete();
        }
    }

    public abstract Authentication login(String email, String code) throws AuthenticationException;

    public abstract void kaptchaValidate(String kaptcha);

    public abstract void kaptchaDelete();

    @Override
    public boolean supports(Class<?> authentication) {
        return EmailAuthenticationToken.class.isAssignableFrom(authentication);
    }
}