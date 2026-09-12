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
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

/** 密钥管理基线的结构和种子契约测试。 */
class SecretManagementMigrationTest {

    @Test
    void createsEncryptedSecretDefinitionAndVersionTablesWithSafetyConstraints() throws IOException {
        String migration = readBaseline();

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
    void baselineSeedsOnlySecretMetadataAndContainsNoLegacyRepair() throws IOException {
        String baseline = readBaseline();

        assertThat(baseline).contains("crypto.server.public-key")
                .contains("crypto.server.private-key")
                .contains("crypto.client.public-key")
                .contains("crypto.client.private-key")
                .contains("notification.provider.sms.secret")
                .contains("security.csrf-signing-key")
                .doesNotContain("INSERT INTO spectra_security.sec_secret_version")
                .doesNotContain("UPDATE spectra_core.sys_config")
                .doesNotContain("V20__repair_incomplete_crypto_configuration.sql");
    }

    private static String readBaseline() throws IOException {
        try (var resource = SecretManagementMigrationTest.class.getClassLoader()
                .getResourceAsStream("db/migration/V1__init_db.sql")) {
            assertThat(resource).as("缺少 Flyway V1 基线").isNotNull();
            return new String(resource.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}
