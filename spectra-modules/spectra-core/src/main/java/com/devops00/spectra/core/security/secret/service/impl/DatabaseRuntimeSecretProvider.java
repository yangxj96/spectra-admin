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

package com.devops00.spectra.core.security.secret.service.impl;

import com.devops00.spectra.common.port.security.RuntimeSecret;
import com.devops00.spectra.common.port.security.RuntimeSecretProvider;
import com.devops00.spectra.core.security.secret.service.SecretRuntimeService;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 将安全 Schema 中的 ACTIVE 密钥暴露给 Framework 和业务模块的适配器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
public class DatabaseRuntimeSecretProvider implements RuntimeSecretProvider {

    private final SecretRuntimeService runtimeService;

    public DatabaseRuntimeSecretProvider(SecretRuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    @Override
    public Optional<RuntimeSecret> findActive(String code) {
        return runtimeService.findActive(code);
    }
}
