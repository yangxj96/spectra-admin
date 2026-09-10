/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.core.security.secret.service;

import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.core.system.javabean.enums.ConfiguredValueType;
import com.devops00.spectra.core.security.secret.service.impl.SecretManagementSettingsServiceImpl;
import com.devops00.spectra.framework.web.crypto.CryptoKeyManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.when;

/** 密钥管理页面接口加解密开关的服务契约测试。 */
@ExtendWith(MockitoExtension.class)
class SecretManagementSettingsServiceTest {

    @Mock
    private ConfiguredService configuredService;
    @Mock
    private CryptoKeyManager cryptoKeyManager;

    private SecretManagementSettingsServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new SecretManagementSettingsServiceImpl(configuredService, cryptoKeyManager);
    }

    @Test
    void enablingRequiresReadyCryptoKeysAndPersistsSystemConfig() {
        when(cryptoKeyManager.isKeyMaterialReady()).thenReturn(true);
        when(cryptoKeyManager.isEnabled()).thenReturn(true);
        when(cryptoKeyManager.getState()).thenReturn(CryptoKeyManager.State.READY);
        when(configuredService.findValue("crypto.enabled")).thenReturn(Optional.of("true"));

        var result = service.setCryptoEnabled(true);

        verify(configuredService).upsert("crypto.enabled", "true", ConfiguredValueType.BOOL,
                "密钥管理页面配置的接口加解密开关");
        verify(cryptoKeyManager).refresh();
        assertEquals(true, result.enabled());
        assertEquals(true, result.ready());
    }

    @Test
    void enablingUnavailableCryptoKeysIsRejectedWithoutPersistingSwitch() {
        when(cryptoKeyManager.isKeyMaterialReady()).thenReturn(false);

        assertThrows(RuntimeException.class, () -> service.setCryptoEnabled(true));

        verify(configuredService, never()).upsert("crypto.enabled", "true", ConfiguredValueType.BOOL,
                "密钥管理页面配置的接口加解密开关");
        verify(cryptoKeyManager, never()).refresh();
    }

    @Test
    void runtimeRefreshIsDeferredUntilTransactionCommits() {
        when(configuredService.findValue("crypto.enabled")).thenReturn(Optional.of("false"));

        TransactionSynchronizationManager.initSynchronization();
        try {
            service.setCryptoEnabled(false);

            verify(cryptoKeyManager, never()).refresh();

            TransactionSynchronization synchronization = TransactionSynchronizationManager
                    .getSynchronizations()
                    .getFirst();
            synchronization.afterCommit();

            verify(cryptoKeyManager).refresh();
        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}
