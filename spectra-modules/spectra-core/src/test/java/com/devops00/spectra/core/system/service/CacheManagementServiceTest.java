/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.service;

import com.devops00.spectra.common.constant.ClientType;
import com.devops00.spectra.common.port.security.SecurityReplayNonceAdminPort;
import com.devops00.spectra.common.port.security.SecuritySessionQueryPort;
import com.devops00.spectra.common.port.security.SecuritySessionRevocationPort;
import com.devops00.spectra.common.port.security.SecurityVerificationAttemptStore;
import com.devops00.spectra.common.port.security.SecurityVerificationCodeStore;
import com.devops00.spectra.core.system.cache.CacheInvalidationCoordinator;
import com.devops00.spectra.core.system.cache.CacheManagementResult;
import com.devops00.spectra.core.system.cache.CacheRegionRegistry;
import com.devops00.spectra.core.system.javabean.from.SecurityNonceGlobalInvalidateFrom;
import com.devops00.spectra.core.system.javabean.from.SecuritySessionRevokeFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityVerificationClearFrom;
import com.devops00.spectra.core.system.javabean.from.SecurityVerificationType;
import com.devops00.spectra.core.system.service.impl.CacheManagementServiceImpl;
import com.devops00.spectra.framework.security.session.lifecycle.SecurityLoginFailureTracker;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CacheManagementServiceTest {

    @Test
    void shouldDelegateSessionRevocationWithoutAcceptingAPlaintextToken() {
        var revocation = mock(SecuritySessionRevocationPort.class);
        var service = service(revocation, mock(SecurityReplayNonceAdminPort.class));
        UUID userId = UUID.randomUUID();

        service.revokeSession(new SecuritySessionRevokeFrom(userId, ClientType.WEB, "maintenance", true));

        verify(revocation).revokeUserClientSessions(userId, ClientType.WEB);
    }

    @Test
    void shouldUseSecurityNoncePortAndNeverOrdinaryCacheClearForGlobalNonceInvalidation() {
        var coordinator = mock(CacheInvalidationCoordinator.class);
        var nonce = mock(SecurityReplayNonceAdminPort.class);
        when(nonce.invalidateAll()).thenReturn(new SecurityReplayNonceAdminPort.Result("ALL", 3L, 100L,
                "SUCCEEDED"));
        var service = service(mock(SecuritySessionRevocationPort.class), nonce, coordinator);

        service.invalidateAllNonces(new SecurityNonceGlobalInvalidateFrom("maintenance", "INVALIDATE-NONCES"));

        verify(nonce).invalidateAll();
        verify(coordinator, never()).clear(any(), any(Boolean.class), any(Boolean.class), any());
    }

    @Test
    void shouldClearVerificationCodeAndItsAttemptCounterThroughTypedPrefixes() {
        var codes = mock(SecurityVerificationCodeStore.class);
        var attempts = mock(SecurityVerificationAttemptStore.class);
        var service = service(mock(SecuritySessionRevocationPort.class), mock(SecurityReplayNonceAdminPort.class),
                mock(CacheInvalidationCoordinator.class), codes, attempts);

        service.clearVerification(new SecurityVerificationClearFrom(
                SecurityVerificationType.LOGIN_SMS, "13800000000", true, "maintenance", true));

        verify(codes).delete(eq("security:verification:login:sms:13800000000"));
        verify(attempts).delete(eq("security:verification:login:sms:attempts:13800000000"));
    }

    private static CacheManagementServiceImpl service(SecuritySessionRevocationPort revocation,
                                                      SecurityReplayNonceAdminPort nonce) {
        return service(revocation, nonce, mock(CacheInvalidationCoordinator.class));
    }

    private static CacheManagementServiceImpl service(SecuritySessionRevocationPort revocation,
                                                      SecurityReplayNonceAdminPort nonce,
                                                      CacheInvalidationCoordinator coordinator) {
        return service(revocation, nonce, coordinator, mock(SecurityVerificationCodeStore.class),
                mock(SecurityVerificationAttemptStore.class));
    }

    private static CacheManagementServiceImpl service(SecuritySessionRevocationPort revocation,
                                                      SecurityReplayNonceAdminPort nonce,
                                                      CacheInvalidationCoordinator coordinator,
                                                      SecurityVerificationCodeStore codes,
                                                      SecurityVerificationAttemptStore attempts) {
        return new CacheManagementServiceImpl(mock(CacheRegionRegistry.class), coordinator,
                mock(SecuritySessionQueryPort.class), revocation, codes, attempts,
                mock(SecurityLoginFailureTracker.class), nonce);
    }
}
