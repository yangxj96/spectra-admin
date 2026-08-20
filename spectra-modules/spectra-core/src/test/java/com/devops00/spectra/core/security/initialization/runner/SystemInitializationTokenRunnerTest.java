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

package com.devops00.spectra.core.security.initialization.runner;

import com.devops00.spectra.core.security.initialization.javabean.vo.SystemInitializationStatusVO;
import com.devops00.spectra.core.security.initialization.service.SystemInitializationService;
import com.devops00.spectra.core.security.initialization.service.impl.SystemInitializationTokenManager;
import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 系统初始化令牌启动引导测试。 */
class SystemInitializationTokenRunnerTest {

    @Test
    void shouldEnsureTokenOnlyWhenSystemIsUninitialized() {
        SystemInitializationService initializationService = mock();
        SystemInitializationTokenManager tokenManager = mock();
        when(initializationService.status()).thenReturn(new SystemInitializationStatusVO("UNINITIALIZED", false, true));

        new SystemInitializationTokenRunner(initializationService, tokenManager).run(null);

        verify(tokenManager).ensureToken();
        verify(tokenManager, never()).clear();
    }

    @Test
    void shouldClearTokenWhenSystemIsInitialized() {
        SystemInitializationService initializationService = mock();
        SystemInitializationTokenManager tokenManager = mock();
        when(initializationService.status()).thenReturn(new SystemInitializationStatusVO("INITIALIZED", true, false));

        new SystemInitializationTokenRunner(initializationService, tokenManager).run(null);

        verify(tokenManager).clear();
        verify(tokenManager, never()).ensureToken();
    }
}
