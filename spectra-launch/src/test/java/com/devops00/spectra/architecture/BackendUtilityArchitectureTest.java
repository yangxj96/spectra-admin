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
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 全后端工具类、横向辅助能力和领域适配器的归属测试。
 *
 * <p>该测试只依赖源码路径和包声明，避免把纯基础能力、技术适配和业务 Bean
 * 再次收拢到没有边界的 {@code utils} 包。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
class BackendUtilityArchitectureTest {

    private static final String BACKEND_ROOT = "";

    private static final String LEGACY_UTILITY_NAMESPACE = String.join(".",
            "com", "devops00", "spectra", "common", "utils");

    private static final Map<String, String> CANONICAL_TYPES = Map.ofEntries(
            Map.entry("StrUtils", "spectra-common/src/main/java/com/devops00/spectra/common/foundation/lang/StrUtils.java"),
            Map.entry("CollUtils", "spectra-common/src/main/java/com/devops00/spectra/common/foundation/collection/CollUtils.java"),
            Map.entry("ObjUtils", "spectra-common/src/main/java/com/devops00/spectra/common/foundation/collection/ObjUtils.java"),
            Map.entry("TreeBuilder", "spectra-common/src/main/java/com/devops00/spectra/common/foundation/tree/TreeBuilder.java"),
            Map.entry("TreeUtils", "spectra-common/src/main/java/com/devops00/spectra/common/foundation/tree/TreeUtils.java"),
            Map.entry("AESUtils", "spectra-common/src/main/java/com/devops00/spectra/common/security/crypto/symmetric/AESUtils.java"),
            Map.entry("RSAUtils", "spectra-common/src/main/java/com/devops00/spectra/common/security/crypto/asymmetric/RSAUtils.java"),
            Map.entry("SHA256Utils", "spectra-common/src/main/java/com/devops00/spectra/common/security/crypto/digest/SHA256Utils.java"),
            Map.entry("IpUtils", "spectra-framework/src/main/java/com/devops00/spectra/framework/web/request/IpUtils.java"));

    private static final Map<String, String> DOMAIN_HELPERS = Map.ofEntries(
            Map.entry("AuthenticationContextUtils",
                    "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/security/authentication/context/AuthenticationContextUtils.java"),
            Map.entry("AuthenticationIdentifierHash",
                    "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/security/authentication/identity/AuthenticationIdentifierHash.java"),
            Map.entry("VerificationCodeDigest",
                    "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/security/authentication/crypto/VerificationCodeDigest.java"),
            Map.entry("SecurityUserAssembler",
                    "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/security/authentication/application/SecurityUserAssembler.java"),
            Map.entry("NotificationAddressMasker",
                    "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/notification/policy/NotificationAddressMasker.java"),
            Map.entry("OaFileReferenceBinder",
                    "spectra-modules/spectra-oa/src/main/java/com/devops00/spectra/oa/file/reference/OaFileReferenceBinder.java"),
            Map.entry("OaFileReferencePermissionChecker",
                    "spectra-modules/spectra-oa/src/main/java/com/devops00/spectra/oa/file/reference/OaFileReferencePermissionChecker.java"),
            Map.entry("OaFileReferenceType",
                    "spectra-modules/spectra-oa/src/main/java/com/devops00/spectra/oa/file/reference/OaFileReferenceType.java"),
            Map.entry("OaApplicationWorkflowSupport",
                    "spectra-modules/spectra-oa/src/main/java/com/devops00/spectra/oa/application/workflow/OaApplicationWorkflowSupport.java"),
            Map.entry("BpmnDiagramSupport",
                    "spectra-modules/spectra-workflow/src/main/java/com/devops00/spectra/workflow/service/diagram/BpmnDiagramSupport.java"));

    private static final List<String> LEGACY_PATHS = List.of(
            "spectra-common/src/main/java/com/devops00/spectra/common/utils",
            "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/security/authentication/util",
            "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/notification/utils",
            "spectra-modules/spectra-oa/src/main/java/com/devops00/spectra/oa/support",
            "spectra-modules/spectra-oa/src/main/java/com/devops00/spectra/oa/application/support");

    private static final Set<String> FORBIDDEN_COMMON_FOUNDATION_IMPORTS = Set.of(
            "jakarta.servlet.",
            "org.springframework.",
            "com.baomidou.",
            "org.postgresql.",
            "lombok.",
            "com.google.common.");

    private static final List<String> BUSINESS_CRYPTO_ADAPTERS = List.of(
            "/core/notification/security/NotificationDigest.java",
            "/core/notification/security/NotificationPayloadCipher.java",
            "/core/system/security/SystemKeyMaterial.java",
            "/core/user/imports/security/PreviewTokenDigest.java");

    @Test
    void canonicalTypesMustHaveExactlyOneProductionSource() throws IOException {
        var sourceFiles = productionJavaFiles();
        var sourcesByType = sourceFiles.stream()
                .collect(Collectors.groupingBy(path -> path.getFileName().toString().replaceFirst("\\.java$", "")));

        CANONICAL_TYPES.forEach((className, relativePath) -> {
            assertThat(Files.exists(resolveBackendPath(relativePath)))
                    .as("%s 必须存在于 canonical 路径 %s", className, relativePath)
                    .isTrue();
            assertThat(sourcesByType.getOrDefault(className, List.of()))
                    .as("%s 只能保留一个生产实现", className)
                    .hasSize(1);
        });
    }

    @Test
    void canonicalTypesMustUseTheirSemanticPackages() throws IOException {
        CANONICAL_TYPES.forEach((className, relativePath) -> {
            var source = resolveBackendPath(relativePath);
            assertThat(readPackage(source))
                    .as("%s 的 package 必须与路径一致", className)
                    .isEqualTo(packageName(source));
        });
    }

    @Test
    void legacyUtilityAndGenericHelperPathsMustBeRemoved() {
        LEGACY_PATHS.forEach(relativePath -> assertThat(Files.exists(resolveBackendPath(relativePath)))
                .as("旧的通用工具/辅助包必须删除: %s", relativePath)
                .isFalse());
    }

    @Test
    void backendMustNotImportLegacyUtilityNamespace() throws IOException {
        var violations = productionJavaFiles().stream()
                .filter(path -> read(path).contains(LEGACY_UTILITY_NAMESPACE))
                .toList();

        assertThat(violations)
                .as("所有调用方都必须迁移到能力归属包")
                .isEmpty();
    }

    @Test
    void commonFoundationMustNotDependOnRuntimeAdapters() throws IOException {
        Path foundation = resolveBackendPath("spectra-common/src/main/java/com/devops00/spectra/common/foundation");
        var violations = javaFiles(foundation).stream()
                .filter(path -> FORBIDDEN_COMMON_FOUNDATION_IMPORTS.stream()
                        .anyMatch(read(path)::contains))
                .toList();

        assertThat(violations)
                .as("common foundation 只能依赖 JDK 和自身纯数据契约")
                .isEmpty();
    }

    @Test
    void securityAlgorithmsMustNotBeImportedByBusinessServices() throws IOException {
        var violations = productionJavaFiles().stream()
                .filter(path -> path.toString().replace('\\', '/').contains("/spectra-modules/"))
                .filter(path -> !BUSINESS_CRYPTO_ADAPTERS.stream().anyMatch(path.toString()::endsWith))
                .filter(path -> read(path).contains("com.devops00.spectra.common.security.crypto."))
                .toList();

        assertThat(violations)
                .as("业务服务只能依赖 feature-owned crypto adapter，不得直接依赖算法实现")
                .isEmpty();
    }

    @Test
    void domainHelpersMustUseOwningFeaturePackages() throws IOException {
        for (var entry : DOMAIN_HELPERS.entrySet()) {
            Path source = resolveBackendPath(entry.getValue());
            assertThat(Files.exists(source))
                    .as("%s 必须位于 feature-owned 包 %s", entry.getKey(), entry.getValue())
                    .isTrue();
            assertThat(readPackage(source))
                    .as("%s 的 package 必须与 feature 目录一致", entry.getKey())
                    .isEqualTo(packageName(source));
        }
    }

    @Test
    void assemblerExecutorMustRemainAnInjectedComponent() throws IOException {
        Path source = resolveBackendPath(
                "spectra-framework/src/main/java/com/devops00/spectra/framework/assembler/NameFillExecutor.java");
        String contents = read(source);

        assertThat(contents)
                .contains("@Component")
                .contains("private final ApplicationContext applicationContext")
                .doesNotContain("static void fill")
                .doesNotContain("static Map")
                .doesNotContain("static Set");
    }

    private static List<Path> productionJavaFiles() throws IOException {
        try (var paths = Files.walk(resolveBackendPath(BACKEND_ROOT))) {
            return paths.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .filter(path -> path.toString().replace('\\', '/').contains("/src/main/java/"))
                    .sorted()
                    .toList();
        }
    }

    private static List<Path> javaFiles(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            return paths.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .sorted()
                    .toList();
        }
    }

    private static String read(Path path) {
        try {
            return Files.readString(path);
        } catch (IOException exception) {
            throw new IllegalStateException("无法读取源码: " + path, exception);
        }
    }

    private static String readPackage(Path source) {
        return read(source).lines()
                .map(String::trim)
                .filter(line -> line.startsWith("package "))
                .map(line -> line.substring("package ".length(), line.length() - 1))
                .findFirst()
                .orElse("");
    }

    private static String packageName(Path source) {
        Path sourceRoot = source;
        while (sourceRoot != null && !sourceRoot.getFileName().toString().equals("src")) {
            sourceRoot = sourceRoot.getParent();
        }
        if (sourceRoot == null) {
            throw new IllegalStateException("无法从路径计算 package: " + source);
        }
        Path javaRoot = sourceRoot.resolve("main/java");
        return javaRoot.relativize(source.getParent())
                .toString()
                .replace(javaRoot.getFileSystem().getSeparator(), ".");
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
        throw new IllegalStateException("无法定位后端工程目录");
    }
}
