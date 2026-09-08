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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Java 25 编译级别和语言特性使用边界测试。
 *
 * <p>本测试只约束语言级别和 {@code var} 的位置，不以局部变量推断比例作为质量指标。</p>
 */
class JavaLanguageUsageContractTest {

    private static final List<String> MODULES = List.of(
            "",
            "spectra-config",
            "spectra-common",
            "spectra-framework",
            "spectra-modules",
            "spectra-modules/spectra-core",
            "spectra-modules/spectra-oa",
            "spectra-modules/spectra-workflow",
            "spectra-launch");

    private static final List<String> SOURCE_ROOTS = List.of(
            "spectra-common/src/main/java",
            "spectra-framework/src/main/java",
            "spectra-modules/spectra-core/src/main/java",
            "spectra-modules/spectra-oa/src/main/java",
            "spectra-modules/spectra-workflow/src/main/java",
            "spectra-launch/src/main/java");

    private static final Pattern VAR_LOCAL = Pattern.compile(
            "\\bvar\\s+[A-Za-z_$][\\w$]*\\s*(?:=|:)");
    private static final Pattern VAR_ANY = Pattern.compile("\\bvar\\b");

    @Test
    void projectUsesJava25AndVarOnlyForLocalInference() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        for (String module : MODULES) {
            String pom = Files.readString(backend.resolve(module).resolve("pom.xml"));
            assertThat(pom).as("模块必须声明 Java 25: " + module)
                    .contains("<java.version>25</java.version>")
                    .contains("<maven.compiler.source>25</maven.compiler.source>")
                    .contains("<maven.compiler.target>25</maven.compiler.target>");
        }

        List<String> varInventory = new ArrayList<>();
        List<String> invalidVarLocations = new ArrayList<>();
        Map<String, Integer> varCountsByModule = new LinkedHashMap<>();
        for (String root : SOURCE_ROOTS) {
            String module = root.substring(0, root.indexOf("/src"));
            int moduleVarCount = 0;
            for (Path sourceFile : SourceContractTestSupport.javaFiles(backend.resolve(root))) {
                String source = Files.readString(sourceFile);
                List<String> sourceVarInventory = findVarLocations(sourceFile, source);
                moduleVarCount += sourceVarInventory.size();
                varInventory.addAll(sourceVarInventory);
                invalidVarLocations.addAll(findInvalidVarLocations(sourceFile, source));
            }
            varCountsByModule.put(module, moduleVarCount);
        }

        System.out.println("JavaLanguageUsageContractTest: varLocalDeclarationsByModule=" + varCountsByModule);
        assertThat(varInventory).as("生产源码应保留可读的 var 局部变量使用证据").isNotEmpty();
        assertThat(invalidVarLocations).as("var 不得出现在字段或方法签名").isEmpty();
    }

    private static List<String> findVarLocations(Path sourceFile, String source) {
        List<String> locations = new ArrayList<>();
        Matcher matcher = VAR_LOCAL.matcher(SourceContractTestSupport.withoutComments(source));
        while (matcher.find()) {
            locations.add(sourceFile + ":" + SourceContractTestSupport.lineNumber(source, matcher.start()));
        }
        return locations;
    }

    private static List<String> findInvalidVarLocations(Path sourceFile, String source) {
        String maskedSource = SourceContractTestSupport.withoutComments(source);
        List<String> locations = new ArrayList<>();

        Matcher anyMatcher = VAR_ANY.matcher(maskedSource);
        while (anyMatcher.find()) {
            Matcher localMatcher = VAR_LOCAL.matcher(maskedSource);
            localMatcher.region(anyMatcher.start(), maskedSource.length());
            if (!localMatcher.lookingAt()) {
                locations.add(sourceFile + ":"
                        + SourceContractTestSupport.lineNumber(source, anyMatcher.start()));
            }
        }
        return locations;
    }
}
