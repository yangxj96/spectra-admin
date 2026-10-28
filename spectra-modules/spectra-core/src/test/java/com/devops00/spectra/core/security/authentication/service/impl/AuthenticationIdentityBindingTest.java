/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.core.security.authentication.constant.LoginType;
import com.devops00.spectra.core.security.authentication.identity.AuthenticationIdentifierHash;
import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.javabean.enums.AuthenticationIdentityState;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.security.root.LastEffectiveDevOpsGuard;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AuthenticationIdentityBindingTest {

    private static final String SECRET = "synthetic-binding-secret";

    private final SecurityVerificationCodeStore codes = mock(SecurityVerificationCodeStore.class);
    private final SecurityProperties properties = new SecurityProperties();
    private final SecretRuntimeService secrets = mock(SecretRuntimeService.class);
    private final AuthenticationIdentityService identities = mock(AuthenticationIdentityService.class);
    private final UserContactService contacts = mock(UserContactService.class);
    private final LastEffectiveDevOpsGuard guard = mock(LastEffectiveDevOpsGuard.class);
    private final SecuritySessionRevocationPort sessions = mock(SecuritySessionRevocationPort.class);

    @Test
    void bindingPhoneKeepsOnlyOneActiveSmsIdentity() {
        var userId = UUID.randomUUID();
        var phone = "13800000001";
        var identity = new AuthenticationIdentity();
        identity.setId(UUID.randomUUID());
        when(secrets.requireActiveValue("security.verification-code-hmac")).thenReturn(SECRET);
        when(codes.consumeWithinAttemptBudget(anyString(), anyString(), anyString(), anyInt()))
                .thenReturn(SecurityVerificationCodeStore.VerificationResult.MATCHED);
        when(identities.createIdentity(userId, LoginType.SMS.name(), phone)).thenReturn(identity);

        service().bindPhone(userId, phone, "123456");

        verify(identities).revokeOtherActiveByUserIdAndMethod(userId, LoginType.SMS.name(), identity.getId());
        verify(contacts).upsertVerified(userId, UserContactService.PHONE, phone);
    }

    @Test
    void unbindRevokesOnlyTheContactMatchingTheIdentity() {
        var userId = UUID.randomUUID();
        var phone = "13800000001";
        var identity = new AuthenticationIdentity();
        identity.setId(UUID.randomUUID());
        identity.setMethodCode(LoginType.SMS.name());
        identity.setState(AuthenticationIdentityState.ACTIVE.name());
        identity.setIdentifierHash(AuthenticationIdentifierHash.digest(phone));
        var contact = new UserContact();
        contact.setUserId(userId);
        contact.setContactType(UserContactService.PHONE);
        contact.setContactValue(phone);
        when(identities.listByUserId(userId)).thenReturn(List.of(identity, otherActiveIdentity()));
        when(contacts.listActiveByUserId(userId)).thenReturn(List.of(contact));

        service().unbind(userId, identity.getId());

        verify(identities).revokeByUserIdAndId(userId, identity.getId());
        verify(contacts).revokeByUserIdAndTypeAndValue(userId, UserContactService.PHONE, phone);
        verify(sessions).revokeUserSessions(userId);
    }

    @Test
    void unbindDoesNotRevokeAnotherActiveContact() {
        var userId = UUID.randomUUID();
        var phone = "13800000001";
        var identity = new AuthenticationIdentity();
        identity.setId(UUID.randomUUID());
        identity.setMethodCode(LoginType.SMS.name());
        identity.setState(AuthenticationIdentityState.ACTIVE.name());
        identity.setIdentifierHash(AuthenticationIdentifierHash.digest(phone));
        var newerContact = new UserContact();
        newerContact.setUserId(userId);
        newerContact.setContactType(UserContactService.PHONE);
        newerContact.setContactValue("13900000002");
        when(identities.listByUserId(userId)).thenReturn(List.of(identity, otherActiveIdentity()));
        when(contacts.listActiveByUserId(userId)).thenReturn(List.of(newerContact));

        service().unbind(userId, identity.getId());

        verify(contacts, never()).revokeByUserIdAndTypeAndValue(any(), anyString(), anyString());
        verify(sessions).revokeUserSessions(userId);
    }

    private AuthenticationIdentityBindingServiceImpl service() {
        return new AuthenticationIdentityBindingServiceImpl(codes, properties, secrets, identities, contacts, guard,
                sessions);
    }

    private static AuthenticationIdentity otherActiveIdentity() {
        var identity = new AuthenticationIdentity();
        identity.setId(UUID.randomUUID());
        identity.setMethodCode(LoginType.PASSWORD.name());
        identity.setState(AuthenticationIdentityState.ACTIVE.name());
        return identity;
    }
}
