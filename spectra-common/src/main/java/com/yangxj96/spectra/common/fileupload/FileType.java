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

}
