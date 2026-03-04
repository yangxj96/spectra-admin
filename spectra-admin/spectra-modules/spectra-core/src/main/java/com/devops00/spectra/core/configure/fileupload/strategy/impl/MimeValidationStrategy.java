/*
 *  Copyright 2018-2025 yangxj96
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

package com.devops00.spectra.core.configure.fileupload.strategy.impl;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.core.configure.fileupload.strategy.FileTypeValidationStrategy;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/// 文件类型验证策略-根据文件mime方式验证
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/19
@Slf4j
public record MimeValidationStrategy(List<String> allowedMimes) implements FileTypeValidationStrategy {

    @Override
    public boolean isValid(@Nullable MultipartFile file) {
        log.debug(LogPrefix.STORAGE.f("文件mime验证"));
        if (file == null || file.isEmpty()) {
            return false;
        }
        var mimeType = file.getContentType();
        return allowedMimes.stream().anyMatch(mime -> mime.equalsIgnoreCase(mimeType));
    }

}
