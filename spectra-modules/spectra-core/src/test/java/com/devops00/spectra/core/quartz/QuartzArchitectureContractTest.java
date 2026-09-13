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

package com.devops00.spectra.core.quartz;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Quartz 调度基础设施入口契约。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class QuartzArchitectureContractTest {

    @Test
    void quartzConfigurationAndManagedDependencyMustExist() throws IOException {
        var configuration = QuartzTestSource.readCoreSource(
                "quartz/configuration/QuartzSchedulerConfiguration.java");
        var properties = QuartzTestSource.readCoreSource(
                "quartz/configuration/QuartzSchedulerProperties.java");
        var pom = Files.readString(QuartzTestSource.backendRoot()
                .resolve("spectra-modules/spectra-core/pom.xml"), StandardCharsets.UTF_8);

        assertThat(configuration).contains("QuartzSchedulerConfiguration");
        assertThat(configuration).contains("getJobStoreClass");
        assertThat(properties).contains("LocalDataSourceJobStore");
        assertThat(pom).contains("spring-boot-starter-quartz");
    }

    @Test
    void executionHistoryListenerMustBeAQuartzRuntimeEntryPoint() throws IOException {
        var listener = QuartzTestSource.readCoreSource(
                "quartz/listener/QuartzExecutionHistoryJobListener.java");

        assertThat(listener).contains("QuartzExecutionHistoryJobListener");
        assertThat(listener).contains("JobListener");
    }
}

/**
 * 供同包源码契约测试复用的仓库路径辅助类。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
final class QuartzTestSource {

    private QuartzTestSource() {
    }

    static Path backendRoot() {
        var current = Path.of(System.getProperty("user.dir")).toAbsolutePath();
        for (var candidate = current; candidate != null; candidate = candidate.getParent()) {
            if (Files.isDirectory(candidate.resolve("spectra-common/src/main/java"))
                    && Files.isDirectory(candidate.resolve("spectra-modules/spectra-core/src/main/java"))) {
                return candidate;
            }
        }
        throw new IllegalStateException("无法定位 spectra-admin 根目录");
    }

    static String readCoreSource(String relativePath) throws IOException {
        var path = backendRoot().resolve("spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core")
                .resolve(relativePath);
        assertThat(Files.isRegularFile(path)).as("缺少 Quartz 源文件: %s", path).isTrue();
        return Files.readString(path, StandardCharsets.UTF_8);
    }

    static String readCommonSource(String relativePath) throws IOException {
        var path = backendRoot().resolve("spectra-common/src/main/java/com/devops00/spectra/common")
                .resolve(relativePath);
        assertThat(Files.isRegularFile(path)).as("缺少 Quartz 公共契约源文件: %s", path).isTrue();
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
