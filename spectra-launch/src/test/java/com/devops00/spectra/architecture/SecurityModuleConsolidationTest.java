/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security 核心化后的模块归属和旧入口清理约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
class SecurityModuleConsolidationTest {

    @Test
    void legacySecurityModulesMustBeRemoved() throws IOException {
        Path backend = resolveBackendPath("");
        String pom = Files.readString(backend.resolve("pom.xml"));

        assertThat(pom).doesNotContain("<module>spectra-starter</module>");
        assertThat(Files.notExists(backend.resolve("spectra-starter/pom.xml"))).isTrue();
        assertThat(Files.notExists(backend.resolve("spectra-starter/spectra-security-base"))).isTrue();
        assertThat(Files.notExists(backend.resolve("spectra-starter/spectra-security-spring-boot-starter"))).isTrue();
    }

    @Test
    void frameworkMustOwnSecurityTechnicalPackages() {
        Path backend = resolveBackendPath("");
        List<String> technicalTypes = List.of(
                "spectra-framework/src/main/java/com/devops00/spectra/framework/configure/security/autoconfiguration/SecurityAutoConfiguration.java",
                "spectra-framework/src/main/java/com/devops00/spectra/framework/configure/security/configuration/SecurityConfiguration.java",
                "spectra-framework/src/main/java/com/devops00/spectra/framework/configure/security/filter/TokenAuthenticationFilter.java",
                "spectra-framework/src/main/java/com/devops00/spectra/framework/configure/security/strategy/RedisSecuritySessionRepository.java");

        assertThat(technicalTypes)
                .allMatch(type -> Files.exists(backend.resolve(type)),
                        "Security 技术实现必须归入 spectra-framework");
    }

    @Test
    void commonSecuritySourcesMustNotUseImplementationTypes() throws IOException {
        Path backend = resolveBackendPath("");
        List<Path> securityRoots = List.of(
                backend.resolve("spectra-common/src/main/java/com/devops00/spectra/common/port/security"),
                backend.resolve("spectra-common/src/main/java/com/devops00/spectra/common/security"));
        assertThat(securityRoots).allSatisfy(root -> assertThat(root).isDirectory());

        List<Path> sources = new ArrayList<>();
        for (Path securityRoot : securityRoots) {
            sources.addAll(sourceFiles(securityRoot));
        }
        var violations = sources.stream()
                .filter(path -> containsForbiddenSecurityType(read(path)))
                .toList();
        assertThat(violations)
                .as("common 安全契约不得依赖 Spring Security、Redis、Servlet 或自动配置")
                .isEmpty();
    }

    @Test
    void backendMustNotReferenceLegacySecurityPackagesOrArtifacts() throws IOException {
        Path backend = resolveBackendPath("");
        Path thisTest = backend.resolve(
                "spectra-launch/src/test/java/com/devops00/spectra/architecture/SecurityModuleConsolidationTest.java");
        try (Stream<Path> paths = Files.walk(backend)) {
            var violations = paths.filter(Files::isRegularFile)
                    .filter(SecurityModuleConsolidationTest::isSourceOrConfiguration)
                    .filter(path -> !path.equals(thisTest))
                    .filter(path -> containsLegacyReference(read(path)))
                    .toList();
            assertThat(violations)
                    .as("后端不得保留旧 Security 包名、artifact 或 Starter 入口")
                    .isEmpty();
        }
    }

    @Test
    void launchMustNotDeclareSecurityStarter() throws IOException {
        Path launchPom = resolveBackendPath("spectra-launch/pom.xml");
        assertThat(Files.readString(launchPom))
                .doesNotContain("spectra-security-spring-boot-starter")
                .doesNotContain("spectra-security-base");
    }

    private static boolean containsForbiddenSecurityType(String source) {
        return source.contains("org.springframework.security")
                || source.contains("org.springframework.data.redis")
                || source.contains("jakarta.servlet")
                || source.contains("@AutoConfiguration");
    }

    private static boolean containsLegacyReference(String source) {
        return source.contains("com.devops00.spectra.security.base")
                || source.contains("com.devops00.spectra.security.starter")
                || source.contains("spectra-security-base")
                || source.contains("spectra-security-spring-boot-starter")
                || source.contains("<module>spectra-starter</module>");
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("无法读取文件: " + path, exception);
        }
    }

    private static boolean isSourceOrConfiguration(Path path) {
        String name = path.getFileName().toString();
        String normalized = path.toString().replace('\\', '/');
        return name.endsWith(".java")
                || name.equals("pom.xml")
                || (name.endsWith(".xml") && normalized.contains("/src/main/"))
                || name.endsWith(".yml")
                || name.endsWith(".yaml")
                || name.equals("AutoConfiguration.imports");
    }

    private static List<Path> sourceFiles(Path root) throws IOException {
        try (Stream<Path> paths = Files.walk(root)) {
            return paths.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    private static Path resolveBackendPath(String relativePath) {
        Path current = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "."))
                .toAbsolutePath()
                .normalize();
        while (current != null) {
            Path marker = current.resolve("spectra-common/pom.xml");
            if (Files.exists(marker)) {
                return current.resolve(relativePath);
            }
            current = current.getParent();
        }
        throw new IllegalStateException("无法定位 Maven 工程目录: " + relativePath);
    }
}
