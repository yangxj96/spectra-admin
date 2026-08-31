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

import com.devops00.spectra.core.CoreModule;
import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.framework.FrameworkModule;
import com.devops00.spectra.notification.NotificationModule;
import com.devops00.spectra.notification.controller.NotificationController;
import com.devops00.spectra.notification.health.NotificationHealthIndicator;
import com.devops00.spectra.notification.service.NotificationCleanupScheduledHandler;
import com.devops00.spectra.oa.OaModule;
import com.devops00.spectra.oa.application.controller.ApplicationController;
import com.devops00.spectra.oa.contract.service.job.ContractReminderScheduledHandler;
import com.devops00.spectra.upload.UploadModule;
import com.devops00.spectra.upload.controller.FileUploadController;
import com.devops00.spectra.upload.health.FileStorageHealthIndicator;
import com.devops00.spectra.upload.service.scheduler.FileUploadCleanupScheduledHandler;
import com.devops00.spectra.workflow.WorkflowModule;
import com.devops00.spectra.workflow.controller.TaskController;
import com.devops00.spectra.launch.configuration.ModuleAssembly;
import com.devops00.spectra.launch.configuration.ModuleAssemblyConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.ComponentScan;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

/**
 * 可选模块装配边界测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/08/31
 */
class OptionalModuleAssemblyTest {

    private static final Map<Class<?>, String> MODULE_IMPORTS = Map.of(
            FrameworkModule.class, "com.devops00.spectra.framework.FrameworkModule",
            CoreModule.class, "com.devops00.spectra.core.CoreModule",
            OaModule.class, "com.devops00.spectra.oa.OaModule",
            WorkflowModule.class, "com.devops00.spectra.workflow.WorkflowModule",
            NotificationModule.class, "com.devops00.spectra.notification.NotificationModule",
            UploadModule.class, "com.devops00.spectra.upload.UploadModule");

    private static final Map<Class<?>, String> OPTIONAL_MODULES = Map.of(
            OaModule.class, "oa",
            WorkflowModule.class, "workflow",
            NotificationModule.class, "notification",
            UploadModule.class, "upload");

    @Test
    void moduleEntriesMustBeAutoConfigurationsWithLocalComponentScans() {
        MODULE_IMPORTS.keySet().forEach(moduleClass -> {
            assertThat(moduleClass.getAnnotation(AutoConfiguration.class))
                    .as("模块入口必须是 Spring Boot 自动配置：%s", moduleClass.getName())
                    .isNotNull();

            var componentScan = moduleClass.getAnnotation(ComponentScan.class);
            assertThat(componentScan)
                    .as("模块入口必须声明本模块组件扫描：%s", moduleClass.getName())
                    .isNotNull();
            assertThat(componentScan.basePackages())
                    .as("模块入口不得使用字符串包扫描：%s", moduleClass.getName())
                    .isEmpty();
            assertThat(componentScan.basePackageClasses())
                    .as("模块入口只能扫描自身包：%s", moduleClass.getName())
                    .containsExactly(moduleClass);
        });
    }

    @Test
    void optionalModuleEntriesMustHaveExplicitEnablementProperties() {
        OPTIONAL_MODULES.forEach((moduleClass, moduleName) -> {
            var condition = moduleClass.getAnnotation(ConditionalOnProperty.class);
            assertThat(condition)
                    .as("可选模块必须具备启用条件：%s", moduleClass.getName())
                    .isNotNull();
            assertThat(condition.prefix()).isEqualTo("spectra.modules." + moduleName);
            assertThat(condition.name()).containsExactly("enabled");
            assertThat(condition.havingValue()).isEqualTo("true");
            assertThat(condition.matchIfMissing()).isTrue();
        });
    }

    @Test
    void autoConfigurationImportsMustRegisterEachModuleEntry() {
        MODULE_IMPORTS.forEach((moduleClass, expectedEntry) -> assertThat(readImports(moduleClass))
                .as("自动配置资源必须注册模块入口：%s", moduleClass.getName())
                .anyMatch(resource -> resource.contains(expectedEntry)));
    }

    @Test
    void coreOnlyAssemblyMustStartWithoutOptionalModules() {
        new ApplicationContextRunner()
                .withUserConfiguration(ModuleAssemblyConfiguration.class)
                .withPropertyValues(
                        "spectra.modules.oa.enabled=false",
                        "spectra.modules.workflow.enabled=false",
                        "spectra.modules.notification.enabled=false",
                        "spectra.modules.upload.enabled=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(ModuleAssembly.class).enabledModules())
                            .containsExactly(ModuleAssembly.CORE);
                });
    }

    @Test
    void coreAndOaAssemblyMustIncludeOaAdaptersWithoutChangingCore() {
        new ApplicationContextRunner()
                .withUserConfiguration(ModuleAssemblyConfiguration.class)
                .withPropertyValues(
                        "spectra.modules.oa.enabled=true",
                        "spectra.modules.workflow.enabled=true",
                        "spectra.modules.notification.enabled=true",
                        "spectra.modules.upload.enabled=true")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context.getBean(ModuleAssembly.class).enabledModules())
                            .contains(ModuleAssembly.CORE, ModuleAssembly.OA,
                                    ModuleAssembly.WORKFLOW, ModuleAssembly.NOTIFICATION, ModuleAssembly.UPLOAD);
                    assertThat(CoreModule.class.getPackageName())
                            .isEqualTo("com.devops00.spectra.core");
                });
    }

    @Test
    void futureErpAssemblyMustBeAllowedWithoutCoreChanges() {
        var futureErp = ModuleAssembly.of(ModuleAssembly.CORE, "erp");

        assertThatCode(futureErp::validate).doesNotThrowAnyException();
        assertThat(futureErp.enabledModules()).containsExactlyInAnyOrder(ModuleAssembly.CORE, "erp");
    }

    @Test
    void missingRequiredAdapterMustFailWithAssemblyMessage() {
        new ApplicationContextRunner()
                .withUserConfiguration(ModuleAssemblyConfiguration.class)
                .withPropertyValues(
                        "spectra.modules.oa.enabled=true",
                        "spectra.modules.workflow.enabled=false",
                        "spectra.modules.notification.enabled=true",
                        "spectra.modules.upload.enabled=true")
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasMessageContaining("spectra.modules.oa.enabled")
                            .hasMessageContaining("workflow")
                            .hasMessageContaining("必需 adapter");
                });
    }

    @Test
    void disabledOptionalModulesMustNotRegisterControllersSchedulersOrHealthContributors() {
        assertDisabled(OaModule.class, "spectra.modules.oa.enabled", ApplicationController.class,
                ContractReminderScheduledHandler.class);
        assertDisabled(WorkflowModule.class, "spectra.modules.workflow.enabled", TaskController.class, null);
        assertDisabled(NotificationModule.class, "spectra.modules.notification.enabled", NotificationController.class,
                NotificationCleanupScheduledHandler.class, NotificationHealthIndicator.class);
        assertDisabled(UploadModule.class, "spectra.modules.upload.enabled", FileUploadController.class,
                FileUploadCleanupScheduledHandler.class, FileStorageHealthIndicator.class);
    }

    private static void assertDisabled(Class<?> moduleClass, String property, Class<?> controller,
                                       Class<?> scheduler, Class<?>... healthContributors) {
        new ApplicationContextRunner()
                .withUserConfiguration(moduleClass)
                .withPropertyValues(property + "=false")
                .run(context -> {
                    assertThat(context).hasNotFailed();
                    assertThat(context).doesNotHaveBean(controller);
                    if (scheduler != null) {
                        assertThat(context).doesNotHaveBean(scheduler);
                    }
                    assertThat(context.getBeansOfType(DependencyHealthContributor.class)).isEmpty();
                    for (var healthContributor : healthContributors) {
                        assertThat(context).doesNotHaveBean(healthContributor);
                    }
                });
    }

    private static java.util.List<String> readImports(Class<?> moduleClass) {
        var resourceName = "META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports";
        try {
            var resources = moduleClass.getClassLoader().getResources(resourceName);
            var contents = java.util.Collections.list(resources)
                    .stream()
                    .map(resource -> {
                        try (var inputStream = resource.openStream()) {
                            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
                        } catch (IOException exception) {
                            throw new UncheckedIOException(exception);
                        }
                    })
                    .collect(Collectors.toList());
            assertThat(contents)
                    .as("模块必须提供 AutoConfiguration.imports：%s", moduleClass.getName())
                    .isNotEmpty();
            return contents;
        } catch (IOException exception) {
            throw new UncheckedIOException(exception);
        }
    }
}
