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

package com.devops00.spectra.core.upload.service;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** 文件上传服务边界契约，防止实体主键和校验事务重新回到应用编排层。 */
class UploadBoundaryContractTest {

    @Test
    void databaseEntityIdsMustBeLeftToMetaObjectHandler() throws IOException {
        var uploadApplicationService = readSource("service/UploadApplicationService.java");
        var referenceService = readSource("service/FileReferenceApplicationService.java");

        assertFalse(uploadApplicationService.contains("session.setId("));
        assertFalse(uploadApplicationService.contains("part.setId("));
        assertFalse(referenceService.contains("reference.setId("));
        assertFalse(referenceService.contains("UUID.randomUUID()"));
    }

    @Test
    void verificationMustBeDelegatedToIndependentTransactionalWorker() throws IOException {
        var uploadApplicationService = readSource("service/UploadApplicationService.java");
        var worker = readSource("service/UploadVerificationWorker.java");

        assertFalse(uploadApplicationService.contains("ObjectProvider<UploadApplicationService>"));
        assertFalse(uploadApplicationService.contains("serviceProvider.getObject().verify"));
        assertTrue(uploadApplicationService.contains("verificationWorker"));
        assertTrue(worker.contains("@Transactional"));
        assertTrue(worker.contains("public void verify(UUID uploadId)"));
    }

    private String readSource(String relativePath) throws IOException {
        var path = Path.of("src", "main", "java",
                "com", "devops00", "spectra", "core", "upload", relativePath);
        assertTrue(Files.isRegularFile(path), "缺少上传边界实现文件: " + path);
        return Files.readString(path, StandardCharsets.UTF_8);
    }
}
