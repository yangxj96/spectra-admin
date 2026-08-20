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

package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStatusVO;
import com.devops00.spectra.core.security.initialization.service.SystemInitializationService;
import com.devops00.spectra.core.system.constant.SystemConfigKeys;
import com.devops00.spectra.core.system.service.ConfiguredService;
import com.devops00.spectra.framework.configure.mvc.crypto.CryptoKeyManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SystemBootstrapServiceImplTest {

    @Mock
    private ConfiguredService configuredService;

    @Mock
    private CryptoKeyManager cryptoKeyManager;

    @Mock
    private SystemInitializationService initializationService;

    @InjectMocks
    private SystemBootstrapServiceImpl systemBootstrapService;

    @Test
    void shouldAssembleConfiguredBootstrapValues() {
        when(configuredService.findValue(SystemConfigKeys.SYSTEM_NAME)).thenReturn(Optional.of("DevOps00"));
        when(configuredService.findValue(SystemConfigKeys.SYSTEM_SHORT_NAME)).thenReturn(Optional.of("DevOps"));
        when(configuredService.findValue(SystemConfigKeys.SYSTEM_LOGO)).thenReturn(Optional.of("/logo.png"));
        when(configuredService.findValue(SystemConfigKeys.SYSTEM_DEFAULT_LOCALE)).thenReturn(Optional.of("en-US"));
        when(configuredService.findValue(SystemConfigKeys.SYSTEM_DEFAULT_TIMEZONE)).thenReturn(Optional.of("UTC"));
        when(cryptoKeyManager.isEnabled()).thenReturn(true);
        when(cryptoKeyManager.getServerPublicKeyBase64()).thenReturn("server-public-key");
        when(initializationService.status()).thenReturn(new SystemInitializationStatusVO("INITIALIZED", true, false));

        var result = systemBootstrapService.get();

        assertEquals("DevOps00", result.getSystem().getName());
        assertEquals("DevOps", result.getSystem().getShortName());
        assertEquals("/logo.png", result.getSystem().getLogo());
        assertEquals("en-US", result.getSystem().getDefaultLocale());
        assertEquals("UTC", result.getSystem().getDefaultTimezone());
        assertTrue(result.getCrypto().getEnabled());
        assertEquals("server-public-key", result.getCrypto().getServerPublicKey());
        assertTrue(result.getInitialization().initialized());
    }

    @Test
    void shouldUseSafeDefaultsBeforeInitialization() {
        when(configuredService.findValue(anyString())).thenReturn(Optional.empty());
        when(cryptoKeyManager.isEnabled()).thenReturn(false);
        when(initializationService.status()).thenReturn(new SystemInitializationStatusVO("UNINITIALIZED", false, true));

        var result = systemBootstrapService.get();

        assertEquals("Spectra", result.getSystem().getName());
        assertEquals("Spectra", result.getSystem().getShortName());
        assertEquals("", result.getSystem().getLogo());
        assertEquals("zh-CN", result.getSystem().getDefaultLocale());
        assertEquals("Asia/Shanghai", result.getSystem().getDefaultTimezone());
        assertFalse(result.getCrypto().getEnabled());
        assertTrue(result.getInitialization().initializationRequired());
    }
}
