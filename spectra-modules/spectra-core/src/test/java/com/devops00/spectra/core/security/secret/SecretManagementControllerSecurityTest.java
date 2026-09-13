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

/**
 * 密钥管理接口的角色和 API 版本契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecretManagementControllerSecurityTest {

    @Test
    void everyManagementEndpointRequiresDevOpsRoleAndVersionOne() throws IOException {
        var candidates = new Path[]{
                Path.of("src/main/java/com/devops00/spectra/core/security/secret/controller/SecretManagementController.java"),
                Path.of("spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/security/secret/controller/SecretManagementController.java"),
                Path.of("..", "..", "spectra-modules", "spectra-core", "src", "main", "java",
                        "com", "devops00", "spectra", "core", "security", "secret", "controller",
                        "SecretManagementController.java")
        };
        Path sourcePath = java.util.Arrays.stream(candidates)
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new IOException("找不到密钥管理 Controller 源码"));
        String source = Files.readString(sourcePath);

        assertThat(source).contains("@RequestMapping(\"/security/secrets\")")
                .contains("@PreAuthorize(\"hasRole('ROLE_DEV_OPS')\")")
                .contains("version = \"1.0.0\"")
                .contains("/settings")
                .contains("/settings/crypto")
                .contains("@PostMapping(value = \"/import/preview\"")
                .contains("@PostMapping(value = \"/import\"")
                .contains("captureArguments = false, captureResult = false")
                .doesNotContain("@PreAuthorize(\"hasAnyRole");
    }
}
