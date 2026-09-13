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

package com.devops00.spectra.core.mapper;

import com.devops00.spectra.framework.persistence.mybatis.handler.UUIDTypeHandler;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.type.InstantTypeHandler;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 验证 {@code MapperXmlStatementContractTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class MapperXmlStatementContractTest {

    private static final Map<String, List<String>> MIGRATED_STATEMENTS = Map.ofEntries(
            Map.entry("com.devops00.spectra.core.system.mapper.DepartmentMapper",
                    List.of("generatePath")),
            Map.entry("com.devops00.spectra.core.system.mapper.DepartmentClosureMapper",
                    List.of("clearClosure", "rebuildClosure")),
            Map.entry("com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper",
                    List.of("insertPrimary")),
            Map.entry("com.devops00.spectra.core.notification.mapper.NotificationSendPreviewMapper",
                    List.of("deleteExpired")),
            Map.entry("com.devops00.spectra.core.upload.mapper.FileAssetMapper",
                    List.of("findReady", "findCleanupCandidates", "claimCleanupCandidate", "findOrphanCandidates",
                            "deleteByIdPhysically", "markDeleting", "markDeleted", "markCleanupRetry", "markOrphaned")),
            Map.entry("com.devops00.spectra.core.upload.mapper.FileReferenceMapper",
                    List.of("findByKey", "findByBusinessKey", "countByAssetId", "softDeleteByKey",
                            "softDeleteByBusinessKeyAndPurpose", "softDeleteById", "softDeleteByBusinessKey")),
            Map.entry("com.devops00.spectra.core.upload.mapper.FileTypeMapper",
                    List.of("findEnabledByCode", "findEnabledByContentType", "findByIdIncludingDisabled")),
            Map.entry("com.devops00.spectra.core.upload.mapper.FileUploadPartMapper",
                    List.of("findBySessionId", "selectForUpdate", "countConfirmed", "prepareTarget", "markUploaded",
                            "markConfirmed", "markExternalConfirmed")),
            Map.entry("com.devops00.spectra.core.upload.mapper.FileUploadSessionMapper",
                    List.of("countActiveByOwner", "findResumable", "selectForUpdate", "findExpiredCandidates",
                            "findCleanupCandidates", "claimCleanupCandidate", "markExpired", "claimForVerification",
                            "markReady", "markFailed", "markCanceled", "updateVerificationProgress", "touchActivity",
                            "markCleanupRetry", "markCleaned", "deleteByIdPhysically", "deletePartsPhysically")));

    @Test
    void everyMigratedMapperXmlMustParseAndDeclareEachCustomStatement() throws Exception {
        Configuration configuration = new Configuration();
        configuration.getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        configuration.getTypeHandlerRegistry().register(Instant.class, InstantTypeHandler.class);

        parse(configuration, "mapper/common/BaseMapper.xml");
        parse(configuration, "mapper/system/DepartmentMapper.xml");
        parse(configuration, "mapper/system/DepartmentClosureMapper.xml");
        parse(configuration, "mapper/user/UserDepartmentMembershipMapper.xml");
        parse(configuration, "mapper/notification/NotificationSendPreviewMapper.xml");
        parse(configuration, "mapper/upload/FileAssetMapper.xml");
        parse(configuration, "mapper/upload/FileReferenceMapper.xml");
        parse(configuration, "mapper/upload/FileTypeMapper.xml");
        parse(configuration, "mapper/upload/FileUploadPartMapper.xml");
        parse(configuration, "mapper/upload/FileUploadSessionMapper.xml");

        MIGRATED_STATEMENTS.forEach((namespace, statements) -> statements.forEach(statement -> assertThat(
                configuration.hasStatement(namespace + "." + statement))
                .as("XML statement %s.%s", namespace, statement)
                .isTrue()));
    }

    /**
     * 解析XML合同。
     */
    private void parse(Configuration configuration, String resource) throws Exception {
        try (InputStream mapperXml = getClass().getResourceAsStream("/" + resource)) {
            assertThat(mapperXml).as("Mapper XML resource %s", resource).isNotNull();
            new XMLMapperBuilder(mapperXml, configuration, resource, configuration.getSqlFragments()).parse();
        }
    }
}
