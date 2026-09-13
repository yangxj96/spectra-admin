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

package com.devops00.spectra.common.port.file;

import java.io.IOException;
import java.io.InputStream;

/**
 * 定义文件相关的跨模块调用契约。
 *
 * @param stream      待下载文件内容的数据流
 * @param displayName 下载文件的展示名称
 * @param contentType 内容类型
 * @param size        下载文件的大小（字节）
 * @param rangeStart  下载范围的起始字节偏移
 * @param rangeEnd    下载范围的结束字节偏移
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record FileDownload(InputStream stream,
                           String displayName,
                           String contentType,
                           long size,
                           Long rangeStart,
                           Long rangeEnd)
        implements
            AutoCloseable {

    @Override
    public void close() throws IOException {
        stream.close();
    }
}
