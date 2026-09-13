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

package com.devops00.spectra.core.security.secret.migration;

import com.devops00.spectra.core.security.secret.service.SecretManagementService;

import java.util.List;
import java.util.Map;

/**
 * 一次性迁移安全签名密钥；完成后验证码和授权变更服务只读取统一运行态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public final class SecuritySecretMigrationCommand {

    private static final List<String> CODES = List.of(
            "security.verification-code-hmac", "security.authorization-change-token-hmac",
            "security.request-integrity-hmac", "security.csrf-signing-key");

    private final SecretManagementService service;

    public SecuritySecretMigrationCommand(SecretManagementService service) {
        this.service = service;
    }

    public SecretMigrationResult migrate(Map<String, String> legacyValues) {
        return SecretMigrationCommands.migrate(service, CODES, legacyValues);
    }
}
