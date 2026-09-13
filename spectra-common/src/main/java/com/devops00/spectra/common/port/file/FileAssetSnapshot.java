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

import java.util.UUID;

/**
 * 定义文件资产快照相关的跨模块调用契约。
 *
 * @param fileAssetId   文件资产标识
 * @param originalName  文件在上传时使用的原始名称
 * @param size          文件内容的大小（字节）
 * @param contentType   内容类型
 * @param contentSha256 文件内容的 SHA-256 摘要
 * @param status        业务状态
 * @param fileTypeCode  文件类型编码
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record FileAssetSnapshot(UUID fileAssetId,
                                String originalName,
                                long size,
                                String contentType,
                                String contentSha256,
                                String status,
                                String fileTypeCode) {
}
