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

import com.devops00.spectra.core.security.authentication.service.LoginService;
import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import com.devops00.spectra.security.base.audit.SecurityAuditWriter;
import com.devops00.spectra.security.base.change.SecurityAuthenticationPort;
import com.devops00.spectra.security.base.constant.ClientType;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.exception.LoginException;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.holder.SecurityUserLoader;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import com.devops00.spectra.security.base.javabean.from.LoginFrom;
import com.devops00.spectra.security.base.javabean.vo.TokenVO;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort;
import com.devops00.spectra.security.base.mfa.SecurityMfaChallengePort.MfaLoginChallenge;
import com.devops00.spectra.security.base.mfa.SecurityMfaVerifier;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 认证登录用例编排。
 *
 * <p>这里负责主认证、登录失败策略、MFA 预认证和正式会话签发；HTTP Cookie、CSRF
 * 和响应格式由 authentication.controller.AuthenticationController 负责。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/21
 */
@Service
public class LoginServiceImpl implements LoginService {

    private final LoginDispatcher loginDispatcher;
    private final SecurityProperties securityProperties;
    private final ObjectProvider<SecurityAuditWriter> securityAuditWriterProvider;
    private final SecurityAuthenticationPort securityAuthenticationPort;
    private final SecurityContextAccessor securityContextAccessor;
    private final ObjectProvider<SecurityMfaChallengePort> mfaChallengeProvider;
    private final ObjectProvider<SecurityMfaVerifier> mfaVerifierProvider;
    private final ObjectProvider<SecurityUserLoader> securityUserLoaderProvider;

    public LoginServiceImpl(LoginDispatcher loginDispatcher, SecurityProperties securityProperties,
                            ObjectProvider<SecurityAuditWriter> securityAuditWriterProvider,
                            SecurityAuthenticationPort securityAuthenticationPort,
                            SecurityContextAccessor securityContextAccessor,
                            ObjectProvider<SecurityMfaChallengePort> mfaChallengeProvider,
                            ObjectProvider<SecurityMfaVerifier> mfaVerifierProvider,
                            ObjectProvider<SecurityUserLoader> securityUserLoaderProvider) {
        this.loginDispatcher = loginDispatcher;
        this.securityProperties = securityProperties;
        this.securityAuditWriterProvider = securityAuditWriterProvider;
        this.securityAuthenticationPort = securityAuthenticationPort;
        this.securityContextAccessor = securityContextAccessor;
        this.mfaChallengeProvider = mfaChallengeProvider;
        this.mfaVerifierProvider = mfaVerifierProvider;
        this.securityUserLoaderProvider = securityUserLoaderProvider;
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
            if (requiresDevOpsMfa(user) && params.getType() != LoginType.PASSWORD) {
                throw new LoginException("DEV_OPS 必须先完成账号密码验证");
            }
            if (requiresDevOpsMfa(user)) {
                return createMfaChallenge(user, clientType);
            }
            return issueAuthenticatedToken(user, clientType);
        } catch (BadCredentialsException exception) {
            securityAuthenticationPort.recordLoginFail(username);
            audit("AUTH_LOGIN_FAILED", null, clientType, "BAD_CREDENTIALS");
            throw exception;
        }
    }

    @Override
    public TokenVO verifyMfa(String challengeId, String code, ClientType clientType) {
        MfaLoginChallenge challenge = requireChallenge(challengeId);
        if (challenge.enrollmentRequired() || challenge.enrollmentCompleted()) {
            throw new LoginException("请先完成 MFA 登记");
        }
        validateChallengeClient(challenge, clientType);

        SecurityMfaVerifier verifier = requireMfaVerifier();
        boolean verified = verifier.verifyTotp(challenge.userId(), code)
                || verifier.consumeRecoveryCode(challenge.userId(), code);
        if (!verified) {
            if (!requireChallengePort().recordFailure(challenge.id())) {
                throw new LoginException("MFA 挑战已失效");
            }
            throw new LoginException("MFA 验证码错误");
        }
        if (!requireChallengePort().consume(challenge.id())) {
            throw new LoginException("MFA 挑战已失效");
        }
        SecurityUser user = loadUser(challenge.userId());
        markMfaVerified(user);
        return issueAuthenticatedToken(user, clientType);
    }

    @Override
    public TokenVO completeMfaEnrollment(String challengeId, ClientType clientType) {
        MfaLoginChallenge challenge = requireChallenge(challengeId);
        if (!challenge.enrollmentCompleted()) {
            throw new LoginException("MFA 尚未完成登记");
        }
        validateChallengeClient(challenge, clientType);
        if (!requireChallengePort().consume(challenge.id())) {
            throw new LoginException("MFA 挑战已失效");
        }
        SecurityUser user = loadUser(challenge.userId());
        markMfaVerified(user);
        return issueAuthenticatedToken(user, clientType);
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
     * 创建或构建目标数据（{@code createMfaChallenge}）。
     */
    private TokenVO createMfaChallenge(SecurityUser user, ClientType clientType) {
        SecurityMfaVerifier verifier = requireMfaVerifier();
        boolean enrollmentRequired = !verifier.hasActiveTotp(user.getId());
        MfaLoginChallenge challenge = requireChallengePort().create(user.getId(), user.getUsername(), clientType,
                enrollmentRequired);
        SecurityContextHolder.clearContext();
        audit("AUTH_MFA_REQUIRED", user.getId(), clientType,
                enrollmentRequired ? "MFA_ENROLLMENT_REQUIRED" : "MFA_VERIFICATION_REQUIRED");
        return TokenVO.builder()
                .loginType(LoginType.PASSWORD)
                .id(user.getId())
                .username(user.getEmail())
                .mfaRequired(true)
                .mfaEnrollmentRequired(enrollmentRequired)
                .mfaChallengeId(challenge.id())
                .mfaChallengeExpiresAt(challenge.expiresAt())
                .build();
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
     * 校验并确保数据满足当前约束（{@code requiresDevOpsMfa}）。
     */
    private boolean requiresDevOpsMfa(SecurityUser user) {
        return securityProperties.isMfaRequiredForDevOps()
                && user.getAuthorities()
                        .stream()
                        .anyMatch(authority -> "ROLE_DEV_OPS".equals(authority.getAuthority()));
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireChallenge}）。
     */
    private MfaLoginChallenge requireChallenge(String challengeId) {
        MfaLoginChallenge challenge = requireChallengePort().find(challengeId);
        if (challenge == null) {
            throw new LoginException("MFA 挑战不存在或已过期");
        }
        return challenge;
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireChallengePort}）。
     */
    private SecurityMfaChallengePort requireChallengePort() {
        var mfaChallengePort = mfaChallengeProvider.getIfAvailable();
        if (mfaChallengePort == null) {
            throw new IllegalStateException("MFA 挑战存储未配置");
        }
        return mfaChallengePort;
    }

    /**
     * 校验并确保数据满足当前约束（{@code requireMfaVerifier}）。
     */
    private SecurityMfaVerifier requireMfaVerifier() {
        var mfaVerifier = mfaVerifierProvider.getIfAvailable();
        if (mfaVerifier == null) {
            throw new IllegalStateException("MFA 校验服务未配置");
        }
        return mfaVerifier;
    }

    /**
     * 查询或获取目标数据（{@code loadUser}）。
     */
    private SecurityUser loadUser(UUID userId) {
        var securityUserLoader = securityUserLoaderProvider.getIfAvailable();
        if (securityUserLoader == null) {
            throw new IllegalStateException("安全主体加载器未配置");
        }
        SecurityUser user = securityUserLoader.load(userId);
        if (user == null) {
            throw new LoginException("账号当前不可用");
        }
        return user;
    }

    /**
     * 更新或推进目标状态（{@code markMfaVerified}）。
     */
    private void markMfaVerified(SecurityUser user) {
        Map<String, Object> extraData = new HashMap<>();
        extraData.put("mfaVerified", true);
        extraData.put("authenticationAssurance", "AAL2");
        user.setExtraData(extraData);
    }

    /**
     * 校验并确保数据满足当前约束（{@code validateChallengeClient}）。
     */
    private void validateChallengeClient(MfaLoginChallenge challenge, ClientType clientType) {
        if (challenge.clientType() != clientType) {
            throw new LoginException("MFA 挑战客户端不匹配");
        }
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
                clientType.name(), null, null, Map.of(), Map.of(), reason, null, result, null));
    }
}
