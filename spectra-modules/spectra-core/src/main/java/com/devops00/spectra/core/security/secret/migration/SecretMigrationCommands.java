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

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.security.secret.service.SecretManagementService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 迁移命令的共同执行器。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
final class SecretMigrationCommands {

    private SecretMigrationCommands() {
    }

    static SecretMigrationResult migrate(SecretManagementService service, List<String> codes,
                                         Map<String, String> legacyValues) {
        if (legacyValues == null) {
            throw new DataSaveException("旧密钥快照不能为空");
        }
        var migrated = new ArrayList<String>();
        for (String code : codes) {
            String value = legacyValues.get(code);
            if (value == null || value.isBlank()) {
                continue;
            }
            service.createPending(code, value, "MIGRATION");
            migrated.add(code);
        }
        return new SecretMigrationResult(migrated.size(), migrated);
    }
}
