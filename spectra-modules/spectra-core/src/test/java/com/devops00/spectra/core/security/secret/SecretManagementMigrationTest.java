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

package com.devops00.spectra.core.security.secret;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** 密钥管理迁移脚本的结构契约测试。 */
class SecretManagementMigrationTest {

    @Test
    void createsEncryptedSecretDefinitionAndVersionTablesWithSafetyConstraints() throws IOException {
        String migration = readMigration();

        assertThat(migration).contains("CREATE TABLE spectra_security.sec_secret_definition")
                .contains("CREATE TABLE spectra_security.sec_secret_version")
                .contains("ciphertext bytea")
                .contains("nonce bytea")
                .contains("COMMENT ON TABLE spectra_security.sec_secret_definition")
                .contains("COMMENT ON TABLE spectra_security.sec_secret_version")
                .contains("CONSTRAINT pk_sec_secret_definition PRIMARY KEY (id)")
                .contains("CONSTRAINT pk_sec_secret_version PRIMARY KEY (id)")
                .contains("UNIQUE INDEX uk_sec_secret_definition_code")
                .contains("state = 'ACTIVE'")
                .contains("'PENDING'::character varying")
                .contains("'ACTIVE'::character varying")
                .contains("state = 'DESTROYED'")
                .contains("secret_definition_id");
    }

    @Test
    void disablesLegacyCryptoSwitchWhenRsaSecretSetIsIncomplete() throws IOException {
        String migration = readMigration("V20__repair_incomplete_crypto_configuration.sql");

        assertThat(migration).contains("UPDATE spectra_core.sys_config")
                .contains("crypto.enabled")
                .contains("value = 'false'")
                .contains("crypto.server.public-key")
                .contains("crypto.server.private-key")
                .contains("crypto.client.public-key")
                .contains("crypto.client.private-key")
                .contains("state = 'ACTIVE'")
                .contains("<> 4");
    }

    private static String readMigration() throws IOException {
        return readMigration("V19__create_secret_management.sql");
    }

    private static String readMigration(String fileName) throws IOException {
        var candidates = new Path[]{
                Path.of("spectra-config", "src", "main", "resources", "db", "migration", fileName),
                Path.of("..", "..", "spectra-config", "src", "main", "resources", "db", "migration", fileName),
                Path.of("..", "..", "..", "spectra-config", "src", "main", "resources", "db", "migration", fileName),
                Path.of("..", "..", "..", "..", "spectra-config", "src", "main", "resources", "db", "migration", fileName)
        };
        for (Path candidate : candidates) {
            if (Files.isRegularFile(candidate)) {
                return Files.readString(candidate);
            }
        }
        throw new IOException("找不到密钥管理迁移脚本");
    }
}
