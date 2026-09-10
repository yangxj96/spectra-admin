/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.core.security.initialization;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/** 首次系统初始化必须负责接口加解密密钥引导的契约测试。 */
class SystemInitializationCryptoContractTest {

    @Test
    void firstSystemInitializationOwnsCryptoKeyBootstrapAndLeavesSwitchDisabled() throws IOException {
        var candidates = new Path[]{
                Path.of("src/main/java/com/devops00/spectra/core/security/initialization/service/impl/SystemInitializationServiceImpl.java"),
                Path.of("spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/security/initialization/service/impl/SystemInitializationServiceImpl.java"),
                Path.of("..", "..", "spectra-modules", "spectra-core", "src", "main", "java", "com", "devops00",
                        "spectra", "core", "security", "initialization", "service", "impl",
                        "SystemInitializationServiceImpl.java")
        };
        Path sourcePath = Arrays.stream(candidates)
                .filter(Files::isRegularFile)
                .findFirst()
                .orElseThrow(() -> new IOException("找不到系统初始化服务源码"));
        String source = Files.readString(sourcePath);

        assertThat(source).contains("SystemSecretInitializer")
                .contains("systemSecretInitializer.initialize()")
                .contains("SystemConfigKeys.CRYPTO_ENABLED")
                .contains("\"false\"");
    }
}
