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
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.change.SecurityAuthenticationPort;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.from.EmailCodeFrom;
import com.devops00.spectra.security.base.javabean.from.LoginFrom;
import com.devops00.spectra.security.base.javabean.from.RefreshTokenFrom;
import com.devops00.spectra.security.base.javabean.from.SmsCodeFrom;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.session.WebCookiePolicy;
import com.devops00.spectra.security.base.util.TokenDigestService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import com.devops00.spectra.security.starter.web.dispatcher.LoginDispatcher;
import com.devops00.spectra.security.starter.web.service.AuthService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

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

    private final SecurityProperties securityProperties;

    private final SecurityAuditWriter securityAuditWriter;

    private final SecurityAuthenticationPort securityAuthenticationPort;

    private final SecurityContextAccessor securityContextAccessor;

    @Autowired
    public AuthController(LoginDispatcher loginDispatcher, AuthService authService, SecurityProperties securityProperties,
                          ObjectProvider<SecurityAuditWriter> securityAuditWriterProvider,
                          SecurityAuthenticationPort securityAuthenticationPort, SecurityContextAccessor securityContextAccessor) {
        this(loginDispatcher, authService, securityProperties, securityAuditWriterProvider.getIfAvailable(),
                securityAuthenticationPort, securityContextAccessor);
    }

    public AuthController(LoginDispatcher loginDispatcher, AuthService authService, SecurityProperties securityProperties,
                          SecurityAuthenticationPort securityAuthenticationPort, SecurityContextAccessor securityContextAccessor) {
        this(loginDispatcher, authService, securityProperties, (SecurityAuditWriter) null,
                securityAuthenticationPort, securityContextAccessor);
    }

    private AuthController(LoginDispatcher loginDispatcher, AuthService authService, SecurityProperties securityProperties,
                           SecurityAuditWriter securityAuditWriter, SecurityAuthenticationPort securityAuthenticationPort,
                           SecurityContextAccessor securityContextAccessor) {
        this.loginDispatcher = loginDispatcher;
        this.authService = authService;
        this.securityProperties = securityProperties;
        this.securityAuditWriter = securityAuditWriter;
        this.securityAuthenticationPort = securityAuthenticationPort;
        this.securityContextAccessor = securityContextAccessor;
        WebCookiePolicy.validate(securityProperties);
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
    public TokenVO login(@Validated @RequestBody LoginFrom params, HttpServletRequest request, HttpServletResponse response) {
        String username = params.getUsername() != null ? params.getUsername() : "";

        // 登录锁定检查
        if (securityAuthenticationPort.isLockedOut(username)) {
            audit("AUTH_LOGIN_FAILED", null, client(request), "LOCKED_OUT");
            throw new SpectraException("账号已锁定，请稍后再试");
        }

        try {
            var authentication = loginDispatcher.authenticate(params);
            SecurityContextHolder.getContext().setAuthentication(authentication);
            if (authentication.getPrincipal() instanceof SecurityUser su) {
                securityAuthenticationPort.clearLoginFail(username);
                audit("AUTH_LOGIN_SUCCEEDED", su.getId(), client(request), null);
                TokenVO token = securityAuthenticationPort.login(su);
                issueWebCookies(request, response, token.getRefreshToken());
                if (isWebClient(request)) {
                    token.setRefreshToken(null);
                }
                return token;
            } else {
                throw new UsernameNotFoundException("未找到用户");
            }
        } catch (BadCredentialsException e) {
            securityAuthenticationPort.recordLoginFail(username);
            audit("AUTH_LOGIN_FAILED", null, client(request), "BAD_CREDENTIALS");
            throw e;
        }
    }

    /**
     * 用户退出登陆
     */
    @ULog(value = "'用户登出系统'", type = SysLogType.SAFETY)
    @PostMapping(value = "/logout", version = "1.0.0+")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("permitAll()")
    public void logout(@RequestBody(required = false) RefreshTokenFrom params, HttpServletRequest request,
                       HttpServletResponse response) {
        String refreshToken = params != null ? params.getRefreshToken() : null;
        boolean webClient = isWebClient(request);
        if (webClient) {
            validateCsrf(request);
            if (StrUtils.isBlank(refreshToken)) {
                refreshToken = readCookie(request, securityProperties.getRefreshCookieName());
            }
        }
        var token = securityContextAccessor.currentToken();

        if (StrUtils.isNotBlank(token)) {
            securityAuthenticationPort.logout(token);
        }

        if (StrUtils.isNotBlank(refreshToken)) {
            securityAuthenticationPort.logoutByRefreshToken(refreshToken);
        }
        if (webClient) {
            clearWebCookies(response);
        }
        audit("AUTH_LOGOUT", securityContextAccessor.currentUserId(), client(request), null);
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
    public TokenVO refresh(@RequestBody(required = false) RefreshTokenFrom params, HttpServletRequest request,
                           HttpServletResponse response) {
        boolean webClient = isWebClient(request);
        if (webClient) {
            validateCsrf(request);
        }
        String cookieRefreshToken = readCookie(request, securityProperties.getRefreshCookieName());
        String refreshToken = webClient ? cookieRefreshToken : params != null ? params.getRefreshToken() : null;
        if (StrUtils.isBlank(refreshToken)) {
            throw new IllegalArgumentException("刷新token不能为空");
        }
        TokenVO token;
        try {
            token = securityAuthenticationPort.refreshByRefreshToken(refreshToken);
        } catch (RuntimeException exception) {
            audit("TOKEN_REFRESH_FAILED", securityContextAccessor.currentUserId(), client(request), "REFRESH_REJECTED");
            throw exception;
        }
        audit("TOKEN_REFRESH_SUCCEEDED", securityContextAccessor.currentUserId(), client(request), null);
        issueWebCookies(request, response, token.getRefreshToken());
        if (webClient) {
            token.setRefreshToken(null);
        }
        return token;
    }

    private String client(HttpServletRequest request) {
        String clientType = request.getHeader("X-Client-Type");
        return clientType == null || clientType.isBlank() ? "WEB" : clientType.trim().toUpperCase(java.util.Locale.ROOT);
    }

    private void audit(String eventType, UUID operatorId, String client, String reason) {
        if (securityAuditWriter == null) {
            return;
        }
        AuditResult result = eventType.endsWith("_FAILED") ? AuditResult.FAILED : AuditResult.SUCCEEDED;
        securityAuditWriter.append(new SecurityAuditEvent(UUID.randomUUID(), eventType, operatorId, null, client, null, null,
                Map.of(), Map.of(), reason, null, result, null));
    }

    private boolean isWebClient(HttpServletRequest request) {
        String clientType = request.getHeader("X-Client-Type");
        return clientType == null || clientType.isBlank() || "web".equalsIgnoreCase(clientType);
    }

    private void issueWebCookies(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
        if (!isWebClient(request) || StrUtils.isBlank(refreshToken)) {
            return;
        }
        addCookie(response, securityProperties.getRefreshCookieName(), refreshToken, true,
                securityProperties.getRefreshCookieSameSite(), securityProperties.getRefreshTokenExpire());
        addCookie(response, securityProperties.getCsrfCookieName(), TokenDigestService.generateToken(), false,
                securityProperties.getRefreshCookieSameSite(), securityProperties.getRefreshTokenExpire());
    }

    private void clearWebCookies(HttpServletResponse response) {
        addCookie(response, securityProperties.getRefreshCookieName(), "", true,
                securityProperties.getRefreshCookieSameSite(), 0);
        addCookie(response, securityProperties.getCsrfCookieName(), "", false,
                securityProperties.getRefreshCookieSameSite(), 0);
    }

    private void addCookie(HttpServletResponse response, String name, String value, boolean httpOnly,
                           String sameSite, long maxAgeSeconds) {
        ResponseCookie cookie = ResponseCookie.from(name, value)
                .httpOnly(httpOnly)
                .secure(securityProperties.isRefreshCookieSecure())
                .path(securityProperties.getRefreshCookiePath())
                .sameSite(sameSite)
                .maxAge(java.time.Duration.ofSeconds(maxAgeSeconds))
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }

    private void validateCsrf(HttpServletRequest request) {
        String header = request.getHeader(securityProperties.getCsrfHeaderName());
        String cookie = null;
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie candidate : cookies) {
                if (securityProperties.getCsrfCookieName().equals(candidate.getName())) {
                    cookie = candidate.getValue();
                    break;
                }
            }
        }
        if (StrUtils.isBlank(header) || StrUtils.isBlank(cookie)
                || !java.security.MessageDigest.isEqual(header.getBytes(java.nio.charset.StandardCharsets.UTF_8),
                cookie.getBytes(java.nio.charset.StandardCharsets.UTF_8))) {
            throw new IllegalArgumentException("CSRF 校验失败");
        }
    }

    private String readCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return null;
        }
        for (Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }
}
