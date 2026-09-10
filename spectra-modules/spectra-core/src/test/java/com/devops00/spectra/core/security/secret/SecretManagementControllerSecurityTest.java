/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.security.secret;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/** 密钥管理接口的角色和 API 版本契约测试。 */
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
