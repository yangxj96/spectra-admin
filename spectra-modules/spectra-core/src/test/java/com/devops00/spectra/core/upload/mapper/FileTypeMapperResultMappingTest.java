/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.mapper;

import com.devops00.spectra.framework.persistence.mybatis.PgJsonbNodeTypeHandler;
import com.devops00.spectra.framework.persistence.mybatis.handler.UUIDTypeHandler;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/** Ensures XML-backed file type lookups preserve the JSONB policy columns. */
class FileTypeMapperResultMappingTest {

    private static final List<String> JSONB_COLUMNS = List.of("allowed_extensions", "allowed_content_types", "magic_rules");

    @Test
    void everyFileTypeLookupMustMapJsonbPolicyColumns() throws Exception {
        Configuration configuration = parseMapperXml();
        for (String methodName : List.of("findEnabledByCode", "findEnabledByContentType", "findByIdIncludingDisabled")) {
            var resultMappings = configuration.getMappedStatement(FileTypeMapper.class.getName() + "." + methodName)
                    .getResultMaps()
                    .getFirst()
                    .getResultMappings();

            assertThat(resultMappings.stream().map(mapping -> mapping.getColumn()).toList())
                    .as("JSONB columns for %s", methodName)
                    .containsAll(JSONB_COLUMNS);
            assertThat(resultMappings.stream()
                    .filter(mapping -> JSONB_COLUMNS.contains(mapping.getColumn()))
                    .allMatch(mapping -> mapping.getTypeHandler() instanceof PgJsonbNodeTypeHandler))
                    .as("JSONB type handlers for %s", methodName)
                    .isTrue();
        }
    }

    private Configuration parseMapperXml() throws Exception {
        Configuration configuration = new Configuration();
        configuration.getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        try (InputStream baseMapperXml = getClass().getResourceAsStream("/mapper/common/BaseMapper.xml")) {
            assertThat(baseMapperXml).as("BaseMapper XML resource").isNotNull();
            new XMLMapperBuilder(baseMapperXml, configuration, "mapper/common/BaseMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        try (InputStream mapperXml = getClass().getResourceAsStream("/mapper/upload/FileTypeMapper.xml")) {
            assertThat(mapperXml).as("FileTypeMapper XML resource").isNotNull();
            new XMLMapperBuilder(mapperXml, configuration, "mapper/upload/FileTypeMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        return configuration;
    }
}
