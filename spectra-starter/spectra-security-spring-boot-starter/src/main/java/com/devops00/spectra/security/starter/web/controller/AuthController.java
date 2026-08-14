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

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.common.exception.SpectraException;
import com.devops00.spectra.common.utils.StrUtils;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.log.base.enums.SysLogType;
import com.devops00.spectra.security.base.holder.SecUtil;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.from.EmailCodeFrom;
import com.devops00.spectra.security.base.javabean.from.LoginFrom;
import com.devops00.spectra.security.base.javabean.from.RefreshTokenFrom;
import com.devops00.spectra.security.base.javabean.from.SmsCodeFrom;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.starter.web.dispatcher.LoginDispatcher;
import com.devops00.spectra.security.starter.web.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 认证处理器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/17 23:28
 */
@Slf4j
@RestController
@RequestMapping("/auth")
public class AuthController {

    private final LoginDispatcher loginDispatcher;

    private final AuthService authService;

    public AuthController(LoginDispatcher loginDispatcher, AuthService authService) {
        this.loginDispatcher = loginDispatcher;
        this.authService = authService;
    }

    /**
     * 用户登陆
     *
     * @param params [LoginFrom]登陆入参
     * @return 成功响应token,失败抛出异常
     */
    @ULog(value = "'用户[' + #params.username + ']进行登陆'", type = SysLogType.SAFETY)
    @Encrypt(response = false)
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/login", version = "1.0.0+")
    public TokenVO login(@Validated @RequestBody LoginFrom params) {
        String username = params.getUsername() != null ? params.getUsername() : "";

        // 登录锁定检查
        if (SecUtil.isLockedOut(username)) {
            throw new SpectraException("账号已锁定，请稍后再试");
        }

        try {
            var authentication = loginDispatcher.authenticate(params);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            if (authentication.getPrincipal() instanceof SecurityUser su) {
                SecUtil.clearLoginFail(username);
                return SecUtil.login(su);
            } else {
                throw new UsernameNotFoundException("未找到用户");
            }
        } catch (BadCredentialsException e) {
            SecUtil.recordLoginFail(username);
            throw e;
        }
    }

    /**
     * 用户退出登陆
     */
    @ULog(value = "'用户['+T(com.devops00.spectra.security.base.holder.SecUtil).getCurrentUsername()+']登出系统'", type = SysLogType.SAFETY)
    @PostMapping(value = "/logout", version = "1.0.0+")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("permitAll()")
    public void logout(@RequestBody(required = false) RefreshTokenFrom params) {
        String refreshToken = params != null ? params.getRefreshToken() : null;
        var token = SecUtil.getCurrentToken();

        if (StrUtils.isNotBlank(token)) {
            SecUtil.logout(token);
        }

        if (StrUtils.isNotBlank(refreshToken)) {
            SecUtil.logoutByRefreshToken(refreshToken);
        }
    }

    /**
     * 发送短信验证码
     */
    @ULog(value = "'发送短信验证码'", type = SysLogType.SAFETY)
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/sms", version = "1.0.0+")
    @ResponseStatus(HttpStatus.OK)
    public void sendSms(@Validated @RequestBody SmsCodeFrom params) {
        authService.sendSmsCode(params.getPhone());
    }

    /**
     * 发送邮箱验证码
     */
    @ULog(value = "'发送邮箱验证码'", type = SysLogType.SAFETY)
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/email", version = "1.0.0+")
    @ResponseStatus(HttpStatus.OK)
    public void sendEmail(@Validated @RequestBody EmailCodeFrom params) {
        authService.sendEmailCode(params.getEmail());
    }

    /**
     * 发送绑定手机号验证码。
     */
    @ULog(value = "'发送绑定手机号验证码'", type = SysLogType.SAFETY)
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/bind/sms", version = "1.0.0+")
    @ResponseStatus(HttpStatus.OK)
    public void sendBindingSms(@Validated @RequestBody SmsCodeFrom params) {
        authService.sendBindingSmsCode(params.getPhone());
    }

    /**
     * 发送绑定邮箱验证码。
     */
    @ULog(value = "'发送绑定邮箱验证码'", type = SysLogType.SAFETY)
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/bind/email", version = "1.0.0+")
    @ResponseStatus(HttpStatus.OK)
    public void sendBindingEmail(@Validated @RequestBody EmailCodeFrom params) {
        authService.sendBindingEmailCode(params.getEmail());
    }

    /**
     * 刷新token
     */
    @ULog(value = "'刷新token'", type = SysLogType.SAFETY)
    @Encrypt(response = false)
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/refresh", version = "1.0.0+")
    public TokenVO refresh(@Validated @RequestBody RefreshTokenFrom params) {
        return SecUtil.refreshByRefreshToken(params.getRefreshToken());
    }
}
