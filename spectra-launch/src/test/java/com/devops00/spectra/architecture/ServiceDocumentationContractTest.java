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
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 公共服务和 Framework 技术组件的源码契约文档清单测试。
 *
 * <p>接口是服务语义的唯一说明位置；实现类的方法只保留必要的算法和安全不变量说明，避免接口与
 * 实现的两份 Javadoc 漂移。</p>
 */
class ServiceDocumentationContractTest {

    private static final List<String> SERVICE_SOURCE_ROOTS = List.of(
            "spectra-common/src/main/java",
            "spectra-modules/spectra-core/src/main/java",
            "spectra-modules/spectra-oa/src/main/java",
            "spectra-modules/spectra-workflow/src/main/java");

    private static final Pattern INTERFACE_PATTERN = Pattern.compile("\\binterface\\s+[A-Za-z_$][\\w$]*Service\\b");

    @Test
    void serviceInterfacesAndFrameworkContractsMustBeDocumented() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        List<String> violations = new ArrayList<>();
        int serviceMethodCount = 0;
        int frameworkContractCount = 0;

        for (String root : SERVICE_SOURCE_ROOTS) {
            for (Path sourceFile : SourceContractTestSupport.javaFiles(backend.resolve(root))) {
                String source = Files.readString(sourceFile);
                if (!sourceFile.getFileName().toString().endsWith("Service.java")
                        || sourceFile.getFileName().toString().endsWith("ServiceImpl.java")
                        || !INTERFACE_PATTERN.matcher(SourceContractTestSupport.withoutComments(source)).find()) {
                    continue;
                }
                var interfaceMatcher = INTERFACE_PATTERN.matcher(SourceContractTestSupport.withoutComments(source));
                interfaceMatcher.find();
                int interfaceBodyOffset = SourceContractTestSupport.withoutComments(source)
                        .indexOf('{', interfaceMatcher.end());
                List<String> typeNames = SourceContractTestSupport.typeNames(source);
                for (SourceContractTestSupport.MethodDeclaration method : SourceContractTestSupport.methods(source)) {
                    if (typeNames.contains(method.name())
                            || !SourceContractTestSupport.isDirectMember(source, interfaceBodyOffset, method.offset())) {
                        continue;
                    }
                    serviceMethodCount++;
                    checkDocumentation(sourceFile, source, method, violations, "Service 接口方法");
                }
            }
        }

        Path frameworkRoot = backend.resolve("spectra-framework/src/main/java");
        for (Path sourceFile : SourceContractTestSupport.javaFiles(frameworkRoot)) {
            String source = Files.readString(sourceFile);
            List<SourceContractTestSupport.TypeDeclaration> publicTypes = SourceContractTestSupport.publicTypes(source);
            for (SourceContractTestSupport.TypeDeclaration type : publicTypes) {
                frameworkContractCount++;
                checkTypeDocumentation(sourceFile, source, type, violations);
            }
            for (SourceContractTestSupport.MethodDeclaration method : SourceContractTestSupport.methods(source)) {
                if (isConstructor(method, publicTypes)) {
                    continue;
                }
                if (method.isPublic()) {
                    frameworkContractCount++;
                    checkDocumentation(sourceFile, source, method, violations, "Framework 公开方法");
                }
            }
        }

        violations.addAll(findDuplicateImplementationDocumentation(backend));
        violations.addAll(findGenericDocumentationPlaceholders(backend));
        System.out.println("ServiceDocumentationContractTest: serviceMethods=" + serviceMethodCount
                + ", frameworkContracts=" + frameworkContractCount
                + ", violations=" + violations.size());
        violations.forEach(System.out::println);
        assertThat(violations).as("公共契约注释缺口或 ServiceImpl 重复注释").isEmpty();
    }

    private static void checkTypeDocumentation(Path sourceFile, String source,
                                               SourceContractTestSupport.TypeDeclaration type,
                                               List<String> violations) {
        String javadoc = SourceContractTestSupport.javadocBefore(source.substring(0, type.offset()));
        if (javadoc == null) {
            violations.add(location(sourceFile, source, type.offset()) + " 缺少 Framework 公开类型 Javadoc: " + type.name());
        }
    }

    private static void checkDocumentation(Path sourceFile, String source,
                                           SourceContractTestSupport.MethodDeclaration method,
                                           List<String> violations, String category) {
        String javadoc = SourceContractTestSupport.javadocBefore(method.prefix());
        if (javadoc == null) {
            violations.add(location(sourceFile, source, method.offset()) + " " + category
                    + "缺少 Javadoc: " + method.name());
            return;
        }
        for (String parameterName : SourceContractTestSupport.parameterNames(method.parameters())) {
            if (!javadoc.matches("(?s).*@param\\s+" + Pattern.quote(parameterName) + "\\b.*")) {
                violations.add(location(sourceFile, source, method.offset()) + " " + category
                        + "缺少 @param " + parameterName + ": " + method.name());
            }
        }
        for (String thrownType : method.thrownTypes()) {
            if (!javadoc.matches("(?s).*@throws\\s+" + Pattern.quote(thrownType) + "\\b.*")) {
                violations.add(location(sourceFile, source, method.offset()) + " " + category
                        + "缺少 @throws " + thrownType + ": " + method.name());
            }
        }
        if (!method.returnType().equals("void") && !javadoc.contains("@return")) {
            violations.add(location(sourceFile, source, method.offset()) + " " + category
                    + "缺少 @return: " + method.name());
        }
    }

    private static List<String> findDuplicateImplementationDocumentation(Path backend) throws IOException {
        List<String> violations = new ArrayList<>();
        List<Path> roots = List.of(
                backend.resolve("spectra-modules/spectra-core/src/main/java"),
                backend.resolve("spectra-modules/spectra-oa/src/main/java"),
                backend.resolve("spectra-modules/spectra-workflow/src/main/java"));
        for (Path root : roots) {
            for (Path sourceFile : SourceContractTestSupport.javaFiles(root)) {
                if (!sourceFile.getFileName().toString().endsWith("ServiceImpl.java")) {
                    continue;
                }
                String source = Files.readString(sourceFile);
                for (SourceContractTestSupport.MethodDeclaration method : SourceContractTestSupport.methods(source)) {
                    String prefix = source.substring(0, method.offset());
                    if (hasOverrideJavadoc(prefix)
                            && SourceContractTestSupport.javadocBefore(prefix).matches("(?s).*@(param|return|throws)\\b.*")) {
                        violations.add(location(sourceFile, source, method.offset())
                                + " ServiceImpl 不应复制接口 Javadoc: " + method.name());
                    }
                }
            }
        }
        return violations;
    }

    private static List<String> findGenericDocumentationPlaceholders(Path backend) throws IOException {
        List<String> violations = new ArrayList<>();
        List<Path> roots = new ArrayList<>(SERVICE_SOURCE_ROOTS.stream()
                .map(backend::resolve)
                .toList());
        roots.add(backend.resolve("spectra-framework/src/main/java"));
        Pattern placeholder = Pattern.compile(
                "@return\\s+(?:本次.*操作产生的 .*?(?:数据|值)|当前 Framework 处理产生的 .*|"
                        + "按输入条件解析出的 .*数据|按标识解析出的 .*按接口约定.*|写入后的 .*对象或处理回执|"
                        + "符合查询条件的 .*数据集合；无匹配时返回空集合|按分页条件和业务筛选得到的 .*结果页，包含总数、当前页和记录列表|"
                        + "布尔判断结果，true 表示当前条件成立|按当前安全上下文解析出的数据|获取到的数据|分页结果)$");
        for (Path root : roots) {
            for (Path sourceFile : SourceContractTestSupport.javaFiles(root)) {
                List<String> lines = Files.readAllLines(sourceFile);
                for (int index = 0; index < lines.size(); index++) {
                    if (placeholder.matcher(lines.get(index)).find()) {
                        violations.add(sourceFile + ":" + (index + 1) + " 返回值注释仍为泛化占位描述");
                    }
                }
            }
        }
        return violations;
    }

    private static boolean isConstructor(SourceContractTestSupport.MethodDeclaration method,
                                         List<SourceContractTestSupport.TypeDeclaration> types) {
        return types.stream().anyMatch(type -> type.name().equals(method.name()));
    }

    private static boolean hasOverrideJavadoc(String prefix) {
        return Pattern.compile("(?s)/\\*\\*.*?\\*/\\s*(?:@[A-Za-z_$][\\w$.]*(?:\\([^;]*?\\))?\\s*)*@Override\\s*$")
                .matcher(prefix)
                .find();
    }

    private static String location(Path sourceFile, String source, int offset) {
        return sourceFile + ":" + SourceContractTestSupport.lineNumber(source, offset);
    }
}
