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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 架构源码契约测试共用的轻量 Java 源码扫描器。
 *
 * <p>测试只需要识别公开声明和 Javadoc 标签，不需要把生产源码重新解析成完整 AST；扫描器会保留
 * 换行位置，因而失败信息可以直接定位到文件和行号。</p>
 */
final class SourceContractTestSupport {

    private static final Pattern METHOD_PATTERN = Pattern.compile(
            "(?m)^[\\t ]*(?:(?:public|protected|private|static|default|synchronized|final|abstract)\\s+)*"
                    + "(?<modifiers>(?:(?:public|protected|private|static|default|synchronized|final|abstract)\\s+)*)"
                    + "(?:(?:<[^;{}]+>)\\s+)?"
                    + "(?:(?:@[A-Za-z_$][\\w$]*(?:\\([^;{}]*\\))?)\\s+)*"
                    + "(?<returnType>[A-Za-z_$][\\w$]*(?:\\s*\\.[\\s]*[A-Za-z_$][\\w$]*)?"
                    + "(?:\\s*<[^;{}]+>)?(?:\\s*\\[\\s*\\])?(?:\\s*\\.\\.\\.)?)\\s+"
                    + "(?<name>[A-Za-z_$][\\w$]*)\\s*\\((?<parameters>[^;{}]*)\\)"
                    + "\\s*(?:(?<throwsClause>throws\\s+[^;{}]+)\\s*)?(?:;|\\{)");

    private static final Pattern TYPE_PATTERN = Pattern.compile(
            "(?m)^[\\t ]*public\\s+(?:abstract\\s+|final\\s+|sealed\\s+|non-sealed\\s+)*"
                    + "(?:class|interface|record|enum|@interface)\\s+(?<name>[A-Za-z_$][\\w$]*)");

    private static final Pattern JAVADOC_PATTERN = Pattern.compile("(?s)/\\*\\*.*?\\*/");

    private SourceContractTestSupport() {
    }

    static Path resolveBackendPath() {
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

    static List<Path> javaFiles(Path root) throws IOException {
        try (var paths = Files.walk(root)) {
            return paths.filter(path -> path.toString().endsWith(".java")).sorted().toList();
        }
    }

    static List<MethodDeclaration> methods(String source) {
        String sourceWithoutComments = withoutComments(source);
        Matcher matcher = METHOD_PATTERN.matcher(sourceWithoutComments);
        List<MethodDeclaration> declarations = new ArrayList<>();
        while (matcher.find()) {
            declarations.add(new MethodDeclaration(
                    matcher.start(),
                    matcher.group("modifiers"),
                    matcher.group("returnType"),
                    matcher.group("name"),
                    matcher.group("parameters"),
                    matcher.group("throwsClause"),
                    source.substring(0, matcher.start())));
        }
        return declarations;
    }

    static List<TypeDeclaration> publicTypes(String source) {
        Matcher matcher = TYPE_PATTERN.matcher(withoutComments(source));
        List<TypeDeclaration> declarations = new ArrayList<>();
        while (matcher.find()) {
            declarations.add(new TypeDeclaration(matcher.start(), matcher.group("name")));
        }
        return declarations;
    }

    static List<String> typeNames(String source) {
        Matcher matcher = Pattern.compile(
                "(?m)^[\\t ]*(?:public\\s+|protected\\s+|private\\s+|abstract\\s+|final\\s+)*"
                        + "(?:class|interface|record|enum|@interface)\\s+(?<name>[A-Za-z_$][\\w$]*)")
                .matcher(withoutComments(source));
        List<String> names = new ArrayList<>();
        while (matcher.find()) {
            names.add(matcher.group("name"));
        }
        return names;
    }

    static String javadocBefore(String prefix) {
        String candidate = prefix;
        while (true) {
            String trimmed = candidate.stripTrailing();
            if (trimmed.isEmpty()) {
                return null;
            }
            int lineStart = Math.max(trimmed.lastIndexOf('\n'), trimmed.lastIndexOf('\r')) + 1;
            String lastLine = trimmed.substring(lineStart).strip();
            if (lastLine.startsWith("@")) {
                candidate = trimmed.substring(0, lineStart);
                continue;
            }
            Matcher matcher = JAVADOC_PATTERN.matcher(trimmed);
            String javadoc = null;
            while (matcher.find()) {
                javadoc = matcher.group();
            }
            if (javadoc != null && trimmed.endsWith(javadoc)) {
                return javadoc;
            }
            return null;
        }
    }

    static boolean isDirectMember(String source, int bodyOffset, int memberOffset) {
        String sourceWithoutComments = withoutComments(source);
        String body = sourceWithoutComments.substring(bodyOffset, memberOffset);
        return body.chars().filter(value -> value == '{').count()
                - body.chars().filter(value -> value == '}').count() == 1;
    }

    static List<String> parameterNames(String parameters) {
        List<String> names = new ArrayList<>();
        for (String parameter : splitParameters(parameters)) {
            String normalized = parameter.replaceAll("@[A-Za-z_$][\\w$]*(?:\\([^)]*\\))?", " ")
                    .replace("final ", " ")
                    .trim();
            Matcher matcher = Pattern.compile("([A-Za-z_$][\\w$]*)\\s*(?:\\[\\s*\\])?(?:\\.\\.\\.)?$")
                    .matcher(normalized);
            if (matcher.find()) {
                names.add(matcher.group(1));
            }
        }
        return names;
    }

    static String lineNumber(String source, int offset) {
        return Integer.toString((int) source.substring(0, offset).chars().filter(value -> value == '\n').count() + 1);
    }

    static String withoutComments(String source) {
        StringBuilder result = new StringBuilder(source.length());
        boolean lineComment = false;
        boolean blockComment = false;
        boolean stringLiteral = false;
        boolean characterLiteral = false;
        boolean escaped = false;
        for (int index = 0; index < source.length(); index++) {
            char current = source.charAt(index);
            char next = index + 1 < source.length() ? source.charAt(index + 1) : '\0';
            if (lineComment) {
                result.append(current == '\n' ? '\n' : ' ');
                if (current == '\n') {
                    lineComment = false;
                }
                continue;
            }
            if (blockComment) {
                result.append(current == '\n' ? '\n' : ' ');
                if (current == '*' && next == '/') {
                    result.append(' ');
                    index++;
                    blockComment = false;
                }
                continue;
            }
            if (stringLiteral) {
                result.append(current == '\n' ? '\n' : ' ');
                if (!escaped && current == '"') {
                    stringLiteral = false;
                }
                escaped = !escaped && current == '\\';
                if (current != '\\') {
                    escaped = false;
                }
                continue;
            }
            if (characterLiteral) {
                result.append(current == '\n' ? '\n' : ' ');
                if (!escaped && current == '\'') {
                    characterLiteral = false;
                }
                escaped = !escaped && current == '\\';
                if (current != '\\') {
                    escaped = false;
                }
                continue;
            }
            if (current == '/' && next == '/') {
                result.append("  ");
                index++;
                lineComment = true;
            } else if (current == '/' && next == '*') {
                result.append("  ");
                index++;
                blockComment = true;
            } else {
                result.append(current);
                if (current == '"') {
                    stringLiteral = true;
                } else if (current == '\'') {
                    characterLiteral = true;
                }
            }
        }
        return result.toString();
    }

    private static List<String> splitParameters(String parameters) {
        if (parameters.isBlank()) {
            return List.of();
        }
        List<String> result = new ArrayList<>();
        int start = 0;
        int angleDepth = 0;
        int parenthesisDepth = 0;
        for (int index = 0; index < parameters.length(); index++) {
            char current = parameters.charAt(index);
            if (current == '<') {
                angleDepth++;
            } else if (current == '>') {
                angleDepth = Math.max(0, angleDepth - 1);
            } else if (current == '(') {
                parenthesisDepth++;
            } else if (current == ')') {
                parenthesisDepth = Math.max(0, parenthesisDepth - 1);
            } else if (current == ',' && angleDepth == 0 && parenthesisDepth == 0) {
                result.add(parameters.substring(start, index).trim());
                start = index + 1;
            }
        }
        result.add(parameters.substring(start).trim());
        return result;
    }

    record MethodDeclaration(int offset, String modifiers, String returnType, String name, String parameters,
                             String throwsClause, String prefix) {
        boolean isPublic() {
            return modifiers.contains("public");
        }

        List<String> thrownTypes() {
            if (throwsClause == null) {
                return List.of();
            }
            return List.of(throwsClause.substring("throws".length()).trim().split("\\s*,\\s*"));
        }
    }

    record TypeDeclaration(int offset, String name) {
    }
}
