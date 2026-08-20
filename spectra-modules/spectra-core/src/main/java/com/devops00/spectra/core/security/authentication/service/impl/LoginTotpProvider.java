/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.PasswordCredentialService;
import com.devops00.spectra.core.security.authentication.mfa.service.MfaService;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.security.base.constant.LoginType;
import com.devops00.spectra.security.base.exception.LoginException;
import com.devops00.spectra.security.base.strategy.tokens.TotpAuthenticationToken;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Service;

import java.util.HashMap;

/** TOTP/Recovery Code 登录及 step-up 认证 Provider。 */
@Service
public class LoginTotpProvider implements AuthenticationProvider {

    private final AuthenticationIdentityService identityService;
    private final PasswordCredentialService passwordCredentialService;
    private final UserService userService;
    private final SecurityUserHelper securityUserHelper;
    private final MfaService mfaService;

    public LoginTotpProvider(AuthenticationIdentityService identityService, PasswordCredentialService passwordCredentialService,
                             UserService userService, SecurityUserHelper securityUserHelper, MfaService mfaService) {
        this.identityService = identityService;
        this.passwordCredentialService = passwordCredentialService;
        this.userService = userService;
        this.securityUserHelper = securityUserHelper;
        this.mfaService = mfaService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        if (!(authentication instanceof TotpAuthenticationToken token)) {
            return null;
        }
        if (!(token.getPrincipal() instanceof String identifier)
                || identifier.isBlank()
                || !(token.getCredentials() instanceof String code)
                || code.isBlank()) {
            throw new BadCredentialsException("账号或 MFA 验证码不能为空");
        }
        AuthenticationIdentity identity = identityService.findPasswordIdentity(identifier);
        if (identity == null || identity.getUserId() == null) {
            throw new LoginException("账号或 MFA 验证码错误");
        }
        boolean verified = mfaService.verifyTotp(identity.getUserId(), code)
                || mfaService.consumeRecoveryCode(identity.getUserId(), code);
        if (!verified) {
            throw new LoginException("账号或 MFA 验证码错误");
        }
        var credential = passwordCredentialService.getByUserId(identity.getUserId());
        var user = userService.getById(identity.getUserId());
        if (credential == null || user == null) {
            throw new LoginException("账号当前不可用");
        }
        var securityUser = securityUserHelper.toSecurityUser(LoginType.PASSWORD, identity, credential, user);
        var extraData = new HashMap<String, Object>();
        extraData.put("mfaVerified", true);
        extraData.put("authenticationAssurance", "AAL2");
        securityUser.setExtraData(extraData);
        return new UsernamePasswordAuthenticationToken(securityUser, null, securityUser.getAuthorities());
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return TotpAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
