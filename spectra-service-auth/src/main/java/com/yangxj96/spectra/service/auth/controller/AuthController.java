/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.service.auth.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.yangxj96.spectra.service.auth.javabean.from.UsernamePasswordFrom;
import com.yangxj96.spectra.service.auth.javabean.vo.TokenVO;
import com.yangxj96.spectra.service.auth.service.AuthService;
import com.yangxj96.spectra.starter.common.annotation.ULog;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.security.auth.login.LoginException;

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

    @SaIgnore
    @ULog("登录")
    @PostMapping("/login")
    public TokenVO login(@Validated @RequestBody UsernamePasswordFrom params) throws LoginException {
        return bindService.login(params);
    }

    @SaCheckLogin
    @ULog("退出登录")
    @PostMapping("/logout")
    @ResponseStatus(HttpStatus.OK)
    public void logout() {
        bindService.logout();
    }

    @SaCheckLogin
    @ULog("token检查")
    @PostMapping("/check")
    public void check() {
        // 能进入方法,就说明是正常的token了,无需多余的逻辑进行检查
    }
}
