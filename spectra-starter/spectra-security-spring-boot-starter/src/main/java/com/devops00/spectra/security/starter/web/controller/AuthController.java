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

package com.devops00.spectra.security.starter.web.controller;


import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.log.base.enums.SysLogType;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.from.LoginFrom;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.starter.web.dispatcher.LoginDispatcher;
import jakarta.annotation.security.PermitAll;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/// 认证处理器
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/2/17 23:28
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginDispatcher loginDispatcher;


    public AuthController(LoginDispatcher loginDispatcher) {
        this.loginDispatcher = loginDispatcher;
    }

    /// 用户登陆
    ///
    /// @param params [LoginFrom]登陆入参
    /// @return 成功响应token,失败跑出异常
    @ULog(
            value = "'用户[' + #params.username + ']进行登陆'",
            type = SysLogType.SAFETY
    )
    @PermitAll
    @PostMapping(value = "/login", version = "1.0.0+")
    public TokenVO login(@Validated @RequestBody LoginFrom params) {
        var authentication = loginDispatcher.authenticate(params);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        if (authentication.getPrincipal() instanceof SecurityUser su) {
            return SecUtil.login(su);
        } else {
            throw new UsernameNotFoundException("未找到用户");
        }
    }

    /// 用户退出登陆
    @ULog(
            value = "'用户['+T(com.devops00.spectra.security.base.holder.SecUtil).getCurrentUsername()+']登出系统'",
            type = SysLogType.SAFETY
    )
    @PostMapping(value = "/logout", version = "1.0.0+")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    public void logout() {
        SecUtil.logout();
    }

    /// 检查当前token是否有效
    @ULog(
            value = "'用户['+T(com.devops00.spectra.security.base.holder.SecUtil).getCurrentUsername()+']检查是否可用'",
            type = SysLogType.SAFETY
    )
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/check", version = "1.0.0+")
    public void check() {
        // 能进入方法就说明 token 是正常的
    }
}
