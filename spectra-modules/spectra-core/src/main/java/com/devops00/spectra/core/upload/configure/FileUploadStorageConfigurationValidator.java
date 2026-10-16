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

package com.devops00.spectra.core.upload.configure;

import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.properties.S3Properties;

/**
 * 文件上传存储配置的启动校验。
 *
 * <p>当默认存储实现为 S3 时，endpoint/access-key/secret-key/region/bucket 与预览有效期必须完整有效；
 * 否则在启动装配阶段失败，不把缺失配置推迟到运行期签名或 provider 构造。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
final class FileUploadStorageConfigurationValidator {

    FileUploadStorageConfigurationValidator(FileUploadProperties properties, S3Properties s3Properties) {
        if (properties.getDefaultStorage() != StorageProviderType.S3) {
            return;
        }
        requireText(s3Properties.getEndpoint(), "spectra.file.upload.s3.endpoint");
        requireText(s3Properties.getAccessKey(), "spectra.file.upload.s3.access-key");
        requireText(s3Properties.getSecretKey(), "spectra.file.upload.s3.secret-key");
        requireText(s3Properties.getRegion(), "spectra.file.upload.s3.region");
        requireText(s3Properties.getBucket(), "spectra.file.upload.s3.bucket");
        if (s3Properties.getPreviewMinutes() == null || s3Properties.getPreviewMinutes() <= 0) {
            throw new IllegalStateException(
                    "文件上传存储配置无效：spectra.file.upload.s3.preview-minutes 必须为正数");
        }
    }

    private static void requireText(String value, String key) {
        if (value == null || value.isBlank()) {
            throw new IllegalStateException("文件上传存储配置无效：" + key + " 在默认存储为 S3 时不能为空");
        }
    }
}
