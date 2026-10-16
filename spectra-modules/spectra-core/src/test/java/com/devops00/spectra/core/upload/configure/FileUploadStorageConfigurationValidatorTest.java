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
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * 验证默认存储为 S3 时的必需配置校验，LOCAL 时允许空 S3 配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class FileUploadStorageConfigurationValidatorTest {

    @Test
    void localDefaultAllowsEmptyS3() {
        assertDoesNotThrow(
                () -> new FileUploadStorageConfigurationValidator(properties(StorageProviderType.LOCAL),
                        new S3Properties()));
    }

    @Test
    void s3DefaultRequiresEndpointAndCredentials() {
        assertThrows(IllegalStateException.class,
                () -> new FileUploadStorageConfigurationValidator(properties(StorageProviderType.S3),
                        new S3Properties()));
    }

    @Test
    void s3DefaultWithCompleteConfigurationPasses() {
        S3Properties s3 = new S3Properties();
        s3.setEndpoint("https://s3.example.test");
        s3.setAccessKey("access");
        s3.setSecretKey("secret");
        s3.setRegion("us-east-1");
        s3.setBucket("bucket");
        assertDoesNotThrow(
                () -> new FileUploadStorageConfigurationValidator(properties(StorageProviderType.S3), s3));
    }

    @Test
    void s3DefaultWithNonPositivePreviewMinutesIsRejected() {
        S3Properties s3 = new S3Properties();
        s3.setEndpoint("https://s3.example.test");
        s3.setAccessKey("access");
        s3.setSecretKey("secret");
        s3.setRegion("us-east-1");
        s3.setBucket("bucket");
        s3.setPreviewMinutes(0);
        assertThrows(IllegalStateException.class,
                () -> new FileUploadStorageConfigurationValidator(properties(StorageProviderType.S3), s3));
    }

    private static FileUploadProperties properties(StorageProviderType provider) {
        FileUploadProperties properties = new FileUploadProperties();
        properties.setDefaultStorage(provider);
        return properties;
    }
}
