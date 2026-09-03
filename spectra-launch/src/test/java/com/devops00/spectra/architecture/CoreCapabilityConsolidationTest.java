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
import com.devops00.spectra.launch.configuration.ModuleAssembly;
import org.mybatis.spring.annotation.MapperScan;
import org.junit.jupiter.api.Test;
import org.springframework.mock.env.MockEnvironment;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 通知和文件上传合并到 Core 的架构契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
class CoreCapabilityConsolidationTest {

    @Test
    void coreMustContainNotificationAndUploadPackages() {
        assertThat(Files.exists(resolveBackendPath(
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/notification")))
                .isTrue();
        assertThat(Files.exists(resolveBackendPath(
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/upload")))
                .isTrue();
    }

    @Test
    void standaloneNotificationAndUploadModulesMustBeRemoved() {
        var modulesRoot = resolveBackendPath("spectra-modules");
        assertThat(Files.exists(modulesRoot.resolve("spectra-" + "notification/pom.xml"))).isFalse();
        assertThat(Files.exists(modulesRoot.resolve("spectra-" + "upload/pom.xml"))).isFalse();
    }

    @Test
    void launchAndModuleAggregatorMustNotDeclareRemovedArtifacts() throws IOException {
        var launchPom = Files.readString(resolveBackendPath("spectra-launch/pom.xml"));
        var modulesPom = Files.readString(resolveBackendPath("spectra-modules/pom.xml"));
        assertThat(launchPom).doesNotContain("<artifactId>spectra-" + "notification</artifactId>")
                .doesNotContain("<artifactId>spectra-" + "upload</artifactId>");
        assertThat(modulesPom).doesNotContain("<module>spectra-" + "notification</module>")
                .doesNotContain("<module>spectra-" + "upload</module>");
    }

    @Test
    void coreMustOwnMergedRuntimeDependencies() throws IOException {
        var corePom = Files.readString(resolveBackendPath("spectra-modules/spectra-core/pom.xml"));
        assertThat(corePom).contains("<artifactId>spring-boot-starter-mail</artifactId>")
                .contains("<artifactId>s3</artifactId>")
                .contains("<artifactId>tika-core</artifactId>");
    }

    @Test
    void notificationAndUploadAreNotOptionalModuleAssemblyEntries() {
        var environment = new MockEnvironment()
                .withProperty("spectra.modules." + "notification.enabled", "false")
                .withProperty("spectra.modules." + "upload.enabled", "false");
        assertThat(ModuleAssembly.from(environment).enabledModules())
                .containsExactlyInAnyOrder(ModuleAssembly.CORE, ModuleAssembly.WORKFLOW, ModuleAssembly.OA);
    }

    @Test
    void coreMustExposeMergedCapabilityTypes() {
        List.of(
                "com.devops00.spectra.core.notification.service.impl.NotificationGatewayImpl",
                "com.devops00.spectra.core.upload.service.FileAssetApplicationService",
                "com.devops00.spectra.core.notification.health.NotificationHealthIndicator",
                "com.devops00.spectra.core.upload.health.FileStorageHealthIndicator",
                "com.devops00.spectra.core.notification.mapper.NotificationTaskMapper",
                "com.devops00.spectra.core.upload.mapper.FileAssetMapper")
                .forEach(className -> assertThat(org.assertj.core.api.Assertions.catchThrowable(() -> Class.forName(className)))
                        .as("Core 必须在 classpath 中提供合并能力类型: %s", className)
                        .isNull());
    }

    @Test
    void coreMustScanAllCoreMappers() {
        assertThat(CoreModule.class.getAnnotation(MapperScan.class).value())
                .containsExactly("com.devops00.spectra.core.**.mapper");
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
