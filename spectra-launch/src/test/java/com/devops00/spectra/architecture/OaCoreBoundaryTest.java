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

package com.devops00.spectra.architecture;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * OA 只能通过 common Port 访问 Core 能力的边界测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class OaCoreBoundaryTest {

    @Test
    void oaProductionSourcesMustNotImportCoreTypes() throws IOException {
        var violations = uncheckedJavaSources(resolveBackendPath(
                "spectra-modules/spectra-oa/src/main/java")).stream()
                .flatMap(path -> imports(path).stream()
                        .filter(line -> line.startsWith("import com.devops00.spectra.core."))
                        .map(line -> path + ": " + line))
                .toList();

        assertThat(violations)
                .as("OA 生产代码不得直接依赖 core Entity、Mapper、Service 或实现类")
                .isEmpty();
    }

    @Test
    void oaProductionSourcesMustUseWorkflowPublicApi() throws IOException {
        var violations = uncheckedJavaSources(resolveBackendPath(
                "spectra-modules/spectra-oa/src/main/java")).stream()
                .flatMap(path -> imports(path).stream()
                        .filter(line -> line.startsWith("import com.devops00.spectra.workflow."))
                        .filter(line -> !line.startsWith("import com.devops00.spectra.workflow.api."))
                        .map(line -> path + ": " + line))
                .toList();

        assertThat(violations)
                .as("OA 生产代码只能依赖 Workflow public API")
                .isEmpty();
    }

    /**
     * 处理边界相关数据。
     */
    private static List<Path> uncheckedJavaSources(Path sourceRoot) {
        try (var paths = Files.walk(sourceRoot)) {
            return paths.filter(path -> path.toString().endsWith(".java")).toList();
        } catch (IOException exception) {
            throw new IllegalStateException("无法扫描 OA Java 源码: " + sourceRoot, exception);
        }
    }

    /**
     * 处理边界相关数据。
     */
    private static List<String> imports(Path sourceFile) {
        try (var lines = Files.lines(sourceFile)) {
            return lines.map(String::trim)
                    .filter(line -> line.startsWith("import "))
                    .toList();
        } catch (IOException exception) {
            throw new IllegalStateException("无法读取 OA Java 源码: " + sourceFile, exception);
        }
    }

    /**
     * 解析路径。
     */
    private static Path resolveBackendPath(String relativePath) {
        Path current = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "."))
                .toAbsolutePath()
                .normalize();
        while (current != null) {
            if (Files.exists(current.resolve("spectra-common/pom.xml"))) {
                return current.resolve(relativePath);
            }
            current = current.getParent();
        }
        throw new IllegalStateException("无法定位 Maven 工程文件: " + relativePath);
    }
}
