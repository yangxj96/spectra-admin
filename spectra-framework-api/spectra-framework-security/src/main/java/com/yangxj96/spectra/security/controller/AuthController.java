/*
 * Copyright (c) 2018-2025 Jack Young (杨新杰)
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package com.yangxj96.spectra.security.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaIgnore;
import com.yangxj96.spectra.core.annotation.ULog;
import com.yangxj96.spectra.security.entity.from.UsernamePasswordFrom;
import com.yangxj96.spectra.security.entity.vo.TokenVO;
import com.yangxj96.spectra.security.service.AuthService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.security.auth.login.LoginException;

/**
 * 认证控制器
 *
 * @author 杨新杰
 * @since 2025/5/26 16:56
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
    public void logout() {
        bindService.logout();
    }
}
