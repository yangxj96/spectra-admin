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

package com.devops00.spectra.upload.properties;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.devops00.spectra.upload.javabean.constant.StorageProviderType;
import java.time.Duration;

/**
 * 文件上传参数
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/19 00:00
 */
@Data
@ConfigurationProperties(prefix = "spectra.file.upload")
public class FileUploadProperties {

    /**
     * 文件上传默认实现
     */
    private StorageProviderType defaultStorage = StorageProviderType.LOCAL;

    /**
     * 文件类型验证策略（扩展名校验已内置于 FileTypeValidator，无需配置）
     */
    private Long maxFileSize = 1024L * 1024 * 1024;

    /**
     * 分片大小,默认5M 5242880L
     */
    private Long chunkSize = 8L * 1024 * 1024;

    private Long minChunkSize = 5L * 1024 * 1024;

    private Long maxChunkSize = 64L * 1024 * 1024;

    private Integer maxParts = 10_000;

    private Integer parallelism = 3;

    private Integer maxConcurrentTasksPerUser = 3;

    private Duration taskTtl = Duration.ofHours(24);

    private Duration idleTimeout = Duration.ofHours(2);

    private Duration recordRetention = Duration.ofDays(7);

    private Duration orphanRetention = Duration.ofDays(7);

    private Duration presignTtl = Duration.ofMinutes(15);

}
