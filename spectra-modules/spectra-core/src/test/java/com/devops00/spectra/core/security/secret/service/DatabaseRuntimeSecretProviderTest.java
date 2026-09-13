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

package com.devops00.spectra.core.security.secret.service;

import com.devops00.spectra.common.port.security.RuntimeSecret;
import com.devops00.spectra.core.security.secret.service.impl.DatabaseRuntimeSecretProvider;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Framework 运行时密钥端口适配测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class DatabaseRuntimeSecretProviderTest {

    @Test
    void delegatesOnlyToDatabaseRuntimeService() {
        var runtimeService = mock(SecretRuntimeService.class);
        var secret = new RuntimeSecret("security.test-key", 1, "value", "fingerprint");
        when(runtimeService.findActive("security.test-key")).thenReturn(Optional.of(secret));

        var provider = new DatabaseRuntimeSecretProvider(runtimeService);

        assertEquals(Optional.of(secret), provider.findActive("security.test-key"));
    }
}
