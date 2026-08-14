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

package com.devops00.spectra.security.starter.web.dispatcher;

import com.devops00.spectra.security.base.javabean.from.LoginFrom;
import com.devops00.spectra.security.base.strategy.tokens.EmailAuthenticationToken;
import com.devops00.spectra.security.base.strategy.tokens.SmsAuthenticationToken;
import com.devops00.spectra.security.base.strategy.tokens.TotpAuthenticationToken;
import com.devops00.spectra.security.base.strategy.tokens.UsernamePasswordCaptchaAuthenticationToken;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 登录分发器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/29 10:47
 */
@Component
public class LoginDispatcher {

    private final AuthenticationManager authenticationManager;

    public LoginDispatcher(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    /**
     * 进行登录
     *
     * @param request 登录请求参数
     * @return 登录结果
     */
    public Authentication authenticate(LoginFrom request) {
        if (request == null || request.getType() == null) {
            throw new BadCredentialsException("登录类型不能为空");
        }

        return switch (request.getType()) {
            case PASSWORD -> authenticationManager
                    .authenticate(new UsernamePasswordCaptchaAuthenticationToken(request.getUsername(), request.getPassword(), request.getCaptcha()));

            case SMS -> authenticationManager.authenticate(new SmsAuthenticationToken(request.getUsername(), request.getSmsCode()));

            case EMAIL -> authenticationManager.authenticate(new EmailAuthenticationToken(request.getUsername(), request.getEmailCode()));

            case OTP -> authenticationManager.authenticate(new TotpAuthenticationToken(
                    request.getUsername(), request.getOtp() != null ? request.getOtp() : request.getPrincipal()));
        };
    }
}
