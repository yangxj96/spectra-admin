/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.service.auth.impl;

import io.github.yangxj96.spectra.common.exception.SpectraException;
import io.github.yangxj96.spectra.core.configure.security.strategy.LoginStrategy;
import io.github.yangxj96.spectra.core.javabean.auth.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.from.LoginFrom;
import io.github.yangxj96.spectra.core.javabean.auth.vo.TokenVO;
import io.github.yangxj96.spectra.core.service.auth.AuthService;
import io.github.yangxj96.spectra.core.service.auth.TokenService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * 认证service层-实现
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private List<LoginStrategy> loginStrategies;

    @Resource
    private TokenService tokenService;

    @Override
    public TokenVO login(LoginFrom request) {
        LoginStrategy handler = loginStrategies.stream()
                .filter(h -> h.supports(request.type()))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("不支持的登录方式"));
        SecurityUser user = handler.authenticate(request);
        return tokenService.createToken(user);
    }

    @Override
    public void logout(String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7);
            tokenService.deleteToken(token);
        } else {
            throw new SpectraException("退出失败");
        }
    }

}
