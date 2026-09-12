/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.core.audit;

import com.devops00.spectra.common.audit.AuditSanitizer;
import com.devops00.spectra.common.audit.DefaultAuditSanitizer;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotSame;

class AuditSanitizerBeanContractTest {

    @Test
    void auditConfigurationExposesOneFreshSanitizerBean() {
        try (var context = new AnnotationConfigApplicationContext(AuditConfiguration.class)) {
            Map<String, AuditSanitizer> beans = context.getBeansOfType(AuditSanitizer.class);

            assertEquals(1, beans.size());
            AuditSanitizer sanitizer = beans.values().iterator().next();
            assertInstanceOf(DefaultAuditSanitizer.class, sanitizer);
            assertNotSame(new DefaultAuditSanitizer(), sanitizer);
        }
    }

    @Test
    void coreProductionCodeDoesNotUseStaticSanitizerInstance() throws IOException {
        Path sourceRoot = Files.isDirectory(Path.of("src/main/java"))
                ? Path.of("src/main/java")
                : Path.of("spectra-modules/spectra-core/src/main/java");

        try (Stream<Path> files = Files.walk(sourceRoot)) {
            files.filter(path -> path.toString().endsWith(".java"))
                    .forEach(path -> {
                        try {
                            assertFalse(Files.readString(path).contains("DefaultAuditSanitizer.INSTANCE"), path.toString());
                        } catch (IOException exception) {
                            throw new IllegalStateException("无法读取生产源码：" + path, exception);
                        }
                    });
        }
    }
}
