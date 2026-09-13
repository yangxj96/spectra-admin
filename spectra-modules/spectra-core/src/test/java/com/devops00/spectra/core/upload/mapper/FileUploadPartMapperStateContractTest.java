/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/** Verifies that external providers can confirm a part directly from PENDING. */
class FileUploadPartMapperStateContractTest {

    @Test
    void externalConfirmationMustTransitionPendingPartToConfirmed() throws Exception {
        Configuration configuration = new Configuration();
        try (InputStream baseMapperXml = getClass().getResourceAsStream("/mapper/common/BaseMapper.xml")) {
            assertThat(baseMapperXml).as("BaseMapper XML resource").isNotNull();
            new XMLMapperBuilder(baseMapperXml, configuration, "mapper/common/BaseMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        try (InputStream mapperXml = getClass().getResourceAsStream("/mapper/upload/FileUploadPartMapper.xml")) {
            assertThat(mapperXml).as("FileUploadPartMapper XML resource").isNotNull();
            new XMLMapperBuilder(mapperXml, configuration, "mapper/upload/FileUploadPartMapper.xml",
                    configuration.getSqlFragments()).parse();
        }
        var statement = configuration.getMappedStatement(
                FileUploadPartMapper.class.getName() + ".markExternalConfirmed");
        String sql = statement.getBoundSql(Map.of(
                "sessionId", java.util.UUID.randomUUID(), "partNumber", 1, "size", 1L,
                "sha256", "hash", "etag", "etag")).getSql();

        assertThat(sql)
                .contains("status = 'CONFIRMED'")
                .contains("AND status = 'PENDING'");
    }
}
