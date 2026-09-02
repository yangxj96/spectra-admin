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

package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.audit.RequestCorrelationContext;
import com.devops00.spectra.core.security.authentication.service.LoginService;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.change.SecurityAuthenticationPort;
import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.exception.LoginException;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.from.LoginFrom;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/**
 * 认证登录用例编排。
 *
 * <p>这里负责主认证、登录失败策略和正式会话签发；HTTP Cookie、CSRF 和响应格式由
 * authentication.controller.AuthenticationController 负责。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Service
public class LoginServiceImpl implements LoginService {

    private final LoginDispatcher loginDispatcher;
    private final ObjectProvider<SecurityAuditWriter> securityAuditWriterProvider;
    private final SecurityAuthenticationPort securityAuthenticationPort;
    private final SecurityContextAccessor securityContextAccessor;

    public LoginServiceImpl(LoginDispatcher loginDispatcher, ObjectProvider<SecurityAuditWriter> securityAuditWriterProvider,
                            SecurityAuthenticationPort securityAuthenticationPort,
                            SecurityContextAccessor securityContextAccessor) {
        this.loginDispatcher = loginDispatcher;
        this.securityAuditWriterProvider = securityAuditWriterProvider;
        this.securityAuthenticationPort = securityAuthenticationPort;
        this.securityContextAccessor = securityContextAccessor;
    }

    @Override
    public TokenVO login(LoginFrom params, ClientType clientType) {
        String username = params.getUsername() != null ? params.getUsername() : "";
        if (securityAuthenticationPort.isLockedOut(username)) {
            audit("AUTH_LOGIN_FAILED", null, clientType, "LOCKED_OUT");
            throw new LoginException("账号已锁定，请稍后再试");
        }

        try {
            Authentication authentication = loginDispatcher.authenticate(params);
            if (!(authentication.getPrincipal() instanceof SecurityUser user)) {
                throw new UsernameNotFoundException("未找到用户");
            }
            return issueAuthenticatedToken(user, clientType);
        } catch (BadCredentialsException exception) {
            securityAuthenticationPort.recordLoginFail(username);
            audit("AUTH_LOGIN_FAILED", null, clientType, "BAD_CREDENTIALS");
            throw exception;
        }
    }

    @Override
    public void logout(String token, String refreshToken, ClientType clientType) {
        if (token != null && !token.isBlank()) {
            securityAuthenticationPort.logout(token);
        }
        if (refreshToken != null && !refreshToken.isBlank()) {
            securityAuthenticationPort.logoutByRefreshToken(refreshToken);
        }
        audit("AUTH_LOGOUT", securityContextAccessor.currentUserId(), clientType, null);
    }

    @Override
    public TokenVO refresh(String refreshToken, ClientType clientType) {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new LoginException("刷新token不能为空");
        }
        try {
            TokenVO token = securityAuthenticationPort.refreshByRefreshToken(refreshToken);
            audit("TOKEN_REFRESH_SUCCEEDED", securityContextAccessor.currentUserId(), clientType, null);
            return token;
        } catch (RuntimeException exception) {
            audit("TOKEN_REFRESH_FAILED", securityContextAccessor.currentUserId(), clientType,
                    "REFRESH_REJECTED");
            throw exception;
        }
    }

    /**
     * 判断条件是否满足（{@code issueAuthenticatedToken}）。
     */
    private TokenVO issueAuthenticatedToken(SecurityUser user, ClientType clientType) {
        SecurityContextHolder.getContext()
                .setAuthentication(
                        new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                                user, null, user.getAuthorities()));
        securityAuthenticationPort.clearLoginFail(user.getUsername());
        audit("AUTH_LOGIN_SUCCEEDED", user.getId(), clientType, null);
        return securityAuthenticationPort.login(user);
    }

    /**
     * 处理内部业务逻辑（{@code audit}）。
     */
    private void audit(String eventType, UUID operatorId, ClientType clientType, String reason) {
        SecurityAuditWriter securityAuditWriter = securityAuditWriterProvider.getIfAvailable();
        if (securityAuditWriter == null) {
            return;
        }
        AuditResult result = eventType.endsWith("_FAILED") ? AuditResult.FAILED : AuditResult.SUCCEEDED;
        securityAuditWriter.append(new SecurityAuditEvent(UUID.randomUUID(), eventType, operatorId, null,
                clientType.name(), null, null, Map.of(), Map.of(), reason, null, result,
                RequestCorrelationContext.current().correlationId()));
    }
}
