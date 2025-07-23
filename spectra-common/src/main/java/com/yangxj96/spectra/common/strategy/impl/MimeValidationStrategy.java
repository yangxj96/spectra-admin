/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.common.strategy.impl;

import com.yangxj96.spectra.common.strategy.FileTypeValidationStrategy;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * <p>
 * 文件类型验证策略-根据文件mime方式验证
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/19
 */
public class MimeValidationStrategy implements FileTypeValidationStrategy {

    private final List<String> allowedMimes;

    public MimeValidationStrategy(List<String> allowedMimes) {
        this.allowedMimes = allowedMimes;
    }

    @Override
    public boolean isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String mimeType = file.getContentType();
        return allowedMimes.stream().anyMatch(mime -> mime.equalsIgnoreCase(mimeType));
    }

}
