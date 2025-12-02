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

package io.github.yangxj96.spectra.core.controller.auth;

import io.github.yangxj96.spectra.core.configure.security.strategy.LoginStrategy;
import io.github.yangxj96.spectra.core.javabean.auth.javabean.dto.SecurityUser;
import io.github.yangxj96.spectra.core.javabean.auth.javabean.from.LoginFrom;
import io.github.yangxj96.spectra.core.javabean.auth.javabean.from.UsernamePasswordFrom;
import io.github.yangxj96.spectra.core.javabean.auth.javabean.vo.TokenVO;
import io.github.yangxj96.spectra.core.service.auth.AuthService;
import io.github.yangxj96.spectra.core.service.auth.TokenService;
import io.github.yangxj96.spectra.framework.features.ulog.annotation.ULog;
import jakarta.annotation.Resource;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;
import java.util.List;
import java.util.Map;

/**
 * 认证控制器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    @Resource
    private AuthService bindService;

    @PostMapping("/login")
    public TokenVO login(@Validated @RequestBody LoginFrom params) throws LoginException {
        return bindService.login(params);
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout(@RequestHeader("Authorization") String authHeader) {
        bindService.logout(authHeader);
    }

    @ULog("token检查")
    @PostMapping(value = "/check", version = "2.0.0")
    public void check() {
        // 能进入方法,就说明是正常的token了,无需多余的逻辑进行检查
    }
}
