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

import com.devops00.spectra.core.security.initialization.service.SystemInitializationService;
import com.devops00.spectra.core.security.initialization.service.impl.SystemInitializationTokenManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * 系统启动时准备首次初始化令牌。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/20
 */
@Component
public class SystemInitializationTokenRunner implements ApplicationRunner {

    private static final String UNINITIALIZED = "UNINITIALIZED";

    private final SystemInitializationService initializationService;

    private final SystemInitializationTokenManager tokenManager;

    public SystemInitializationTokenRunner(SystemInitializationService initializationService,
                                           SystemInitializationTokenManager tokenManager) {
        this.initializationService = initializationService;
        this.tokenManager = tokenManager;
    }

    @Override
    public void run(ApplicationArguments args) {
        var status = initializationService.status();
        if (UNINITIALIZED.equals(status.state())) {
            tokenManager.ensureToken();
        } else if (status.initialized()) {
            tokenManager.clear();
        }
    }
}
