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

package com.devops00.spectra.core.security.authentication.controller;

import com.devops00.spectra.common.annotation.Encrypt;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaCompleteFrom;
import com.devops00.spectra.core.security.authentication.javabean.from.MfaVerifyFrom;
import com.devops00.spectra.core.security.authentication.service.LoginService;
import com.devops00.spectra.core.security.authentication.service.VerificationCodeService;
import com.devops00.spectra.framework.configure.mvc.security.AuthenticationWebUtils;
import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.javabean.from.EmailCodeFrom;
import com.devops00.spectra.security.base.javabean.from.LoginFrom;
import com.devops00.spectra.security.base.javabean.from.RefreshTokenFrom;
import com.devops00.spectra.security.base.javabean.from.SmsCodeFrom;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.base.session.WebCookiePolicy;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * 认证 HTTP 适配器。
 *
 * <p>登录、MFA、会话撤销和刷新编排由 LoginService 负责；本控制器只处理 HTTP 参数、
 * Web Cookie 和 CSRF。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@RestController
@RequestMapping("/security/authentication")
public class AuthenticationController {

    private final LoginService loginService;
    private final VerificationCodeService verificationCodeService;
    private final SecurityProperties securityProperties;
    private final SecurityContextAccessor securityContextAccessor;

    public AuthenticationController(LoginService loginService, VerificationCodeService verificationCodeService,
                                    SecurityProperties securityProperties,
                                    SecurityContextAccessor securityContextAccessor) {
        this.loginService = loginService;
        this.verificationCodeService = verificationCodeService;
        this.securityProperties = securityProperties;
        this.securityContextAccessor = securityContextAccessor;
        WebCookiePolicy.validate(securityProperties);
    }

    /**
     * 处理内部业务逻辑（{@code login}）。
     */
    @Audit(value = "'用户[' + #params.username + ']进行登陆'", category = AuditCategory.SECURITY)
    @Encrypt(response = false)
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/login", version = "1.0.0")
    public TokenVO login(@Validated @RequestBody LoginFrom params, HttpServletRequest request,
                         HttpServletResponse response) {
        ClientType clientType = AuthenticationWebUtils.clientType(request);
        TokenVO token = loginService.login(params, clientType);
        return AuthenticationWebUtils.writeWebToken(response, token, securityProperties, clientType);
    }

    /**
     * 处理内部业务逻辑（{@code verifyMfa}）。
     */
    @Audit(value = "'完成 MFA 登录验证'", category = AuditCategory.SECURITY)
    @Encrypt(response = false)
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/mfa/verify", version = "1.0.0")
    public TokenVO verifyMfa(@Valid @RequestBody MfaVerifyFrom params, HttpServletRequest request,
                             HttpServletResponse response) {
        ClientType clientType = AuthenticationWebUtils.clientType(request);
        TokenVO token = loginService.verifyMfa(params.getChallengeId(), params.getCode(), clientType);
        return AuthenticationWebUtils.writeWebToken(response, token, securityProperties, clientType);
    }

    /**
     * 处理内部业务逻辑（{@code completeMfaEnrollment}）。
     */
    @Audit(value = "'完成首次 MFA 登记登录'", category = AuditCategory.SECURITY)
    @Encrypt(response = false)
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/mfa/complete", version = "1.0.0")
    public TokenVO completeMfaEnrollment(@Valid @RequestBody MfaCompleteFrom params,
                                         HttpServletRequest request, HttpServletResponse response) {
        ClientType clientType = AuthenticationWebUtils.clientType(request);
        TokenVO token = loginService.completeMfaEnrollment(params.getChallengeId(), clientType);
        return AuthenticationWebUtils.writeWebToken(response, token, securityProperties, clientType);
    }

    @Audit(value = "'用户登出系统'", category = AuditCategory.SECURITY)
    @PostMapping(value = "/logout", version = "1.0.0")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("permitAll()")
    public void logout(@RequestBody(required = false) RefreshTokenFrom params, HttpServletRequest request,
                       HttpServletResponse response) {
        ClientType clientType = AuthenticationWebUtils.clientType(request);
        String refreshToken = params != null ? params.getRefreshToken() : null;
        if (AuthenticationWebUtils.isWebClient(clientType)) {
            AuthenticationWebUtils.validateCsrf(request, securityProperties);
            if (refreshToken == null || refreshToken.isBlank()) {
                refreshToken = AuthenticationWebUtils.readCookie(request, securityProperties.getRefreshCookieName());
            }
        }
        loginService.logout(securityContextAccessor.currentToken(), refreshToken, clientType);
        if (AuthenticationWebUtils.isWebClient(clientType)) {
            AuthenticationWebUtils.clearWebCookies(response, securityProperties);
        }
    }

    /**
     * 更新或推进目标状态（{@code sendSms}）。
     */
    @Audit(value = "'发送短信验证码'", category = AuditCategory.SECURITY)
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/sms", version = "1.0.0")
    @ResponseStatus(HttpStatus.OK)
    public void sendSms(@Validated @RequestBody SmsCodeFrom params) {
        verificationCodeService.sendSmsCode(params.getPhone());
    }

    /**
     * 更新或推进目标状态（{@code sendEmail}）。
     */
    @Audit(value = "'发送邮箱验证码'", category = AuditCategory.SECURITY)
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/email", version = "1.0.0")
    @ResponseStatus(HttpStatus.OK)
    public void sendEmail(@Validated @RequestBody EmailCodeFrom params) {
        verificationCodeService.sendEmailCode(params.getEmail());
    }

    /**
     * 更新或推进目标状态（{@code sendBindingSms}）。
     */
    @Audit(value = "'发送绑定手机号验证码'", category = AuditCategory.SECURITY)
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/bind/sms", version = "1.0.0")
    @ResponseStatus(HttpStatus.OK)
    public void sendBindingSms(@Validated @RequestBody SmsCodeFrom params) {
        verificationCodeService.sendBindingSmsCode(params.getPhone());
    }

    /**
     * 更新或推进目标状态（{@code sendBindingEmail}）。
     */
    @Audit(value = "'发送绑定邮箱验证码'", category = AuditCategory.SECURITY)
    @PreAuthorize("isAuthenticated()")
    @PostMapping(value = "/bind/email", version = "1.0.0")
    @ResponseStatus(HttpStatus.OK)
    public void sendBindingEmail(@Validated @RequestBody EmailCodeFrom params) {
        verificationCodeService.sendBindingEmailCode(params.getEmail());
    }

    @Audit(value = "'刷新token'", category = AuditCategory.SECURITY)
    @Encrypt(response = false)
    @PreAuthorize("permitAll()")
    @PostMapping(value = "/refresh", version = "1.0.0")
    public TokenVO refresh(@RequestBody(required = false) RefreshTokenFrom params, HttpServletRequest request,
                           HttpServletResponse response) {
        ClientType clientType = AuthenticationWebUtils.clientType(request);
        if (AuthenticationWebUtils.isWebClient(clientType)) {
            AuthenticationWebUtils.validateCsrf(request, securityProperties);
        }
        String cookieRefreshToken = AuthenticationWebUtils.readCookie(request, securityProperties.getRefreshCookieName());
        String refreshToken = AuthenticationWebUtils.isWebClient(clientType)
                ? cookieRefreshToken
                : params != null ? params.getRefreshToken() : null;
        TokenVO token = loginService.refresh(refreshToken, clientType);
        AuthenticationWebUtils.issueWebCookies(response, token.getRefreshToken(), securityProperties, clientType);
        if (AuthenticationWebUtils.isWebClient(clientType)) {
            token.setRefreshToken(null);
        }
        return token;
    }
}
