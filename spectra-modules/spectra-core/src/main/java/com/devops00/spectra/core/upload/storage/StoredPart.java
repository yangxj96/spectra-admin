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

package com.devops00.spectra.core.upload.storage;

/**
 * 承载分片相关的不可变数据。
 *
 * @param partNumber 分片编号
 * @param size       已存储分片的大小（字节）
 * @param sha256     文件内容的 SHA-256 摘要
 * @param etag       ETag
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record StoredPart(int partNumber, long size, String sha256, String etag) {
}
