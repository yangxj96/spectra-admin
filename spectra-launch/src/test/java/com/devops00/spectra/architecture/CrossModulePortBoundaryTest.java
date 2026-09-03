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

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 跨模块端口边界测试。
 *
 * <p>这些断言只检查源码归属和依赖方向，不检查具体业务实现；实现必须留在提供能力的模块中。</p>
 */
class CrossModulePortBoundaryTest {

    @Test
    void coreUploadMustNotDependOnCoreSecurityImplementations() throws IOException {
        var source = readJavaSources("spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/upload");

        assertThat(source).noneMatch(content -> content.contains(
                "import com.devops00.spectra.core.security.authentication.util.AuthenticationWebUtils;"));
        assertThat(source).noneMatch(content -> content.contains(
                "import com.devops00.spectra.core.security.audit."));
    }

    @Test
    void oaMustUseWorkflowPublicApi() throws IOException {
        var source = readJavaSources("spectra-modules/spectra-oa/src/main/java");

        assertThat(source).noneMatch(content -> content.contains(
                "import com.devops00.spectra.workflow.service."));
    }

    @Test
    void sharedFileContractsMustLiveInCommonPort() {
        var backend = resolveBackendPath("");
        List<String> contracts = List.of(
                "spectra-common/src/main/java/com/devops00/spectra/common/port/file/FileAccessContext.java",
                "spectra-common/src/main/java/com/devops00/spectra/common/port/file/FileAssetPort.java",
                "spectra-common/src/main/java/com/devops00/spectra/common/port/file/FileAssetSnapshot.java",
                "spectra-common/src/main/java/com/devops00/spectra/common/port/file/FileDownload.java",
                "spectra-common/src/main/java/com/devops00/spectra/common/port/file/FileReferenceCommand.java",
                "spectra-common/src/main/java/com/devops00/spectra/common/port/file/FileReferenceKey.java",
                "spectra-common/src/main/java/com/devops00/spectra/common/port/file/FileReferencePermissionChecker.java",
                "spectra-common/src/main/java/com/devops00/spectra/common/port/file/FileReferenceService.java",
                "spectra-common/src/main/java/com/devops00/spectra/common/port/file/FileReferenceView.java");

        assertThat(contracts).allMatch(contract -> Files.exists(backend.resolve(contract)),
                "文件跨模块契约必须位于 spectra-common/common/port/file");
    }

    @Test
    void securityAuditArchivePortMustLiveInCommonPort() {
        var backend = resolveBackendPath("");
        List<String> contracts = List.of(
                "spectra-common/src/main/java/com/devops00/spectra/common/port/audit/SecurityAuditArchiveBackend.java",
                "spectra-common/src/main/java/com/devops00/spectra/common/port/audit/SecurityAuditArchiveIntegrity.java",
                "spectra-common/src/main/java/com/devops00/spectra/common/port/audit/SecurityAuditArchiveReceipt.java");

        assertThat(contracts).allMatch(contract -> Files.exists(backend.resolve(contract)),
                "安全审计归档跨模块契约必须位于 spectra-common/common/port/audit");
    }

    @Test
    void authenticationWebUtilityMustLiveInFramework() {
        var backend = resolveBackendPath("");

        assertThat(Files.exists(backend.resolve(
                "spectra-framework/src/main/java/com/devops00/spectra/framework/configure/mvc/security/AuthenticationWebUtils.java")))
                .as("HTTP 认证工具属于 framework 技术适配")
                .isTrue();
        assertThat(Files.exists(backend.resolve(
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/security/authentication/util/AuthenticationWebUtils.java")))
                .as("Core 不应继续保留 HTTP 认证工具")
                .isFalse();
    }

    private static List<String> readJavaSources(String relativePath) throws IOException {
        try (var paths = Files.walk(resolveBackendPath(relativePath))) {
            return paths.filter(path -> path.toString().endsWith(".java"))
                    .map(CrossModulePortBoundaryTest::read)
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

    private static Path resolveBackendPath(String relativePath) {
        Path current = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "."))
                .toAbsolutePath()
                .normalize();
        while (current != null) {
            Path backendMarker = current.resolve("spectra-common/pom.xml");
            if (Files.exists(backendMarker)) {
                return current.resolve(relativePath);
            }
            current = current.getParent();
        }
        throw new IllegalStateException("无法定位 Maven 工程文件: " + relativePath);
    }
}
