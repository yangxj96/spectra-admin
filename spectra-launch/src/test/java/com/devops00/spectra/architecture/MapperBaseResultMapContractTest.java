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

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.mybatis.handler.UUIDTypeHandler;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.mapping.ResultMap;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 {@code MapperBaseResultMapContractTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class MapperBaseResultMapContractTest {

    @Test
    void everyBaseResultMapMustMatchItsEntityFields() throws IOException {
        Path backend = SourceContractTestSupport.resolveBackendPath();
        List<Path> mapperXmlFiles;
        try (Stream<Path> paths = Files.walk(backend)) {
            mapperXmlFiles = paths.filter(path -> path.toString().contains("/src/main/resources/mapper/"))
                    .filter(path -> path.toString().endsWith(".xml"))
                    .sorted()
                    .toList();
        }
        assertThat(mapperXmlFiles).as("backend Mapper XML inventory").isNotEmpty();

        Configuration configuration = new Configuration();
        configuration.getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        for (Path mapperXml : mapperXmlFiles) {
            try (InputStream input = Files.newInputStream(mapperXml)) {
                new XMLMapperBuilder(input, configuration, mapperXml.toString(), configuration.getSqlFragments())
                        .parse();
            }
        }

        List<String> fieldDrift = new ArrayList<>();
        List<String> missingTableEntities = new ArrayList<>();
        int baseResultMapCount = 0;
        for (Object mappedResultMap : configuration.getResultMaps()) {
            if (!(mappedResultMap instanceof ResultMap resultMap)) {
                continue;
            }
            if (!resultMap.getId().endsWith(".BaseResultMap")) {
                continue;
            }
            baseResultMapCount++;
            if (resultMap.getType().getAnnotation(TableName.class) == null) {
                missingTableEntities.add(resultMap.getId() + " -> " + resultMap.getType().getName());
                continue;
            }

            Set<String> mappedProperties = new HashSet<>();
            resultMap.getResultMappings()
                    .stream()
                    .map(mapping -> mapping.getProperty())
                    .filter(property -> property != null && !property.isBlank())
                    .forEach(mappedProperties::add);

            Set<String> entityProperties = new HashSet<>();
            Set<String> persistentProperties = new HashSet<>();
            for (Class<?> type = resultMap.getType(); type != null && type != Object.class; type = type.getSuperclass()) {
                for (Field field : type.getDeclaredFields()) {
                    if (Modifier.isStatic(field.getModifiers()) || field.isSynthetic()) {
                        continue;
                    }
                    entityProperties.add(field.getName());
                    TableField tableField = field.getAnnotation(TableField.class);
                    if (tableField == null || tableField.exist()) {
                        persistentProperties.add(field.getName());
                    }
                }
            }

            Set<String> missing = new HashSet<>(persistentProperties);
            missing.removeAll(mappedProperties);
            Set<String> unknown = new HashSet<>(mappedProperties);
            unknown.removeAll(entityProperties);
            if (!missing.isEmpty() || !unknown.isEmpty()) {
                fieldDrift.add(resultMap.getId() + " missing=" + missing + " unknown=" + unknown);
            }
        }

        assertThat(fieldDrift)
                .as("BaseResultMap properties must match the persistent fields on their entity")
                .isEmpty();
        assertThat(missingTableEntities)
                .as("every BaseResultMap type must be an @TableName entity")
                .isEmpty();
        assertThat(baseResultMapCount).as("BaseResultMap inventory").isPositive();
    }
}
