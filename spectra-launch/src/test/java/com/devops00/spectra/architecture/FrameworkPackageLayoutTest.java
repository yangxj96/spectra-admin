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
 * Framework 包布局和 64 个历史 configure 类型迁移清单的架构测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/07
 */
class FrameworkPackageLayoutTest {

    private static final String FRAMEWORK_PACKAGE = "com.devops00.spectra.framework";

    private static final String CONFIGURE_PACKAGE = FRAMEWORK_PACKAGE + ".configure";

    /**
     * Task 11 的 64 个历史类型清单及其目标包。清单保持一类一个入口，防止迁移遗漏或重复。
     */
    private static final Map<String, String> MIGRATION_TARGETS = Map.ofEntries(
            Map.entry("CacheConfiguration", CONFIGURE_PACKAGE),
            Map.entry("StandardCacheKeyGenerator", FRAMEWORK_PACKAGE + ".cache"),
            Map.entry("JacksonConfiguration", CONFIGURE_PACKAGE),
            Map.entry("JacksonProperties", CONFIGURE_PACKAGE),
            Map.entry("KaptchaConfiguration", CONFIGURE_PACKAGE),
            Map.entry("KaptchaTextCreator", FRAMEWORK_PACKAGE + ".captcha"),
            Map.entry("KaptchaType", FRAMEWORK_PACKAGE + ".captcha"),
            Map.entry("KaptchaProperties", CONFIGURE_PACKAGE),
            Map.entry("GlobalMapperConfig", CONFIGURE_PACKAGE),
            Map.entry("TimeMapper", FRAMEWORK_PACKAGE + ".serialization"),
            Map.entry("MvcConfiguration", CONFIGURE_PACKAGE),
            Map.entry("PasswordEncoderConfiguration", CONFIGURE_PACKAGE),
            Map.entry("CommonExceptionAdvice", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("KaptchaExceptionAdvice", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("SqlExceptionAdvice", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("RequestDecryptAdvice", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("ResponseEncryptAdvice", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("ResponseModifyAdvice", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("CryptoKeyManager", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("RequestCorrelationFilter", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("RequestGetParamsFilter", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("AuthenticationWebUtils", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("DataScopeContextFilter", FRAMEWORK_PACKAGE + ".persistence"),
            Map.entry("DataScopeEntityRegistry", FRAMEWORK_PACKAGE + ".persistence"),
            Map.entry("DataScopeExecutor", FRAMEWORK_PACKAGE + ".persistence"),
            Map.entry("MetaObjectHandlerImpl", FRAMEWORK_PACKAGE + ".persistence"),
            Map.entry("MyBatisPlusConfiguration", CONFIGURE_PACKAGE),
            Map.entry("DataScopeInnerInterceptor", FRAMEWORK_PACKAGE + ".persistence"),
            Map.entry("ResourceAuthorizationGuard", FRAMEWORK_PACKAGE + ".persistence"),
            Map.entry("ScopeSqlPolicy", FRAMEWORK_PACKAGE + ".persistence"),
            Map.entry("RedisConfiguration", CONFIGURE_PACKAGE),
            Map.entry("LoginExceptionAdvice", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("RestAccessDeniedHandler", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("RestAuthenticationEntryPoint", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecurityAutoConfiguration", CONFIGURE_PACKAGE),
            Map.entry("SecJacksonConfiguration", CONFIGURE_PACKAGE),
            Map.entry("SecRedisConfiguration", CONFIGURE_PACKAGE),
            Map.entry("SecurityConfiguration", CONFIGURE_PACKAGE),
            Map.entry("SecuritySessionPortConfiguration", CONFIGURE_PACKAGE),
            Map.entry("WebCookiePolicy", FRAMEWORK_PACKAGE + ".web"),
            Map.entry("UserOnlineConverter", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SpectraPermissionEvaluator", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("TokenAuthenticationFilter", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecurityLoginFailureTracker", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecuritySessionContextAccessor", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecuritySessionIssuer", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecuritySessionQuery", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecuritySessionReader", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecuritySessionRevoker", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecurityTokenAccessor", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecMode", CONFIGURE_PACKAGE),
            Map.entry("SecurityProperties", CONFIGURE_PACKAGE),
            Map.entry("RateLimitPolicy", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("RedisRateLimiter", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("RequestRateLimitFilter", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("RedisSecurityInitializationTokenStore", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("RedisSecurityVerificationStore", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("RefreshTokenRotationStore", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecurityRedisExecutor", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecurityRedisKey", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("SecurityRedisNamespace", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("TokenDigestService", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("DefaultRootAuthorizationPolicy", FRAMEWORK_PACKAGE + ".security"),
            Map.entry("RedisSecuritySessionRepository", FRAMEWORK_PACKAGE + ".security"));

    @Test
    void configurePackageMustBeFlat() throws IOException {
        var violations = frameworkSources().stream()
                .filter(source -> source.packageName().startsWith(CONFIGURE_PACKAGE + "."))
                .toList();

        assertThat(violations)
                .as("framework.configure 不得包含更深层子包")
                .isEmpty();
    }

    @Test
    void migrationInventoryMustContainExactly64UniqueTypes() throws IOException {
        var frameworkSources = frameworkSources();
        var sourcesByClassName = frameworkSources.stream()
                .collect(Collectors.groupingBy(SourceFile::className));

        assertThat(sourcesByClassName.keySet())
                .as("Task 11 的 framework.configure 迁移清单必须完整")
                .containsAll(MIGRATION_TARGETS.keySet());
        assertThat(MIGRATION_TARGETS)
                .as("Task 11 的迁移清单必须正好覆盖当前 64 个类型")
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
