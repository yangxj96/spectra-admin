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
 * 一次性迁移业务服务凭据；完成后业务运行时只从密钥管理读取。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public final class BusinessSecretMigrationCommand {

    private static final List<String> CODES = List.of(
            "notification.provider.sms.secret", "notification.provider.email.secret");

    private final SecretManagementService service;

    public BusinessSecretMigrationCommand(SecretManagementService service) {
        this.service = service;
    }

    public SecretMigrationResult migrate(Map<String, String> legacyValues) {
        return SecretMigrationCommands.migrate(service, CODES, legacyValues);
    }
}
