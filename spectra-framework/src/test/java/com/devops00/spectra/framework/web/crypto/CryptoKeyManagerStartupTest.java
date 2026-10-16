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

import com.devops00.spectra.common.config.SystemConfigValueProvider;
import com.devops00.spectra.common.port.security.RuntimeSecretProvider;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证启用加解密但密钥未就绪时启动失败，明确关闭时仍可启动。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class CryptoKeyManagerStartupTest {

    @Test
    void enabledButKeysMissingFailsStartup() {
        RuntimeSecretProvider secrets = mock();
        when(secrets.findActive(anyString())).thenReturn(Optional.empty());
        SystemConfigValueProvider config = mock();
        when(config.find("crypto.enabled")).thenReturn(Optional.of("true"));

        CryptoKeyManager manager = new CryptoKeyManager(secrets, config);
        assertThrows(CryptoConfigurationException.class, manager::init);
    }

    @Test
    void disabledStartsSuccessfully() {
        RuntimeSecretProvider secrets = mock();
        SystemConfigValueProvider config = mock();
        when(config.find("crypto.enabled")).thenReturn(Optional.of("false"));

        CryptoKeyManager manager = new CryptoKeyManager(secrets, config);
        assertDoesNotThrow(manager::init);
        assertEquals(CryptoKeyManager.State.DISABLED, manager.getState());
    }
}
