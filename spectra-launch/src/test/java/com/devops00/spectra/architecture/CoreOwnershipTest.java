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
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Core 必选能力和依赖边界测试。
 *
 * <p>该测试只确认归属和依赖方向，不把 OA、Workflow、Notification、Upload 或未来 ERP 的实现
 * 强行搬入 Core。</p>
 */
class CoreOwnershipTest {

    private static final List<String> OPTIONAL_PACKAGES = List.of(
            "com.devops00.spectra.oa.",
            "com.devops00.spectra.workflow.",
            "com.devops00.spectra.notification.",
            "com.devops00.spectra.upload.",
            "com.devops00.spectra.erp.");

    private static final Pattern DEPENDENCY_ARTIFACT = Pattern.compile(
            "<dependency>\\s*<groupId>com\\.devops00\\.spectra</groupId>\\s*<artifactId>([^<]+)</artifactId>",
            Pattern.DOTALL);

    @Test
    void everyCoreSourceFileMustStayUnderCorePackage() throws IOException {
        var sourceRoot = resolveBackendPath("spectra-modules/spectra-core/src/main/java");
        var violations = javaSources(sourceRoot).stream()
                .filter(path -> !readPackage(path).startsWith("package com.devops00.spectra.core"))
                .toList();

        assertThat(violations)
                .as("Core 源码不得声明其他模块的包")
                .isEmpty();
    }

    @Test
    void coreMustOwnAllMandatorySystemCapabilities() throws IOException {
        var sourceRoot = resolveBackendPath("spectra-modules/spectra-core/src/main/java");
        Map<String, String> capabilities = Map.of(
                "用户", "com.devops00.spectra.core.user",
                "角色权限", "com.devops00.spectra.core.security.authorization",
                "系统管理", "com.devops00.spectra.core.system",
                "安全审计", "com.devops00.spectra.core.security.audit",
                "普通操作日志", "com.devops00.spectra.core.audit",
                "调度", "com.devops00.spectra.core.scheduler");

        var packages = javaSources(sourceRoot).stream()
                .map(thisPath -> readPackage(thisPath).replaceFirst("^package\\s+", "").replace(";", ""))
                .toList();
        capabilities.forEach((name, packageName) -> assertThat(packages)
                .as("Core 必须拥有%s能力", name)
                .anyMatch(packageValue -> packageValue.equals(packageName)
                        || packageValue.startsWith(packageName + ".")));
    }

    @Test
    void coreMustNotImportOptionalBusinessImplementations() throws IOException {
        var sourceRoot = resolveBackendPath("spectra-modules/spectra-core/src/main/java");
        var violations = javaSources(sourceRoot).stream()
                .filter(path -> OPTIONAL_PACKAGES.stream().anyMatch(read(path)::contains))
                .toList();

        assertThat(violations)
                .as("Core 不得 import 或引用可选业务模块实现")
                .isEmpty();
    }

    @Test
    void corePomMustOnlyUseLeafDependencies() throws IOException {
        var pom = Files.readString(resolveBackendPath("spectra-modules/spectra-core/pom.xml"));
        var artifacts = DEPENDENCY_ARTIFACT.matcher(pom).results().map(match -> match.group(1)).toList();

        assertThat(artifacts)
                .as("Core 不得直接依赖聚合层或可选业务实现")
                .noneMatch(artifact -> artifact.equals("spectra-modules")
                        || artifact.equals("spectra-starter")
                        || artifact.equals("spectra-oa")
                        || artifact.equals("spectra-workflow")
                        || artifact.equals("spectra-notification")
                        || artifact.equals("spectra-upload")
                        || artifact.equals("spectra-erp"));
    }

    private static List<Path> javaSources(Path sourceRoot) throws IOException {
        try (var paths = Files.walk(sourceRoot)) {
            return paths.filter(path -> path.toString().endsWith(".java")).toList();
        }
    }

    private static String readPackage(Path sourceFile) {
        try (var lines = Files.lines(sourceFile)) {
            return lines.map(String::trim)
                    .filter(line -> line.startsWith("package "))
                    .findFirst()
                    .orElse("");
        } catch (IOException exception) {
            throw new IllegalStateException("无法读取 Java 包声明: " + sourceFile, exception);
        }
    }

    private static String read(Path sourceFile) {
        try {
            return Files.readString(sourceFile);
        } catch (IOException exception) {
            throw new IllegalStateException("无法读取源码: " + sourceFile, exception);
        }
    }

    private static Path resolveBackendPath(String relativePath) {
        Path current = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "."))
                .toAbsolutePath()
                .normalize();
        while (current != null) {
            Path candidate = current.resolve(relativePath);
            if (Files.exists(candidate)) {
                return candidate;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("无法定位 Maven 工程文件: " + relativePath);
    }
}
