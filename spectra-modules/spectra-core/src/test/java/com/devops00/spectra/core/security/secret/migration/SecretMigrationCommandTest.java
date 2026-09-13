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
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * 一次性迁移命令只写入待启用版本且不返回密钥值。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecretMigrationCommandTest {

    @Test
    void migratesOnlyKnownApplicationValuesAsPending() {
        var service = mock(SecretManagementService.class);
        var command = new ApplicationSecretMigrationCommand(service);

        var result = command.migrate(Map.of(
                "crypto.server.public-key", "PEM-VALUE",
                "unknown.key", "SHOULD-NOT-MIGRATE"));

        assertEquals(1, result.migratedCount());
        assertEquals(List.of("crypto.server.public-key"), result.codes());
        verify(service).createPending("crypto.server.public-key", "PEM-VALUE", "MIGRATION");
    }
}
