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
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * common 层技术依赖边界测试。
 *
 * <p>common 只承载项目契约、基础值对象和 JDK/Jakarta 基础类型；Spring、MyBatis、PostgreSQL 和 Servlet
 * 适配必须位于 framework 或具体业务模块，避免低层契约包反向绑定运行时实现。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/9
 */
class CommonLayerDependencyTest {

    private static final List<String> FORBIDDEN_IMPORT_PREFIXES = List.of(
            "org.springframework",
            "com.baomidou",
            "org.postgresql",
            "org.apache.ibatis",
            "jakarta.servlet");

    /**
     * 迁移前的 12 个 common 技术耦合文件清单，作为边界收敛的审计范围记录；迁移完成后不要求这些路径继续存在。
     */
    private static final List<String> LEGACY_TECHNICAL_FILES = List.of(
            "common/base/BaseEntity.java",
            "common/base/BaseService.java",
            "common/base/BaseServiceImpl.java",
            "common/base/javabean/from/PageFrom.java",
            "common/constant/ConfiguredValueType.java",
            "common/constant/RegionLevel.java",
            "common/event/FileUploadFinishEvent.java",
            "common/mybatis/PgJsonbNodeTypeHandler.java",
            "common/mybatis/PgJsonbTypeHandler.java",
            "common/mybatis/handler/UUIDTypeHandler.java",
            "common/properties/SystemProperties.java",
            "common/response/R.java");

    @Test
    void commonProductionSourcesMustNotImportRuntimeTechnology() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        Path sourceRoot = backend.resolve("spectra-common/src/main/java");
        List<String> violations = new ArrayList<>();

        for (Path sourceFile : SourceContractTestSupport.javaFiles(sourceRoot)) {
            String normalizedPath = sourceRoot.relativize(sourceFile).toString().replace('\\', '/');
            String source = Files.readString(sourceFile);
            source.lines()
                    .map(String::strip)
                    .filter(line -> line.startsWith("import "))
                    .filter(this::isForbiddenImport)
                    .forEach(line -> violations.add(normalizedPath + ": " + line));
        }

        assertThat(LEGACY_TECHNICAL_FILES).hasSize(12);
        assertThat(violations)
                .as("common 生产源码不得依赖 Spring、MyBatis、PostgreSQL、Servlet；违规项应从技术适配边界迁出")
                .isEmpty();
    }

    @Test
    void commonPomMustNotDeclareRuntimeImplementationDependencies() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        String pom = Files.readString(backend.resolve("spectra-common/pom.xml"));

        assertThat(pom)
                .doesNotContain("<artifactId>spring-boot-starter-web</artifactId>")
                .doesNotContain("<artifactId>postgresql</artifactId>")
                .doesNotContain("<artifactId>mybatis-plus-spring-boot4-starter</artifactId>");
    }

    private boolean isForbiddenImport(String line) {
        return FORBIDDEN_IMPORT_PREFIXES.stream()
                .anyMatch(prefix -> line.startsWith("import " + prefix));
    }
}
