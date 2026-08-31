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

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 重复基础实现清理边界测试。
 *
 * <p>只约束明确完成归属迁移的类型；业务专属状态和模块内部探针不在本测试的删除范围内。</p>
 */
class DuplicateImplementationCleanupTest {

    @Test
    void coreOnlyRedisCacheKeysMustNotRemainInCommon() {
        var backend = resolveBackendPath();

        assertThat(Files.exists(backend.resolve(
                "spectra-common/src/main/java/com/devops00/spectra/common/constant/RedisCacheKey.java")))
                .as("只被 Core 使用的 Redis key 不应继续放在 common")
                .isFalse();
        assertThat(Files.exists(backend.resolve(
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/common/constant/RedisCacheKey.java")))
                .as("Core 的 Redis key 应归入 Core")
                .isTrue();
    }

    @Test
    void uploadSpecificExceptionsMustNotRemainInCommon() {
        var backend = resolveBackendPath();

        assertThat(Files.exists(backend.resolve(
                "spectra-common/src/main/java/com/devops00/spectra/common/exception/FileUploadException.java")))
                .as("Upload 专属异常不应继续放在 common")
                .isFalse();
        assertThat(Files.exists(backend.resolve(
                "spectra-common/src/main/java/com/devops00/spectra/common/exception/FileTypeException.java")))
                .as("未使用的 Upload 专属旧异常不应继续放在 common")
                .isFalse();
    }

    @Test
    void onlineUserViewMustHaveOneSecurityOwnedDefinition() {
        var backend = resolveBackendPath();

        assertThat(Files.exists(backend.resolve(
                "spectra-modules/spectra-core/src/main/java/com/devops00/spectra/core/user/javabean/vo/UserOnlineVO.java")))
                .as("在线用户视图不应在 Core 保留第二份定义")
                .isFalse();
        assertThat(Files.exists(backend.resolve(
                "spectra-starter/spectra-security-base/src/main/java/com/devops00/spectra/security/base/javabean/vo/UserOnlineVO.java")))
                .as("在线用户会话视图应由 security-base 统一提供")
                .isTrue();
    }

    private static Path resolveBackendPath() {
        Path current = Path.of(System.getProperty("maven.multiModuleProjectDirectory", "."))
                .toAbsolutePath()
                .normalize();
        while (current != null) {
            if (Files.exists(current.resolve("spectra-common/pom.xml"))) {
                return current;
            }
            current = current.getParent();
        }
        throw new IllegalStateException("无法定位 Maven 工程目录");
    }
}
