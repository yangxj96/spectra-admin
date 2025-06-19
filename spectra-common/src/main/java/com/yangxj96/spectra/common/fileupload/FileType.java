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

package com.yangxj96.spectra.common.fileupload;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * <p>
 * 文件类型
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/19
 */
@Getter
@AllArgsConstructor
public enum FileType {

    JPEG("JPEG", ".jpg", "image/jpeg", new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF}),
    PNG("PNG", ".png", "image/png", new byte[]{(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A}),
    GIF("GIF", ".gif", "image/gif", new byte[]{0x47, 0x49, 0x46, 0x38}),
    PDF("PDF", ".pdf", "application/pdf", new byte[]{0x25, 0x50, 0x44, 0x46}),
    ZIP("ZIP", ".zip", "application/zip", new byte[]{0x50, 0x4B, 0x03, 0x04});

    /**
     * 文件名称
     */
    private final String name;

    /**
     * 文件扩展后缀
     */
    private final String extension;

    /**
     * 文件mime
     */
    private final String mime;

    /**
     * 文件魔数
     */
    private final byte[] magicNumber;

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
    public static byte[] readHeader(MultipartFile file) throws IOException {
        var length = Arrays.stream(values())
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

    /**
     * 获取可上传的文件的mimes列表
     *
     * @param allowedTypes 允许上传的列表
     * @return mime列表
     */
    public static List<String> mimes(List<FileType> allowedTypes) {
        List<String> m = new ArrayList<>();
        for (FileType allowedType : allowedTypes) {
            m.add(allowedType.getMime());
        }
        return m;
    }

}
