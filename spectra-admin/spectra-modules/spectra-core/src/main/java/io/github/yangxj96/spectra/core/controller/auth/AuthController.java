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

import io.github.yangxj96.spectra.core.configure.security.holder.SecUtil;
import io.github.yangxj96.spectra.core.configure.security.javabean.SecurityUser;
import io.github.yangxj96.spectra.core.configure.security.strategy.LoginDispatcher;
import io.github.yangxj96.spectra.core.configure.ulog.annotation.ULog;
import io.github.yangxj96.spectra.core.configure.ulog.enums.SysLogType;
import io.github.yangxj96.spectra.core.javabean.auth.from.LoginFrom;
import io.github.yangxj96.spectra.core.javabean.auth.vo.TokenVO;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/// 认证控制器
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginDispatcher loginDispatcher;

    public AuthController(LoginDispatcher loginDispatcher) {
        this.loginDispatcher = loginDispatcher;
    }


    @PermitAll
    @PostMapping("/login")
    @ULog(value = "用户登录", type = SysLogType.SAFETY)
    public TokenVO login(@Validated @RequestBody LoginFrom params) {
        var authentication = loginDispatcher.authenticate(params);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (authentication.getPrincipal() instanceof SecurityUser su) {
            return SecUtil.login(su);
        } else {
            throw new UsernameNotFoundException("未找到用户");
        }
    }

    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @ULog(value = "用户登出", type = SysLogType.SAFETY)
    public void logout() {
        SecUtil.logout();
    }

    @ULog("token 检查")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/check", version = "1.0.0+")
    public void check() {
        // 能进入方法就说明 token 是正常的
    }
}
