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

import com.devops00.spectra.common.audit.AuditService;
import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.core.CoreModule;
import com.devops00.spectra.launch.configuration.ModuleAssembly;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 可选模块隔离、统一入口和未来模块装配契约测试。
 */
class OptionalModuleIsolationTest {

    private static final Pattern BUSINESS_IMPLEMENTATION = Pattern.compile(
            "(?m)^\\s*@(?:RestController|Controller|Service|Repository|Mapper|Entity)\\b");

    private static final List<String> BUSINESS_PACKAGES = List.of(
            "com.devops00.spectra.core.",
            "com.devops00.spectra.oa.",
            "com.devops00.spectra.workflow.",
            "com.devops00.spectra.erp.");

    @Test
    void launchMustOnlyContainAssemblyCode() throws IOException {
        var sourceRoot = resolveBackendPath("spectra-launch/src/main/java");
        var sources = javaSources(sourceRoot);
        var implementationFiles = sources.stream()
                .filter(path -> BUSINESS_IMPLEMENTATION.matcher(read(path)).find()
                        || path.getFileName().toString().matches(".*(Controller|Entity|Mapper|Repository|Service)\\.java"))
                .toList();

        assertThat(implementationFiles)
                .as("launch 只能包含启动入口和模块装配代码")
                .isEmpty();
        assertThat(sources)
                .allMatch(path -> readPackage(path).startsWith("package com.devops00.spectra.launch"));
    }

    @Test
    void commonAndFrameworkMustNotImportBusinessModules() throws IOException {
        var roots = List.of(
                resolveBackendPath("spectra-common/src/main/java"),
                resolveBackendPath("spectra-framework/src/main/java"));
        var violations = roots.stream()
                .flatMap(root -> uncheckedJavaSources(root).stream())
                .filter(path -> hasBusinessImport(read(path)))
                .toList();

        assertThat(violations)
                .as("common/framework 不得反向依赖业务模块")
                .isEmpty();
    }

    @Test
    void healthChecksMustUseOneCommonContributorProtocol() throws IOException {
        Map<String, String> expectedContributors = Map.of(
                "spectra-framework/src/main/java/com/devops00/spectra/framework/health/DataSourceHealthContributor.java",
                "com.devops00.spectra.common.health.DependencyHealthContributor",
                "spectra-framework/src/main/java/com/devops00/spectra/framework/health/RedisHealthContributor.java",
                "com.devops00.spectra.common.health.DependencyHealthContributor",
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/scheduler/health/SchedulerHealthIndicator.java",
                "com.devops00.spectra.common.health.DependencyHealthContributor",
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/notification/health/NotificationHealthIndicator.java",
                "com.devops00.spectra.common.health.DependencyHealthContributor",
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/upload/health/FileStorageHealthIndicator.java",
                "com.devops00.spectra.common.health.DependencyHealthContributor",
                "spectra-modules/spectra-workflow/src/main/java/com/devops00/spectra/workflow/health/FlowableHealthContributor.java",
                "com.devops00.spectra.common.health.DependencyHealthContributor");

        expectedContributors.forEach((relativePath, contract) -> assertThat(read(resolveBackendPath(relativePath)))
                .as("健康实现必须使用统一 contributor 协议: %s", relativePath)
                .contains(contract));

        assertThat(read(resolveBackendPath(
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/system/health/CoreHealthRegistry.java")))
                .contains("List<DependencyHealthContributor>");
        assertThat(read(resolveBackendPath(
                "spectra-framework/src/main/java/com/devops00/spectra/framework/health/ActuatorHealthContributorAdapter.java")))
                .contains("implements HealthIndicator");
    }

    @Test
    void auditCallsMustUseUnifiedAuditServiceAndNoLegacySinkTypes() throws IOException {
        var backend = resolveBackendPath("");
        var productionSources = uncheckedJavaSources(backend).stream()
                .filter(path -> path.toString().contains("src\\main\\java"))
                .toList();
        var annotatedSources = productionSources.stream()
                .filter(path -> read(path).contains("@Audit"))
                .toList();

        assertThat(annotatedSources)
                .as("所有审计注解必须来自统一 common.audit 入口")
                .allMatch(path -> read(path).contains("import com.devops00.spectra.common.audit.Audit;"));
        assertThat(Files.exists(backend.resolve(
                "spectra-starter/spectra-log-base/src/main/java/com/devops00/spectra/log/base/annotation/ULog.java")))
                .as("旧 ULog 注解不得恢复")
                .isFalse();
        assertThat(Files.exists(backend.resolve("spectra-starter/spectra-log-base")))
                .as("旧 spectra-log-base 模块不得恢复")
                .isFalse();
        assertThat(Files.exists(backend.resolve("spectra-starter/spectra-log-spring-boot-starter")))
                .as("旧 spectra-log-spring-boot-starter 模块不得恢复")
                .isFalse();
        assertThat(Files.exists(backend.resolve(
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/common/listener/ulog/ULogListener.java")))
                .as("旧 ULog Listener 不得恢复")
                .isFalse();
        assertThat(read(resolveBackendPath(
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/audit/CoreAuditService.java")))
                .contains("implements AuditService");
    }

    @Test
    void futureErpMustBeAnAssemblyConcernNotACoreChange() throws IOException {
        var coreSources = uncheckedJavaSources(resolveBackendPath("spectra-modules/spectra-core/src/main/java"));
        assertThat(coreSources)
                .as("Core 不能因为未来 ERP 增加业务依赖")
                .allMatch(path -> !read(path).contains("com.devops00.spectra.erp"));

        var futureErp = ModuleAssembly.of(ModuleAssembly.CORE, "erp");
        assertThatCode(futureErp::validate).doesNotThrowAnyException();
        assertThat(futureErp.enabledModules()).containsExactlyInAnyOrder(ModuleAssembly.CORE, "erp");
        assertThat(CoreModule.class.getPackageName()).isEqualTo("com.devops00.spectra.core");
        assertThat(AuditService.class.getPackageName()).startsWith("com.devops00.spectra.common.audit");
        assertThat(DependencyHealthContributor.class.getPackageName())
                .startsWith("com.devops00.spectra.common.health");
    }

    private static List<Path> uncheckedJavaSources(Path sourceRoot) {
        try {
            return javaSources(sourceRoot);
        } catch (IOException exception) {
            throw new IllegalStateException("无法扫描 Java 源码: " + sourceRoot, exception);
        }
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

    private static boolean hasBusinessImport(String source) {
        return source.lines()
                .map(String::trim)
                .filter(line -> line.startsWith("import "))
                .anyMatch(line -> BUSINESS_PACKAGES.stream().anyMatch(line::contains));
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
