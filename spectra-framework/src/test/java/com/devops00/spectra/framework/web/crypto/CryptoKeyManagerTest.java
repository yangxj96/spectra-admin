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

package com.devops00.spectra.framework.web.crypto;

import com.devops00.spectra.common.exception.EncryptException;
import com.devops00.spectra.common.utils.RSAUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.security.KeyPair;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 加密密钥运行态和 fail-closed 行为测试。 */
class CryptoKeyManagerTest {

    private static KeyPair serverKeyPair;
    private static KeyPair clientKeyPair;

    @BeforeAll
    static void setUpKeys() throws Exception {
        serverKeyPair = RSAUtils.generateKeyPair();
        clientKeyPair = RSAUtils.generateKeyPair();
    }

    @Test
    void shouldKeepExplicitlyDisabledState() {
        var manager = new CryptoKeyManager(jdbc(Map.of("crypto.enabled", "false")));

        manager.refresh();

        assertEquals(CryptoKeyManager.State.DISABLED, manager.getState());
        assertEquals(false, manager.isConfiguredEnabled());
        assertNull(manager.getServerPrivateKey());
        assertNull(manager.getClientPublicKey());
    }

    @Test
    void shouldExposeReadyStateOnlyAfterAllKeysAreLoaded() {
        var manager = new CryptoKeyManager(jdbc(readyConfig()));

        manager.refresh();

        assertEquals(CryptoKeyManager.State.READY, manager.getState());
        assertEquals(true, manager.isEnabled());
        assertEquals(true, manager.isConfiguredEnabled());
    }

    @Test
    void shouldHidePartialKeysWhenConfigurationIsIncomplete() {
        var config = readyConfig();
        config.remove("crypto.client.private-key");
        var manager = new CryptoKeyManager(jdbc(config));

        manager.refresh();

        assertEquals(CryptoKeyManager.State.UNAVAILABLE, manager.getState());
        assertEquals(false, manager.isEnabled());
        assertNull(manager.getServerPrivateKey());
        assertNull(manager.getClientPublicKey());
        assertThrows(EncryptException.class, manager::requireReady);
    }

    @Test
    void shouldConvertConfigurationReadFailureToUnavailableState() {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(), eq(String.class), any(Object[].class)))
                .thenThrow(new DataAccessResourceFailureException("database unavailable"));
        var manager = new CryptoKeyManager(jdbc);

        manager.refresh();

        assertEquals(CryptoKeyManager.State.UNAVAILABLE, manager.getState());
        assertEquals(false, manager.isEnabled());
        assertEquals(true, manager.isConfiguredEnabled());
    }

    @Test
    void shouldRejectMalformedEnabledValueAsConfigurationFailure() {
        var manager = new CryptoKeyManager(jdbc(Map.of("crypto.enabled", "yes")));

        manager.refresh();

        assertEquals(CryptoKeyManager.State.UNAVAILABLE, manager.getState());
    }

    private static JdbcTemplate jdbc(Map<String, String> values) {
        var jdbc = mock(JdbcTemplate.class);
        when(jdbc.queryForList(anyString(), eq(String.class), any(Object[].class))).thenAnswer(invocation -> {
            String value = values.get(invocation.getArgument(2, String.class));
            return value == null ? List.of() : List.of(value);
        });
        return jdbc;
    }

    private static Map<String, String> readyConfig() {
        var config = new HashMap<String, String>();
        config.put("crypto.enabled", "true");
        config.put("crypto.server.public-key", RSAUtils.getPublicKeyBase64(serverKeyPair.getPublic()));
        config.put("crypto.server.private-key", RSAUtils.getPrivateKeyBase64(serverKeyPair.getPrivate()));
        config.put("crypto.client.public-key", RSAUtils.getPublicKeyBase64(clientKeyPair.getPublic()));
        config.put("crypto.client.private-key", RSAUtils.getPrivateKeyBase64(clientKeyPair.getPrivate()));
        return config;
    }
}
