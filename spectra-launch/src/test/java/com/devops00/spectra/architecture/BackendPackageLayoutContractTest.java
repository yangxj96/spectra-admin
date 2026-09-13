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
 * 验证 {@code BackendPackageLayoutContractTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class BackendPackageLayoutContractTest {

    private static final List<String> FORBIDDEN_PACKAGE_FRAGMENTS = List.of(
            "/core/audit/query/",
            "/core/security/authorization/entity/",
            "/core/user/imports/",
            "/core/upload/validation/",
            "/framework/security/converter/");

    @Test
    void backendTypesMustUseDocumentedRolePackages() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        try (var paths = Files.walk(backend)) {
            var misplacedTypes = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("/src/main/java/"))
                    .filter(path -> FORBIDDEN_PACKAGE_FRAGMENTS.stream()
                            .anyMatch(fragment -> path.toString().replace('\\', '/').contains(fragment)))
                    .map(Path::toString)
                    .toList();
            assertThat(misplacedTypes)
                    .as("types must use their documented service, policy, observability, javabean, validator, or converter package")
                    .isEmpty();
        }
    }

    @Test
    void auditServiceAndPolicyHelpersMustNotLiveInDomainRoot() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        try (var paths = Files.walk(backend)) {
            var misplacedTypes = paths
                    .filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("/src/main/java/"))
                    .filter(path -> path.toString().replace('\\', '/').endsWith("/core/audit/CoreAuditService.java")
                            || path.toString()
                                    .replace('\\', '/')
                                    .endsWith("/core/security/authorization/GrantBoundaryPolicy.java"))
                    .map(Path::toString)
                    .toList();
            assertThat(misplacedTypes)
                    .as("application services and policies must live in role-specific packages")
                    .isEmpty();
        }
    }
}
