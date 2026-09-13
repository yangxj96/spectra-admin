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

/**
 * 验证 {@code AuditSanitizerBeanContractTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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
