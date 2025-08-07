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
import org.jetbrains.annotations.NotNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.List;

/**
 * 文件类型验证策略-根据文件魔数验证
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-06-19
 */
public record MagicNumberValidationStrategy(List<FileType> allowedTypes) implements FileTypeValidationStrategy {

    @Override
    public boolean isValid(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            return false;
        }

        byte[] fileHeader = readHeader(file);

        for (FileType type : allowedTypes) {
            byte[] magic = type.getMagicNumber();
            if (matches(fileHeader, magic)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 判断两个字节数组前 n 字节是否相等
     *
     * @param fileHeader 文件头字节
     * @param magic      文件类型的魔数字节
     * @return 是否相等
     */
    public static boolean matches(byte[] fileHeader, byte[] magic) {
        if (fileHeader == null || magic == null || fileHeader.length < magic.length) {
            return false;
        }
        for (int i = 0; i < magic.length; i++) {
            if (fileHeader[i] != magic[i]) {
                return false;
            }
        }
        return true;
    }


    /**
     * 从 MultipartFile 读取指定长度的文件头
     *
     * @param file 需要读取的文件
     * @return 读取到的头部长度
     */
    public static byte @NotNull [] readHeader(MultipartFile file) throws IOException {
        var length = Arrays.stream(FileType.values())
                .mapToInt(t -> t.getMagicNumber().length)
                .max()
                .orElse(0);

        if (length <= 0) {
            throw new RuntimeException("不允许的文件类型");
        }

        try (InputStream is = new ByteArrayInputStream(file.getBytes())) {
            byte[] header = new byte[length];
            int bytesRead = is.read(header);
            if (bytesRead < 1) {
                throw new IOException("空文件");
            }
            return header;
        }
    }

}
