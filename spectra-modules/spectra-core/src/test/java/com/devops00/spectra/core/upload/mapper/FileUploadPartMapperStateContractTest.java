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

package com.devops00.spectra.core.upload.mapper;

import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 {@code FileUploadPartMapperStateContractTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
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
