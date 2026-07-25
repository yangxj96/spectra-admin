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
import com.devops00.spectra.upload.javabean.domain.MagicRule;
import com.devops00.spectra.upload.javabean.entity.FileType;
import com.devops00.spectra.upload.service.FileTypeService;
import com.devops00.spectra.upload.strategy.FileTypeValidationStrategy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/// 文件类型验证策略-根据文件魔数检测危险文件（仅黑名单）
///
/// @author yangxj96
/// @version 2.0
/// @since 2025/6/19 00:00
@Slf4j
@RequiredArgsConstructor
public class MagicNumberValidationStrategy implements FileTypeValidationStrategy {

    private static final int DEFAULT_HEADER_SIZE = 32;

    private final FileTypeService fileTypeService;

    @Override
    public boolean isValid(@Nullable MultipartFile file) throws IOException {
        log.debug(LogPrefix.STORAGE.f("文件魔数验证"));
        if (file == null || file.isEmpty()) {
            return false;
        }

        byte[] header = readHeader(file, DEFAULT_HEADER_SIZE);

        for (FileType type : fileTypeService.findDangerousWithMagicRules()) {
            if (matchType(header, type)) {
                log.debug(LogPrefix.STORAGE.f("文件头匹配危险类型: " + type.getName()));
                return false;
            }
        }

        return true;
    }

    private boolean matchType(byte[] header, FileType type) {
        var rules = type.getMagicRules();
        if (rules == null || rules.isEmpty()) {
            return false;
        }

        for (MagicRule rule : rules) {
            if (matchRule(header, rule)) {
                return true;
            }
        }

        return false;
    }

    private boolean matchRule(byte[] header, MagicRule rule) {
        byte[] magic = getCompiled(rule);
        int offset = rule.getOffset() == null ? 0 : rule.getOffset();

        if (header.length < offset + magic.length) {
            return false;
        }

        for (int i = 0; i < magic.length; i++) {
            if (header[offset + i] != magic[i]) {
                return false;
            }
        }

        return true;
    }

    private byte[] readHeader(MultipartFile file, int maxLen) throws IOException {
        try (var is = file.getInputStream()) {
            return is.readNBytes(maxLen);
        }
    }

    private byte[] getCompiled(MagicRule rule) {
        if (rule.getCompiled() == null) {
            rule.setCompiled(hexToBytes(rule.getBytes()));
        }
        return rule.getCompiled();
    }

    private byte[] hexToBytes(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];

        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) (
                    (Character.digit(hex.charAt(i), 16) << 4)
                            + Character.digit(hex.charAt(i + 1), 16)
            );
        }
        return data;
    }
}
