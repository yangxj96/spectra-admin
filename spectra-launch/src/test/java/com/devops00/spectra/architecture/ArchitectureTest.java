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

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

import static com.tngtech.archunit.core.importer.ImportOption.DoNotIncludeTests;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 后端分层和模块依赖架构测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/24
 */
@AnalyzeClasses(packages = "com.devops00.spectra", importOptions = DoNotIncludeTests.class)
class ArchitectureTest {

    @ArchTest
    static final ArchRule CONTROLLERS_MUST_NOT_ACCESS_DATA_ACCESS = noClasses()
            .that()
            .resideInAnyPackage("..controller..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..mapper..", "..repository..");

    @ArchTest
    static final ArchRule SERVICES_MUST_NOT_DEPEND_ON_CONTROLLERS = noClasses()
            .that()
            .resideInAnyPackage("..service..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..controller..");

    @ArchTest
    static final ArchRule DATA_ACCESS_MUST_NOT_DEPEND_ON_CONTROLLERS = noClasses()
            .that()
            .resideInAnyPackage("..mapper..", "..repository..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("..controller..");

    @ArchTest
    static final ArchRule APPLICATION_MUST_NOT_DEPEND_ON_MICROSERVICE_COMPONENTS = noClasses()
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "com.alibaba.cloud..",
                    "com.alibaba.nacos..",
                    "org.springframework.cloud..",
                    "com.netflix.eureka..",
                    "com.netflix.discovery..",
                    "com.ecwid.consul..",
                    "org.apache.curator..",
                    "org.apache.zookeeper..");

    @ArchTest
    static final ArchRule CORE_MUST_NOT_DEPEND_ON_OPTIONAL_BUSINESS_MODULES = noClasses()
            .that()
            .resideInAnyPackage("com.devops00.spectra.core..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "com.devops00.spectra.oa..",
                    "com.devops00.spectra.workflow..",
                    "com.devops00.spectra.erp..");

    @ArchTest
    static final ArchRule LAUNCH_MUST_NOT_CONTAIN_BUSINESS_IMPLEMENTATIONS = noClasses()
            .that()
            .resideInAnyPackage("com.devops00.spectra.launch..")
            .should()
            .resideInAnyPackage(
                    "com.devops00.spectra.launch..controller..",
                    "com.devops00.spectra.launch..entity..",
                    "com.devops00.spectra.launch..mapper..",
                    "com.devops00.spectra.launch..repository..",
                    "com.devops00.spectra.launch..service..");

    @ArchTest
    static final ArchRule COMMON_MUST_NOT_DEPEND_ON_BUSINESS_MODULES = noClasses()
            .that()
            .resideInAnyPackage("com.devops00.spectra.common..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "com.devops00.spectra.core..",
                    "com.devops00.spectra.oa..",
                    "com.devops00.spectra.workflow..",
                    "com.devops00.spectra.erp..");

    @ArchTest
    static final ArchRule FRAMEWORK_MUST_NOT_DEPEND_ON_BUSINESS_MODULES = noClasses()
            .that()
            .resideInAnyPackage("com.devops00.spectra.framework..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "com.devops00.spectra.core..",
                    "com.devops00.spectra.oa..",
                    "com.devops00.spectra.workflow..",
                    "com.devops00.spectra.erp..");

    @ArchTest
    static final ArchRule OPTIONAL_MODULES_MUST_NOT_DEPEND_ON_LAUNCH = noClasses()
            .that()
            .resideInAnyPackage(
                    "com.devops00.spectra.core..",
                    "com.devops00.spectra.oa..",
                    "com.devops00.spectra.workflow..",
                    "com.devops00.spectra.erp..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("com.devops00.spectra.launch..");

    @ArchTest
    static final ArchRule COMMON_MUST_NOT_DEPEND_ON_SECURITY_IMPLEMENTATION = noClasses()
            .that()
            .resideInAnyPackage(
                    "com.devops00.spectra.common.port.security..",
                    "com.devops00.spectra.common.security..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage(
                    "org.springframework.security..",
                    "org.springframework.data.redis..",
                    "jakarta.servlet..");

    @ArchTest
    static final ArchRule FRAMEWORK_MUST_NOT_DEPEND_ON_CORE = noClasses()
            .that()
            .resideInAnyPackage("com.devops00.spectra.framework..")
            .should()
            .dependOnClassesThat()
            .resideInAnyPackage("com.devops00.spectra.core..");

    @ArchTest
    static void coreMustOwnMandatoryBusinessCapabilities(JavaClasses classes) {
        Map<String, String> mandatoryCapabilities = Map.ofEntries(
                Map.entry("用户", "com.devops00.spectra.core.user"),
                Map.entry("角色和权限", "com.devops00.spectra.core.security.authorization"),
                Map.entry("组织", "com.devops00.spectra.core.system"),
                Map.entry("菜单", "com.devops00.spectra.core.system"),
                Map.entry("字典", "com.devops00.spectra.core.system"),
                Map.entry("系统配置", "com.devops00.spectra.core.system"),
                Map.entry("安全审计", "com.devops00.spectra.core.security.audit"),
                Map.entry("普通操作日志", "com.devops00.spectra.core.audit"),
                Map.entry("调度", "com.devops00.spectra.core.scheduler"),
                Map.entry("服务监控", "com.devops00.spectra.core.system.service"),
                Map.entry("统一通知", "com.devops00.spectra.core.notification"),
                Map.entry("文件上传", "com.devops00.spectra.core.upload"));

        var packageNames = classes.stream()
                .map(JavaClass::getPackageName)
                .toList();
        mandatoryCapabilities.forEach((capability, packageName) -> assertTrue(
                packageNames.stream()
                        .anyMatch(current -> current.equals(packageName)
                                || current.startsWith(packageName + ".")),
                () -> "core 必须拥有必选能力「" + capability + "」对应的包: " + packageName));
    }

    @ArchTest
    static void coreMustNotContainOptionalBusinessPackages(JavaClasses classes) {
        var optionalPackagesInCore = classes.stream()
                .map(JavaClass::getPackageName)
                .filter(packageName -> packageName.startsWith("com.devops00.spectra.core.oa.")
                        || packageName.startsWith("com.devops00.spectra.core.workflow.")
                        || packageName.startsWith("com.devops00.spectra.core.erp."))
                .toList();

        assertTrue(optionalPackagesInCore.isEmpty(),
                () -> "可选业务类型不得移动到 core 子包: " + optionalPackagesInCore);
    }

    @ArchTest
    static void frameworkMustOwnSecurityTechnicalMechanisms(JavaClasses classes) {
        Map<String, String> technicalPackages = Map.of(
                "安全过滤器", "com.devops00.spectra.framework.configure.security.filter",
                "安全运行时配置", "com.devops00.spectra.framework.configure.security.configuration",
                "安全 Redis repository", "com.devops00.spectra.framework.configure.security.strategy");

        var packageNames = classes.stream()
                .map(JavaClass::getPackageName)
                .toList();
        technicalPackages.forEach((capability, packageName) -> assertTrue(
                packageNames.stream()
                        .anyMatch(current -> current.equals(packageName)
                                || current.startsWith(packageName + ".")),
                () -> "framework 必须拥有安全技术机制「" + capability + "」对应的包: " + packageName));
    }

    @Test
    void corePomMustNotDeclareOptionalBusinessModules() throws IOException {
        String pom = Files.readString(resolveBackendPath("spectra-modules/spectra-core/pom.xml"));

        assertFalse(pom.contains("<artifactId>spectra-workflow</artifactId>"));
        assertFalse(pom.contains("<artifactId>spectra-oa</artifactId>"));
    }

    @Test
    void launchPomMustDeclareCoreAndOptionalModules() throws IOException {
        String pom = Files.readString(resolveBackendPath("spectra-launch/pom.xml"));

        assertTrue(pom.contains("<artifactId>spectra-core</artifactId>"));
        assertTrue(pom.contains("<artifactId>spectra-workflow</artifactId>"));
        assertTrue(pom.contains("<artifactId>spectra-oa</artifactId>"));
    }

    @Test
    void optionalBusinessTypesMustRemainInTheirOwningModule() throws IOException {
        Map<String, String> optionalModules = Map.of(
                "spectra-modules/spectra-oa/src/main/java", "com.devops00.spectra.oa",
                "spectra-modules/spectra-workflow/src/main/java", "com.devops00.spectra.workflow");

        for (var entry : optionalModules.entrySet()) {
            Path sourceRoot = resolveBackendPath(entry.getKey());
            try (var paths = Files.walk(sourceRoot)) {
                paths.filter(path -> path.toString().endsWith(".java"))
                        .forEach(path -> assertTrue(
                                readPackageDeclaration(path).startsWith("package " + entry.getValue() + ";")
                                        || readPackageDeclaration(path).startsWith("package " + entry.getValue() + "."),
                                () -> "可选模块类型必须留在所属模块包内: " + path));
            }
        }
    }

    private static String readPackageDeclaration(Path sourceFile) {
        try (var lines = Files.lines(sourceFile)) {
            return lines.map(String::trim)
                    .filter(line -> line.startsWith("package "))
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

}
