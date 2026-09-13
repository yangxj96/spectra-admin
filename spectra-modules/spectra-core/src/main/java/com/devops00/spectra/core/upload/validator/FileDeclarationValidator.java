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
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;

import java.util.Locale;

/**
 * 校验文件声明相关的数据和业务约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
public class FileDeclarationValidator {

    public void validate(CreateUploadRequest request, FileType type) {
        if (type == null || !Boolean.TRUE.equals(type.getEnabled()) || !Boolean.TRUE.equals(type.getUploadEnabled())) {
            throw invalid("file type is not enabled");
        }
        if (Boolean.TRUE.equals(type.getDangerous())) {
            throw invalid("dangerous file type is not allowed");
        }
        if (type.getMaxSize() != null && request.getSize() > type.getMaxSize()) {
            throw invalid("file exceeds the configured type limit");
        }
        if (request.getSize() < 0) {
            throw invalid("file size cannot be negative");
        }
        String extension = extension(request.getOriginalName());
        if (!containsText(type.getAllowedExtensions(), extension)) {
            throw invalid("file extension is not allowed");
        }
        if (!containsText(type.getAllowedContentTypes(), request.getContentType().toLowerCase(Locale.ROOT))) {
            throw invalid("content type is not allowed");
        }
    }

    /**
     * 判断文件声明。
     */
    private boolean containsText(JsonNode node, String expected) {
        if (node == null || !node.isArray()) {
            return false;
        }
        for (JsonNode value : node) {
            if (expected.equalsIgnoreCase(value.asText().trim().replaceFirst("^\\.", ""))) {
                return true;
            }
        }
        return false;
    }

    /**
     * 处理文件声明相关数据。
     */
    private String extension(String name) {
        if (!StringUtils.hasText(name)) {
            return "";
        }
        int dot = name.lastIndexOf('.');
        return dot < 0 ? "" : name.substring(dot + 1).toLowerCase(Locale.ROOT);
    }

    /**
     * 处理文件声明相关数据。
     */
    private FileUploadException invalid(String message) {
        return new FileUploadException(FileErrorCode.FILE_PART_INVALID, message);
    }
}
