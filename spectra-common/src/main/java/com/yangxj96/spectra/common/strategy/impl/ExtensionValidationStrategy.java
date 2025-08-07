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

import com.yangxj96.spectra.common.enums.FileType;
import com.yangxj96.spectra.common.strategy.FileTypeValidationStrategy;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件类型验证策略-根据文件扩展名验证
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-06-19
 */
public record ExtensionValidationStrategy(List<FileType> allowed) implements FileTypeValidationStrategy {

    private static final Integer MIN = -1;

    @Override
    public boolean isValid(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            return false;
        }
        String filename = file.getOriginalFilename();
        if (StringUtils.isNotBlank(filename)) {
            String fileExtension = getFileExtension(filename);
            return allowed.stream().anyMatch(ext -> ext.getExtension().equalsIgnoreCase(fileExtension));
        }
        return false;
    }

    /**
     * 获取文件扩展名
     *
     * @param filename 文件名称
     * @return 扩展名
     */
    private String getFileExtension(String filename) {
        int lastIndexOfDot = filename.lastIndexOf('.');
        if (lastIndexOfDot == MIN) {
            return "";
        }
        return filename.substring(lastIndexOfDot).toLowerCase();
    }
}
