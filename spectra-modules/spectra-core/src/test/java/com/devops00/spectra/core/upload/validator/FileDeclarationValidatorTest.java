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

package com.devops00.spectra.core.upload.validator;

import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.javabean.entity.FileType;
import com.devops00.spectra.core.upload.javabean.from.CreateUploadRequest;
import org.junit.jupiter.api.Test;
import tools.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 验证 {@code FileDeclarationValidatorTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class FileDeclarationValidatorTest {

    private final FileDeclarationValidator validator = new FileDeclarationValidator();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void acceptsAnAllowedExtensionAndContentType() {
        var type = type("DOCUMENT", "[\"pdf\"]", "[\"application/pdf\"]", 1000L);
        var request = request("contract.PDF", "application/pdf", 800L);

        assertDoesNotThrow(() -> validator.validate(request, type));
    }

    @Test
    void rejectsAnExtensionOutsideThePolicy() {
        var type = type("DOCUMENT", "[\"pdf\"]", "[\"application/pdf\"]", 1000L);
        var request = request("contract.exe", "application/octet-stream", 800L);

        var exception = assertThrows(FileUploadException.class, () -> validator.validate(request, type));

        assertEquals(FileErrorCode.FILE_PART_INVALID, exception.getErrorCode());
    }

    @Test
    void rejectsFilesAboveThePolicyLimit() {
        var type = type("DOCUMENT", "[\"pdf\"]", "[\"application/pdf\"]", 1000L);
        var request = request("contract.pdf", "application/pdf", 1001L);

        var exception = assertThrows(FileUploadException.class, () -> validator.validate(request, type));

        assertEquals(FileErrorCode.FILE_PART_INVALID, exception.getErrorCode());
    }

    /**
     * 处理请求相关数据。
     */
    private CreateUploadRequest request(String name, String contentType, long size) {
        var request = new CreateUploadRequest();
        request.setOriginalName(name);
        request.setContentType(contentType);
        request.setSize(size);
        request.setContentSha256("0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef");
        request.setFileTypeCode("DOCUMENT");
        return request;
    }

    /**
     * 处理类型相关数据。
     */
    private FileType type(String code, String extensions, String contentTypes, long maxSize) {
        var type = new FileType();
        type.setCode(code);
        type.setAllowedExtensions(objectMapper.readTree(extensions));
        type.setAllowedContentTypes(objectMapper.readTree(contentTypes));
        type.setMaxSize(maxSize);
        type.setUploadEnabled(true);
        type.setEnabled(true);
        type.setDangerous(false);
        return type;
    }
}
