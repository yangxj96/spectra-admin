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
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Framework 包布局和核心类型迁移清单的架构测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
class FrameworkPackageLayoutTest {

    private static final String FRAMEWORK_PACKAGE = "com.devops00.spectra.framework";

    private static final String CONFIGURE_PACKAGE = FRAMEWORK_PACKAGE + ".configure";

    private static final List<String> CAPABILITY_PACKAGES = List.of(
            FRAMEWORK_PACKAGE + ".cache",
            FRAMEWORK_PACKAGE + ".captcha",
            FRAMEWORK_PACKAGE + ".persistence",
            FRAMEWORK_PACKAGE + ".security",
            FRAMEWORK_PACKAGE + ".serialization",
            FRAMEWORK_PACKAGE + ".web",
            FRAMEWORK_PACKAGE + ".assembler",
            FRAMEWORK_PACKAGE + ".health");

    /**
     * Framework 核心类型清单及其目标包。清单保持一类一个入口，防止迁移遗漏或重复。
     */
    private static final Map<String, String> MIGRATION_TARGETS = Map.ofEntries(
            Map.entry("CacheConfiguration", FRAMEWORK_PACKAGE + ".cache.configuration"),
            Map.entry("StandardCacheKeyGenerator", FRAMEWORK_PACKAGE + ".cache"),
            Map.entry("JacksonConfiguration", FRAMEWORK_PACKAGE + ".serialization.jackson"),
            Map.entry("JacksonProperties", FRAMEWORK_PACKAGE + ".serialization.jackson"),
            Map.entry("KaptchaConfiguration", FRAMEWORK_PACKAGE + ".captcha.configuration"),
            Map.entry("KaptchaTextCreator", FRAMEWORK_PACKAGE + ".captcha.generator"),
            Map.entry("KaptchaType", FRAMEWORK_PACKAGE + ".captcha.generator"),
            Map.entry("KaptchaProperties", FRAMEWORK_PACKAGE + ".captcha.configuration"),
            Map.entry("GlobalMapperConfig", FRAMEWORK_PACKAGE + ".serialization.mapper"),
            Map.entry("TimeMapper", FRAMEWORK_PACKAGE + ".serialization.mapper"),
            Map.entry("MvcConfiguration", FRAMEWORK_PACKAGE + ".web.configuration"),
            Map.entry("PasswordEncoderConfiguration", FRAMEWORK_PACKAGE + ".security.configuration.authentication"),
            Map.entry("CommonExceptionAdvice", FRAMEWORK_PACKAGE + ".web.advice.exception"),
            Map.entry("KaptchaExceptionAdvice", FRAMEWORK_PACKAGE + ".web.advice.exception"),
            Map.entry("SqlExceptionAdvice", FRAMEWORK_PACKAGE + ".web.advice.exception"),
            Map.entry("RequestDecryptAdvice", FRAMEWORK_PACKAGE + ".web.advice.crypto"),
            Map.entry("ResponseEncryptAdvice", FRAMEWORK_PACKAGE + ".web.advice.crypto"),
            Map.entry("ResponseModifyAdvice", FRAMEWORK_PACKAGE + ".web.advice.crypto"),
            Map.entry("CryptoKeyManager", FRAMEWORK_PACKAGE + ".web.crypto"),
            Map.entry("RequestCorrelationFilter", FRAMEWORK_PACKAGE + ".web.filter"),
            Map.entry("RequestGetParamsFilter", FRAMEWORK_PACKAGE + ".web.filter"),
            Map.entry("AuthenticationWebUtils", FRAMEWORK_PACKAGE + ".web.security"),
            Map.entry("DataScopeContextFilter", FRAMEWORK_PACKAGE + ".persistence.scope.context"),
            Map.entry("DataScopeEntityRegistry", FRAMEWORK_PACKAGE + ".persistence.scope.context"),
            Map.entry("DataScopeExecutor", FRAMEWORK_PACKAGE + ".persistence.scope.context"),
            Map.entry("MetaObjectHandlerImpl", FRAMEWORK_PACKAGE + ".persistence.mybatis"),
            Map.entry("MyBatisPlusConfiguration", FRAMEWORK_PACKAGE + ".persistence.configuration"),
            Map.entry("DataScopeInnerInterceptor", FRAMEWORK_PACKAGE + ".persistence.scope.authorization"),
            Map.entry("ResourceAuthorizationGuard", FRAMEWORK_PACKAGE + ".persistence.scope.authorization"),
            Map.entry("ScopeSqlPolicy", FRAMEWORK_PACKAGE + ".persistence.scope.authorization"),
            Map.entry("RedisConfiguration", FRAMEWORK_PACKAGE + ".cache.configuration"),
            Map.entry("LoginExceptionAdvice", FRAMEWORK_PACKAGE + ".security.advice"),
            Map.entry("RestAccessDeniedHandler", FRAMEWORK_PACKAGE + ".security.advice"),
            Map.entry("RestAuthenticationEntryPoint", FRAMEWORK_PACKAGE + ".security.advice"),
            Map.entry("SecurityAutoConfiguration", FRAMEWORK_PACKAGE + ".security.configuration"),
            Map.entry("SecJacksonConfiguration", FRAMEWORK_PACKAGE + ".security.configuration.redis"),
            Map.entry("SecRedisConfiguration", FRAMEWORK_PACKAGE + ".security.configuration.redis"),
            Map.entry("SecurityConfiguration", FRAMEWORK_PACKAGE + ".security.configuration.authentication"),
            Map.entry("SecuritySessionPortConfiguration", FRAMEWORK_PACKAGE + ".security.configuration.session"),
            Map.entry("WebCookiePolicy", FRAMEWORK_PACKAGE + ".web.security"),
            Map.entry("UserOnlineConverter", FRAMEWORK_PACKAGE + ".security.converter"),
            Map.entry("SpectraPermissionEvaluator", FRAMEWORK_PACKAGE + ".security.authorization"),
            Map.entry("TokenAuthenticationFilter", FRAMEWORK_PACKAGE + ".security.authentication"),
            Map.entry("SecurityLoginFailureTracker", FRAMEWORK_PACKAGE + ".security.session.lifecycle"),
            Map.entry("SecuritySessionContextAccessor", FRAMEWORK_PACKAGE + ".security.session.query"),
            Map.entry("SecuritySessionIssuer", FRAMEWORK_PACKAGE + ".security.session.lifecycle"),
            Map.entry("SecuritySessionQuery", FRAMEWORK_PACKAGE + ".security.session.query"),
            Map.entry("SecuritySessionReader", FRAMEWORK_PACKAGE + ".security.session.query"),
            Map.entry("SecuritySessionRevoker", FRAMEWORK_PACKAGE + ".security.session.lifecycle"),
            Map.entry("SecurityTokenAccessor", FRAMEWORK_PACKAGE + ".security.session.token"),
            Map.entry("SecMode", FRAMEWORK_PACKAGE + ".security.properties"),
            Map.entry("SecurityProperties", FRAMEWORK_PACKAGE + ".security.properties"),
            Map.entry("RateLimitPolicy", FRAMEWORK_PACKAGE + ".security.ratelimit"),
            Map.entry("RedisRateLimiter", FRAMEWORK_PACKAGE + ".security.ratelimit"),
            Map.entry("RequestRateLimitFilter", FRAMEWORK_PACKAGE + ".security.ratelimit"),
            Map.entry("RedisSecurityInitializationTokenStore", FRAMEWORK_PACKAGE + ".security.redis.store"),
            Map.entry("RedisSecurityVerificationStore", FRAMEWORK_PACKAGE + ".security.redis.store"),
            Map.entry("RefreshTokenRotationStore", FRAMEWORK_PACKAGE + ".security.redis.store"),
            Map.entry("SecurityRedisExecutor", FRAMEWORK_PACKAGE + ".security.redis.key"),
            Map.entry("SecurityRedisKey", FRAMEWORK_PACKAGE + ".security.redis.key"),
            Map.entry("SecurityRedisNamespace", FRAMEWORK_PACKAGE + ".security.redis.key"),
            Map.entry("TokenDigestService", FRAMEWORK_PACKAGE + ".security.redis.token"),
            Map.entry("DefaultRootAuthorizationPolicy", FRAMEWORK_PACKAGE + ".security.authorization"),
            Map.entry("SecuritySessionIssueService", FRAMEWORK_PACKAGE + ".security.session"));

    @Test
    void configurePackageMustBeRemoved() throws IOException {
        var violations = frameworkSources().stream()
                .filter(source -> source.packageName().equals(CONFIGURE_PACKAGE)
                        || source.packageName().startsWith(CONFIGURE_PACKAGE + "."))
                .toList();

        assertThat(violations)
                .as("framework.configure 不得包含生产类")
                .isEmpty();
    }

    @Test
    void autoConfigurationImportsMustExposeOnlyFrameworkModule() throws IOException {
        Path imports = resolveBackendPath(
                "spectra-framework/src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports");

        var entries = Files.readAllLines(imports)
                .stream()
                .map(String::trim)
                .filter(line -> !line.isEmpty() && !line.startsWith("#"))
                .toList();

        assertThat(entries)
                .as("自动配置索引只允许暴露 framework 组合入口")
                .containsExactly("com.devops00.spectra.framework.FrameworkModule");
    }

    @Test
    void frameworkModuleMustScanTheWholeFrameworkPackage() throws IOException {
        Path source = resolveBackendPath(
                "spectra-framework/src/main/java/com/devops00/spectra/framework/FrameworkModule.java");
        String contents = Files.readString(source);

        assertThat(contents)
                .contains("@ComponentScan(basePackageClasses = FrameworkModule.class)")
                .doesNotContain("@Import(")
                .doesNotContain("excludeFilters");
    }

    @Test
    void frameworkMustNotAddPerCapabilityAutoConfigurations() throws IOException {
        Path sourceRoot = resolveBackendPath("spectra-framework/src/main/java");
        try (var paths = Files.walk(sourceRoot)) {
            var autoConfigurations = paths.filter(path -> Files.isRegularFile(path)
                    && path.getFileName().toString().endsWith("AutoConfiguration.java"))
                    .map(path -> path.getFileName().toString())
                    .toList();

            assertThat(autoConfigurations)
                    .as("Framework 只保留安全配置本身，能力包不得增加独立 AutoConfiguration 入口")
                    .containsExactly("SecurityAutoConfiguration.java");
        }
    }

    @Test
    void migrationInventoryMustContainExactly64UniqueTypes() throws IOException {
        var frameworkSources = frameworkSources();
        var sourcesByClassName = frameworkSources.stream()
                .collect(Collectors.groupingBy(SourceFile::className));

        assertThat(sourcesByClassName.keySet())
                .as("Framework 核心类型清单必须完整")
                .containsAll(MIGRATION_TARGETS.keySet());
        assertThat(MIGRATION_TARGETS)
                .as("Framework 核心类型清单必须正好覆盖当前 64 个基线类型")
                .hasSize(64);

        MIGRATION_TARGETS.keySet()
                .forEach(className -> assertThat(sourcesByClassName.get(className))
                        .as("迁移类型必须只保留一个生产源文件: %s", className)
                        .hasSize(1));
    }

    @Test
    void migratedTypesMustUseTheSemanticTargetPackages() throws IOException {
        var sourcesByClassName = frameworkSources().stream()
                .collect(Collectors.groupingBy(SourceFile::className));

        MIGRATION_TARGETS.forEach((className, targetPackage) -> assertThat(sourcesByClassName.get(className))
                .as("%s 必须归位到 %s", className, targetPackage)
                .extracting(SourceFile::packageName)
                .containsExactly(targetPackage));
    }

    @Test
    void frameworkSourcePathsMustMatchPackageDeclarations() throws IOException {
        Path sourceRoot = resolveBackendPath("spectra-framework/src/main/java");

        var violations = frameworkSources().stream()
                .filter(source -> !sourceRoot.relativize(source.path())
                        .toString()
                        .replace('\\', '/')
                        .equals(source.packageName().replace('.', '/') + "/" + source.path().getFileName()))
                .toList();

        assertThat(violations)
                .as("framework 生产源码路径必须与 package 声明一致")
                .isEmpty();
    }

    @Test
    void capabilityPackagesMustNotAccumulateDirectTypes() throws IOException {
        var directTypeCounts = frameworkSources().stream()
                .collect(Collectors.groupingBy(SourceFile::packageName, Collectors.counting()));

        var violations = CAPABILITY_PACKAGES.stream()
                .filter(packageName -> directTypeCounts.getOrDefault(packageName, 0L) > 3)
                .collect(Collectors.toMap(packageName -> packageName,
                        packageName -> directTypeCounts.get(packageName)));

        assertThat(violations)
                .as("framework 能力包的直接源码文件不得超过 3 个，超出后必须按职责拆分子包")
                .isEmpty();
    }

    private static List<SourceFile> frameworkSources() throws IOException {
        Path sourceRoot = resolveBackendPath("spectra-framework/src/main/java");
        try (var paths = Files.walk(sourceRoot)) {
            return paths.filter(path -> Files.isRegularFile(path) && path.toString().endsWith(".java"))
                    .map(path -> new SourceFile(path, readPackage(path), path.getFileName()
                            .toString()
                            .replaceFirst("\\.java$", "")))
                    .toList();
        }
    }

    private static String readPackage(Path sourceFile) {
        try (var lines = Files.lines(sourceFile)) {
            return lines.map(String::trim)
                    .filter(line -> line.startsWith("package "))
                    .map(line -> line.substring("package ".length(), line.length() - 1))
                    .findFirst()
                    .orElse("");
        } catch (IOException exception) {
            throw new IllegalStateException("无法读取 Java 包声明: " + sourceFile, exception);
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

    private record SourceFile(Path path, String packageName, String className) {
    }
}
