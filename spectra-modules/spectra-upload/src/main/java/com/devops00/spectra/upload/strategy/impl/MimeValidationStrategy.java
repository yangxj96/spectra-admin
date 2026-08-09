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

package com.devops00.spectra.upload.strategy.impl;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.upload.service.FileTypeService;
import com.devops00.spectra.upload.strategy.FileTypeValidationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件类型验证策略-根据文件 MIME 类型验证
 *
 * @author yangxj96
 * @version 2.0
 * @since 2025/6/19 00:00
 */
@Slf4j
@RequiredArgsConstructor
public class MimeValidationStrategy implements FileTypeValidationStrategy {

    private final FileTypeService fileTypeService;

    private final boolean whitelistEnabled;

    private final boolean blacklistEnabled;

    @Override
    public boolean isValid(@Nullable MultipartFile file) {
        log.debug(LogPrefix.STORAGE.f("文件MIME验证"));
        if (file == null || file.isEmpty()) {
            return false;
        }
        var mimeType = file.getContentType();
        if (mimeType == null || mimeType.isBlank()) {
            return false;
        }
        var mime = mimeType.toLowerCase();

        if (blacklistEnabled && fileTypeService.findDangerousMimes().contains(mime)) {
            return false;
        }
        if (whitelistEnabled && !fileTypeService.findAllowedMimes().contains(mime)) {
            return false;
        }
        return true;
    }
}
