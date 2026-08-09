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

package com.devops00.spectra.upload.strategy;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.upload.service.FileTypeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Set;

/**
 * 文件类型校验策略管理器
 *
 * @author yangxj96
 * @version 2.0
 * @since 2025/6/19 00:00
 */
@Slf4j
@RequiredArgsConstructor
public class FileTypeValidator {

    private final List<FileTypeValidationStrategy> strategies;

    private final FileTypeService fileTypeService;

    private final boolean whitelistEnabled;

    private final boolean blacklistEnabled;

    /**
     * 执行所有注册的验证策略（含扩展名 + 内容校验）
     *
     * @param file
     *            待验证的文件
     * @return 如果所有策略均通过，则返回 true；否则返回 false
     */
    public boolean validate(MultipartFile file) {
        if (!validateFilename(file.getOriginalFilename())) {
            return false;
        }
        for (FileTypeValidationStrategy strategy : strategies) {
            try {
                if (!strategy.isValid(file)) {
                    return false;
                }
            } catch (IOException e) {
                log.error("{}验证策略失败:{}", LogPrefix.STORAGE.p(), e.getMessage(), e);
                return false;
            }
        }
        return true;
    }

    /**
     * 仅根据文件名做轻量校验（适用于 pre 阶段，无文件内容）
     *
     * @param filename
     *            原始文件名
     * @return 文件名校验是否通过
     */
    public boolean validateFilename(@Nullable String filename) {
        if (filename == null || filename.isBlank()) {
            return false;
        }
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex == -1 || dotIndex == filename.length() - 1) {
            return false;
        }
        String ext = filename.substring(dotIndex).toLowerCase();

        if (blacklistEnabled) {
            Set<String> dangerous = fileTypeService.findDangerousExtensions();
            if (dangerous.contains(ext)) {
                return false;
            }
        }

        if (whitelistEnabled) {
            Set<String> allowed = fileTypeService.findAllowedExtensions();
            if (!allowed.contains(ext)) {
                return false;
            }
        }

        return true;
    }
}
