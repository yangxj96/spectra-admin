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
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/** Enforces the XML-based SQL mapping contract across backend modules. */
class MapperXmlContractTest {

    private static final Pattern PACKAGE_PATTERN = Pattern.compile("(?m)^\\s*package\\s+([\\w.]+)\\s*;");
    private static final Pattern PUBLIC_INTERFACE_PATTERN = Pattern.compile("\\bpublic\\s+interface\\s+(\\w+)");
    private static final Pattern BASE_MAPPER_ENTITY_PATTERN = Pattern.compile(
            "\\bextends\\s+[^\\{;]*\\bBaseMapper\\s*<\\s*([\\w.]+)\\s*>");
    private static final Pattern MAPPER_NAMESPACE_PATTERN = Pattern.compile(
            "<mapper\\b[^>]*\\bnamespace\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']");
    private static final Pattern BASE_RESULT_MAP_PATTERN = Pattern.compile(
            "(?s)<resultMap\\b(?=[^>]*\\bid=[\\\"']BaseResultMap[\\\"'])[^>]*>");
    private static final Pattern TABLE_NAME_PATTERN = Pattern.compile("(?m)^\\s*@TableName\\s*\\(([^)]*)\\)");
    private static final Pattern TABLE_VALUE_PATTERN = Pattern.compile("\\bvalue\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']");
    private static final Pattern TABLE_SCHEMA_PATTERN = Pattern.compile("\\bschema\\s*=\\s*[\\\"']([^\\\"']+)[\\\"']");
    private static final Pattern SQL_TABLE_REFERENCE_PATTERN = Pattern.compile(
            "\\b(?:FROM|JOIN|INTO|UPDATE|DELETE\\s+FROM)\\s+([\\w]+\\.[\\w]+)", Pattern.CASE_INSENSITIVE);
    private static final Pattern TYPE_ATTRIBUTE_PATTERN = Pattern.compile("\\btype=[\\\"']([^\\\"']+)[\\\"']");
    private static final Pattern RESULT_PROPERTY_PATTERN = Pattern.compile(
            "<(?:id|result)\\s+column=\"[^\"]+\"\\s+property=\"[^\"]+\"");
    private static final Pattern SQL_ANNOTATION_PATTERN = Pattern.compile(
            "@(?:[\\w$]+\\.)*(Select|Insert|Update|Delete|SelectKey|SelectProvider|InsertProvider|UpdateProvider|DeleteProvider)\\b");
    private static final Pattern MYBATIS_ANNOTATION_PATTERN = Pattern.compile(
            "@(?:org\\.apache\\.ibatis\\.annotations\\.)?Mapper\\b");

    @Test
    void everyMyBatisMapperMustHaveSameNameXmlWithMatchingNamespaceAndNoSqlAnnotations() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        List<String> missingXml = new ArrayList<>();
        List<String> mismatchedNamespace = new ArrayList<>();
        List<String> sqlAnnotations = new ArrayList<>();

        try (var paths = Files.walk(backend)) {
            for (Path javaFile : paths.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("/src/main/java/"))
                    .filter(path -> path.getParent().toString().contains("/mapper/"))
                    .sorted()
                    .toList()) {
                String source = Files.readString(javaFile);
                if (!isMyBatisMapper(source)) {
                    continue;
                }
                String qualifiedName = qualifiedName(javaFile, source);
                Path moduleRoot = moduleRoot(javaFile);
                Path mapperResourceRoot = moduleRoot.resolve("src/main/resources/mapper");
                String expectedFileName = javaFile.getFileName().toString().replaceFirst("\\.java$", ".xml");
                List<Path> matchingFiles;
                if (Files.exists(mapperResourceRoot)) {
                    try (var xmlPaths = Files.walk(mapperResourceRoot)) {
                        matchingFiles = xmlPaths.filter(path -> path.getFileName().toString().equals(expectedFileName))
                                .toList();
                    }
                } else {
                    matchingFiles = List.of();
                }
                if (matchingFiles.isEmpty()) {
                    missingXml.add(javaFile + " -> " + expectedFileName);
                    continue;
                }

                boolean hasMatchingNamespace = matchingFiles.stream().anyMatch(xml -> hasNamespace(xml, qualifiedName));
                if (!hasMatchingNamespace) {
                    mismatchedNamespace.add(javaFile + " -> " + qualifiedName);
                }
                Matcher annotationMatcher = SQL_ANNOTATION_PATTERN.matcher(SourceContractTestSupport.withoutComments(source));
                while (annotationMatcher.find()) {
                    sqlAnnotations.add(javaFile + ":" + SourceContractTestSupport.lineNumber(source, annotationMatcher.start())
                            + " -> @" + annotationMatcher.group(1));
                }
            }
        }

        assertThat(missingXml)
                .as("every MyBatis Mapper interface must have a same-name XML under module src/main/resources/mapper")
                .isEmpty();
        assertThat(mismatchedNamespace)
                .as("each Mapper XML namespace must match the Java interface fully qualified name")
                .isEmpty();
        assertThat(sqlAnnotations)
                .as("SQL statements must be declared in XML, not MyBatis SQL or provider annotations")
                .isEmpty();
    }

    @Test
    void everyBaseMapperMustUseATableEntityAsItsResultMapType() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        List<String> missingEntities = new ArrayList<>();
        List<String> missingTableMappings = new ArrayList<>();
        List<String> mismatchedResultTypes = new ArrayList<>();

        try (var paths = Files.walk(backend)) {
            for (Path mapperJava : paths.filter(path -> path.toString().endsWith(".java"))
                    .filter(path -> path.toString().contains("/src/main/java/"))
                    .filter(path -> path.getParent().toString().contains("/mapper/"))
                    .sorted()
                    .toList()) {
                String mapperSource = Files.readString(mapperJava);
                String mapperSourceWithoutComments = SourceContractTestSupport.withoutComments(mapperSource);
                Matcher entityTypeMatcher = BASE_MAPPER_ENTITY_PATTERN.matcher(mapperSourceWithoutComments);
                if (!entityTypeMatcher.find()) {
                    continue;
                }

                String expectedType = qualifiedTypeName(entityTypeMatcher.group(1), mapperJava, mapperSource);
                Path moduleRoot = moduleRoot(mapperJava);
                Path entityJava = moduleRoot.resolve("src/main/java")
                        .resolve(expectedType.replace('.', '/') + ".java");
                if (!Files.exists(entityJava)) {
                    missingEntities.add(mapperJava + " -> " + expectedType);
                    continue;
                }
                String entitySource = SourceContractTestSupport.withoutComments(Files.readString(entityJava));
                if (!entitySource.contains("@TableName")) {
                    missingTableMappings.add(entityJava.toString());
                    continue;
                }

                Path mapperResourceRoot = moduleRoot.resolve("src/main/resources/mapper");
                String expectedFileName = mapperJava.getFileName().toString().replaceFirst("\\.java$", ".xml");
                List<Path> matchingXmlFiles;
                if (Files.exists(mapperResourceRoot)) {
                    try (var xmlPaths = Files.walk(mapperResourceRoot)) {
                        matchingXmlFiles = xmlPaths.filter(path -> path.getFileName().toString().equals(expectedFileName))
                                .toList();
                    }
                } else {
                    matchingXmlFiles = List.of();
                }
                for (Path mapperXml : matchingXmlFiles) {
                    String xml = Files.readString(mapperXml);
                    Matcher namespaceMatcher = MAPPER_NAMESPACE_PATTERN.matcher(xml);
                    if (!namespaceMatcher.find()
                            || !namespaceMatcher.group(1)
                                    .equals(qualifiedName(mapperJava,
                                            mapperSource))) {
                        continue;
                    }
                    Matcher resultMapMatcher = BASE_RESULT_MAP_PATTERN.matcher(xml);
                    if (!resultMapMatcher.find()) {
                        mismatchedResultTypes.add(mapperXml + " -> missing BaseResultMap");
                        continue;
                    }
                    Matcher typeMatcher = TYPE_ATTRIBUTE_PATTERN.matcher(resultMapMatcher.group());
                    String actualType = typeMatcher.find() ? typeMatcher.group(1) : "missing type";
                    if (!expectedType.equals(actualType)) {
                        mismatchedResultTypes.add(mapperXml + " -> expected " + expectedType
                                + ", actual " + actualType);
                    }
                }
            }
        }

        assertThat(missingEntities)
                .as("every Mapper extending BaseMapper must reference an existing Java entity")
                .isEmpty();
        assertThat(missingTableMappings)
                .as("every BaseMapper generic type must be an @TableName entity")
                .isEmpty();
        assertThat(mismatchedResultTypes)
                .as("each BaseResultMap type must match its BaseMapper generic entity")
                .isEmpty();
    }

    @Test
    void auditEventMapperMustExposeItsPartitionedTableEntityMapping() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        String expectedNamespace = "com.devops00.spectra.core.audit.mapper.AuditLogQueryMapper";
        String expectedType = "com.devops00.spectra.core.audit.javabean.entity.AuditEvent";
        List<String> violations = new ArrayList<>();
        try (var paths = Files.walk(backend)) {
            for (Path xmlFile : paths.filter(path -> path.toString().endsWith("/mapper/audit/AuditLogQueryMapper.xml"))
                    .toList()) {
                String xml = Files.readString(xmlFile);
                Matcher namespaceMatcher = MAPPER_NAMESPACE_PATTERN.matcher(xml);
                if (!namespaceMatcher.find() || !namespaceMatcher.group(1).equals(expectedNamespace)) {
                    continue;
                }
                Matcher resultMapMatcher = BASE_RESULT_MAP_PATTERN.matcher(xml);
                if (!resultMapMatcher.find()) {
                    violations.add(xmlFile + " -> missing BaseResultMap");
                } else {
                    Matcher typeMatcher = TYPE_ATTRIBUTE_PATTERN.matcher(resultMapMatcher.group());
                    if (!typeMatcher.find() || !typeMatcher.group(1).equals(expectedType)) {
                        violations.add(xmlFile + " -> BaseResultMap must use " + expectedType);
                    }
                }
                if (!xml.contains("<sql id=\"Base_Column_List\">")) {
                    violations.add(xmlFile + " -> missing Base_Column_List");
                }
            }
        }
        assertThat(violations)
                .as("the audit query mapper must keep an entity mapping for the partitioned parent table")
                .isEmpty();
    }

    @Test
    void everySchemaQualifiedMapperTableMustHaveATableEntity() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        Set<String> entityTables = new HashSet<>();
        try (var paths = Files.walk(backend)) {
            for (Path javaFile : paths.filter(path -> path.toString().contains("/src/main/java/"))
                    .filter(path -> path.toString().endsWith(".java"))
                    .toList()) {
                Matcher tableNameMatcher = TABLE_NAME_PATTERN
                        .matcher(Files.readString(javaFile));
                while (tableNameMatcher.find()) {
                    Matcher valueMatcher = TABLE_VALUE_PATTERN.matcher(tableNameMatcher.group(1));
                    if (!valueMatcher.find()) {
                        continue;
                    }
                    Matcher schemaMatcher = TABLE_SCHEMA_PATTERN.matcher(tableNameMatcher.group(1));
                    String table = schemaMatcher.find()
                            ? schemaMatcher.group(1) + "." + valueMatcher.group(1)
                            : valueMatcher.group(1);
                    entityTables.add(table.toLowerCase());
                }
            }
        }

        List<String> missingEntities = new ArrayList<>();
        try (var paths = Files.walk(backend)) {
            for (Path mapperXml : paths.filter(path -> path.toString().contains("/src/main/resources/mapper/"))
                    .filter(path -> path.toString().endsWith(".xml"))
                    .toList()) {
                Matcher tableMatcher = SQL_TABLE_REFERENCE_PATTERN.matcher(Files.readString(mapperXml));
                while (tableMatcher.find()) {
                    String table = tableMatcher.group(1).toLowerCase();
                    if (!entityTables.contains(table)) {
                        missingEntities.add(mapperXml + " -> " + table);
                    }
                }
            }
        }

        assertThat(missingEntities)
                .as("every schema-qualified table used by Mapper XML must have an @TableName entity")
                .isEmpty();
    }

    @Test
    void everyMapperXmlMustBeStoredUnderItsDomainDirectory() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        List<String> misplacedXml = new ArrayList<>();
        try (var paths = Files.walk(backend)) {
            for (Path xmlFile : paths.filter(path -> path.toString().contains("/src/main/resources/mapper/"))
                    .filter(path -> path.toString().endsWith(".xml"))
                    .sorted()
                    .toList()) {
                Path mapperResourceRoot = mapperResourceRoot(xmlFile);
                Path actualDirectory = mapperResourceRoot.relativize(xmlFile).getParent();
                Matcher namespaceMatcher = MAPPER_NAMESPACE_PATTERN.matcher(Files.readString(xmlFile));
                if (!namespaceMatcher.find()) {
                    misplacedXml.add(xmlFile + " -> missing mapper namespace");
                    continue;
                }
                Path expectedDirectory = domainDirectory(namespaceMatcher.group(1));
                if (!expectedDirectory.equals(actualDirectory)) {
                    misplacedXml.add(xmlFile + " -> expected mapper/" + expectedDirectory);
                }
            }
        }
        assertThat(misplacedXml)
                .as("each Mapper XML must be stored directly under its domain directory")
                .isEmpty();
    }

    @Test
    void everyMapperXmlMustHaveCopyrightAndEntityMappersMustHaveBaseMappings() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        List<String> missingCopyright = new ArrayList<>();
        List<String> missingBaseMappings = new ArrayList<>();
        try (var paths = Files.walk(backend)) {
            for (Path xmlFile : paths.filter(path -> path.toString().contains("/src/main/resources/mapper/"))
                    .filter(path -> path.toString().endsWith(".xml"))
                    .sorted()
                    .toList()) {
                String xml = Files.readString(xmlFile);
                if (!xml.contains("Copyright 2018-2026 yangxj96")) {
                    missingCopyright.add(xmlFile.toString());
                }

                Matcher namespaceMatcher = MAPPER_NAMESPACE_PATTERN.matcher(xml);
                if (!namespaceMatcher.find()) {
                    continue;
                }
                Path mapperResourceRoot = mapperResourceRoot(xmlFile);
                Path moduleRoot = mapperResourceRoot.getParent().getParent().getParent().getParent();
                Path mapperJava = moduleRoot.resolve("src/main/java")
                        .resolve(namespaceMatcher.group(1).replace('.', '/') + ".java");
                if (!Files.exists(mapperJava)) {
                    continue;
                }
                String mapperSource = Files.readString(mapperJava);
                boolean entityMapper = SourceContractTestSupport.withoutComments(mapperSource)
                        .matches("(?s).*\\bextends\\s+[^\\{;]*\\bBaseMapper\\s*<.*");
                if (!entityMapper) {
                    continue;
                }
                if (!xml.contains("<resultMap id=\"BaseResultMap\"")
                        || !xml.contains("<sql id=\"Base_Column_List\">")) {
                    missingBaseMappings.add(xmlFile.toString());
                }
            }
        }

        assertThat(missingCopyright)
                .as("every Mapper XML must include the standard copyright header")
                .isEmpty();
        assertThat(missingBaseMappings)
                .as("every entity Mapper extending BaseMapper must define BaseResultMap and Base_Column_List")
                .isEmpty();
    }

    @Test
    void resultMapsAndBaseColumnListsMustFollowUserMapperFormatting() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        List<String> formattingViolations = new ArrayList<>();
        try (var paths = Files.walk(backend)) {
            for (Path xmlFile : paths.filter(path -> path.toString().contains("/src/main/resources/mapper/"))
                    .filter(path -> path.toString().endsWith(".xml"))
                    .sorted()
                    .toList()) {
                List<String> lines = Files.readAllLines(xmlFile);
                for (int index = 0; index < lines.size(); index++) {
                    String trimmed = lines.get(index).strip();
                    if (trimmed.startsWith("<resultMap ")) {
                        int closingIndex = findClosingTag(lines, index, "</resultMap>");
                        if (!lines.get(index).startsWith("    <resultMap")
                                || index == 0
                                || !lines.get(index - 1).strip().equals("<!-- @formatter:off -->")) {
                            formattingViolations.add(xmlFile + ":" + (index + 1) + " -> resultMap opening indentation");
                        }
                        if (closingIndex < 0 || !lines.get(closingIndex).equals("    </resultMap>")) {
                            formattingViolations.add(xmlFile + ":" + (index + 1) + " -> resultMap closing indentation");
                            continue;
                        }
                        if (closingIndex + 1 >= lines.size()
                                || !lines.get(closingIndex + 1).strip().equals("<!-- @formatter:on -->")) {
                            formattingViolations.add(xmlFile + ":" + (closingIndex + 1) + " -> formatter marker after resultMap");
                        }

                        List<Integer> propertyColumns = new ArrayList<>();
                        for (int mappingIndex = index + 1; mappingIndex < closingIndex; mappingIndex++) {
                            String mappingLine = lines.get(mappingIndex);
                            String mapping = mappingLine.stripLeading();
                            if (mapping.startsWith("<id ") || mapping.startsWith("<result ")) {
                                if (!mappingLine.startsWith("        ")) {
                                    formattingViolations.add(xmlFile + ":" + (mappingIndex + 1)
                                            + " -> result mapping indentation");
                                }
                                Matcher propertyMatcher = RESULT_PROPERTY_PATTERN.matcher(mapping);
                                if (propertyMatcher.find()) {
                                    propertyColumns.add(mappingLine.indexOf("property="));
                                }
                            }
                        }
                        if (!propertyColumns.isEmpty()
                                && propertyColumns.stream().distinct().count() > 1) {
                            formattingViolations.add(xmlFile + ":" + (index + 1)
                                    + " -> result property columns are not aligned");
                        }
                    }
                    if (trimmed.equals("<sql id=\"Base_Column_List\">")) {
                        int closingIndex = findClosingTag(lines, index, "</sql>");
                        if (!lines.get(index).equals("    <sql id=\"Base_Column_List\">")
                                || closingIndex < 0
                                || !lines.get(closingIndex).equals("    </sql>")) {
                            formattingViolations.add(xmlFile + ":" + (index + 1) + " -> Base_Column_List indentation");
                            continue;
                        }
                        for (int columnIndex = index + 1; columnIndex < closingIndex; columnIndex++) {
                            if (!lines.get(columnIndex).isBlank() && !lines.get(columnIndex).startsWith("        ")) {
                                formattingViolations.add(xmlFile + ":" + (columnIndex + 1)
                                        + " -> Base_Column_List child indentation");
                            }
                        }
                    }
                }
            }
        }
        assertThat(formattingViolations)
                .as("Mapper result maps and Base_Column_List must follow UserMapper.xml formatting")
                .isEmpty();
    }

    @Test
    void baseColumnListsMustMatchEntityColumnsInBaseResultMaps() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        List<String> mismatchedColumns = new ArrayList<>();
        try (var paths = Files.walk(backend)) {
            for (Path xmlFile : paths.filter(path -> path.toString().contains("/src/main/resources/mapper/"))
                    .filter(path -> path.toString().endsWith(".xml"))
                    .sorted()
                    .toList()) {
                List<String> lines = Files.readAllLines(xmlFile);
                int resultMapStart = -1;
                int columnListStart = -1;
                for (int index = 0; index < lines.size(); index++) {
                    if (lines.get(index).contains("<resultMap id=\"BaseResultMap\"")) {
                        resultMapStart = index;
                    }
                    if (lines.get(index).strip().equals("<sql id=\"Base_Column_List\">")) {
                        columnListStart = index;
                    }
                }
                if (resultMapStart < 0 || columnListStart < 0) {
                    continue;
                }

                int resultMapEnd = findClosingTag(lines, resultMapStart, "</resultMap>");
                int columnListEnd = findClosingTag(lines, columnListStart, "</sql>");
                if (resultMapEnd < 0 || columnListEnd < 0) {
                    mismatchedColumns.add(xmlFile + " -> unterminated BaseResultMap or Base_Column_List");
                    continue;
                }
                String resultMap = String.join("\n", lines.subList(resultMapStart, resultMapEnd + 1));
                Matcher resultColumnMatcher = Pattern.compile("<(?:id|result)\\s+column=\"([^\"]+)\"")
                        .matcher(resultMap);
                List<String> mappedColumns = new ArrayList<>();
                while (resultColumnMatcher.find()) {
                    mappedColumns.add(resultColumnMatcher.group(1));
                }

                String columnList = String.join("\n", lines.subList(columnListStart + 1, columnListEnd))
                        .replaceAll("(?s)<!--.*?-->", "")
                        .replaceAll("<include\\b[^>]*/>", "");
                List<String> listedColumns = Arrays.stream(columnList.split(","))
                        .map(String::trim)
                        .map(column -> column.replace("\"", ""))
                        .filter(column -> !column.isBlank())
                        .toList();
                if (!mappedColumns.stream().sorted().toList().equals(listedColumns.stream().sorted().toList())) {
                    mismatchedColumns.add(xmlFile + " -> BaseResultMap=" + mappedColumns
                            + ", Base_Column_List=" + listedColumns);
                }
            }
        }
        assertThat(mismatchedColumns)
                .as("Base_Column_List must stay in the same order and contain the same entity columns as BaseResultMap")
                .isEmpty();
    }

    private boolean isMyBatisMapper(String source) {
        String withoutComments = SourceContractTestSupport.withoutComments(source);
        return PUBLIC_INTERFACE_PATTERN.matcher(withoutComments).find()
                && ((source.contains("import org.apache.ibatis.annotations.Mapper;")
                        && MYBATIS_ANNOTATION_PATTERN.matcher(withoutComments).find())
                        || withoutComments.matches("(?s).*@org\\.apache\\.ibatis\\.annotations\\.Mapper\\b.*")
                        || withoutComments.matches("(?s).*extends\\s+[^\\{;]*\\bBaseMapper\\s*<.*"));
    }

    private String qualifiedName(Path javaFile, String source) {
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(source);
        Matcher interfaceMatcher = PUBLIC_INTERFACE_PATTERN.matcher(SourceContractTestSupport.withoutComments(source));
        if (!packageMatcher.find() || !interfaceMatcher.find()) {
            throw new IllegalStateException("Cannot identify mapper package/type: " + javaFile);
        }
        return packageMatcher.group(1) + "." + interfaceMatcher.group(1);
    }

    private String qualifiedTypeName(String typeName, Path javaFile, String source) {
        if (typeName.contains(".")) {
            return typeName;
        }
        Matcher importMatcher = Pattern.compile("(?m)^import\\s+([\\w.]+\\." + Pattern.quote(typeName) + ");")
                .matcher(source);
        if (importMatcher.find()) {
            return importMatcher.group(1);
        }
        Matcher packageMatcher = PACKAGE_PATTERN.matcher(source);
        if (!packageMatcher.find()) {
            throw new IllegalStateException("Cannot identify entity package: " + javaFile);
        }
        return packageMatcher.group(1) + "." + typeName;
    }

    private Path moduleRoot(Path javaFile) {
        Path current = javaFile.getParent();
        while (current != null && !current.toString().endsWith("/src/main/java")) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("Cannot identify Java module root: " + javaFile);
        }
        return current.getParent().getParent().getParent();
    }

    private Path domainDirectory(String qualifiedName) {
        String[] segments = qualifiedName.split("\\.");
        int mapperIndex = Arrays.asList(segments).subList(3, segments.length - 1).lastIndexOf("mapper") + 3;
        if (qualifiedName.equals("com.devops00.spectra.common.base.BaseMapper")) {
            return Path.of("common");
        }
        if (segments[3].equals("workflow")) {
            return Path.of("form");
        }
        if (!segments[3].equals("core") && !segments[3].equals("oa")) {
            throw new IllegalStateException("Cannot identify mapper domain: " + qualifiedName);
        }
        if (mapperIndex < 4) {
            throw new IllegalStateException("Cannot identify mapper package: " + qualifiedName);
        }
        return Path.of(String.join("/", Arrays.copyOfRange(segments, 4, mapperIndex)));
    }

    private Path mapperResourceRoot(Path xmlFile) {
        Path current = xmlFile.getParent();
        while (current != null && !current.toString().endsWith("/src/main/resources/mapper")) {
            current = current.getParent();
        }
        if (current == null) {
            throw new IllegalStateException("Cannot identify Mapper resource root: " + xmlFile);
        }
        return current;
    }

    private int findClosingTag(List<String> lines, int startIndex, String closingTag) {
        for (int index = startIndex + 1; index < lines.size(); index++) {
            if (lines.get(index).strip().equals(closingTag)) {
                return index;
            }
        }
        return -1;
    }

    private boolean hasNamespace(Path xmlFile, String namespace) {
        try {
            Matcher matcher = MAPPER_NAMESPACE_PATTERN.matcher(Files.readString(xmlFile));
            return matcher.find() && matcher.group(1).equals(namespace);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read mapper XML: " + xmlFile, exception);
        }
    }
}
