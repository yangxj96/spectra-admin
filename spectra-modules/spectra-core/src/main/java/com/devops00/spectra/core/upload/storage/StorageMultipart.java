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
 * 承载存储分片上传相关的不可变数据。
 *
 * @param container        文件存储所在的容器或存储桶
 * @param key              分片上传对象在存储服务中的对象键
 * @param providerUploadId 提供器上传标识
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public record StorageMultipart(String container, String key, String providerUploadId) {
}
