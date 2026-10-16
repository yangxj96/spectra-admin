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

package com.devops00.spectra.core.upload.properties;

import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import java.time.Duration;

/**
 * 文件上传参数。
 *
 * <p>分片大小、分片数和各类 TTL 都会进入运行期计算或资源预算，因此在配置绑定阶段即校验：
 * 正数、分片区间一致、TTL 为正；不合法配置启动失败，而不是在运行期产生非法分片或无界行为。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/19 00:00
 */
@Data
@Validated
@ConfigurationProperties(prefix = "spectra.file.upload")
public class FileUploadProperties {

    /**
     * 文件上传默认实现
     */
    @NotNull(message = "默认存储实现不能为空")
    private StorageProviderType defaultStorage = StorageProviderType.LOCAL;

    /**
     * 单文件最大大小（字节）
     */
    @NotNull(message = "单文件最大大小不能为空")
    @Positive(message = "单文件最大大小必须为正数")
    private Long maxFileSize = 1024L * 1024 * 1024;

    /**
     * 分片大小,默认8M
     */
    @NotNull(message = "分片大小不能为空")
    @Positive(message = "分片大小必须为正数")
    private Long chunkSize = 8L * 1024 * 1024;

    @NotNull(message = "最小分片大小不能为空")
    @Positive(message = "最小分片大小必须为正数")
    private Long minChunkSize = 5L * 1024 * 1024;

    @NotNull(message = "最大分片大小不能为空")
    @Positive(message = "最大分片大小必须为正数")
    private Long maxChunkSize = 64L * 1024 * 1024;

    @NotNull(message = "最大分片数不能为空")
    @Positive(message = "最大分片数必须为正数")
    private Integer maxParts = 10_000;

    @NotNull(message = "并行度不能为空")
    @Positive(message = "并行度必须为正数")
    private Integer parallelism = 3;

    @NotNull(message = "单用户最大并发任务数不能为空")
    @Positive(message = "单用户最大并发任务数必须为正数")
    private Integer maxConcurrentTasksPerUser = 3;

    @NotNull(message = "任务 TTL 不能为空")
    private Duration taskTtl = Duration.ofHours(24);

    @NotNull(message = "空闲超时不能为空")
    private Duration idleTimeout = Duration.ofHours(2);

    @NotNull(message = "记录保留期不能为空")
    private Duration recordRetention = Duration.ofDays(7);

    @NotNull(message = "孤儿记录保留期不能为空")
    private Duration orphanRetention = Duration.ofDays(7);

    @NotNull(message = "预签名 TTL 不能为空")
    private Duration presignTtl = Duration.ofMinutes(15);

    /**
     * 分片大小必须落在最小/最大区间内，避免运行时出现非法分片。
     */
    @AssertTrue(message = "分片大小必须满足 minChunkSize <= chunkSize <= maxChunkSize")
    public boolean isChunkSizeWithinRange() {
        if (minChunkSize == null || chunkSize == null || maxChunkSize == null) {
            return true;
        }
        return minChunkSize <= chunkSize && chunkSize <= maxChunkSize;
    }

    /**
     * 所有 TTL 必须为正，避免清理/过期在运行期产生非法边界。
     */
    @AssertTrue(message = "上传相关 TTL 必须为正数")
    public boolean isDurationsPositive() {
        return positive(taskTtl)
                && positive(idleTimeout)
                && positive(recordRetention)
                && positive(orphanRetention)
                && positive(presignTtl);
    }

    private static boolean positive(Duration value) {
        return value != null && !value.isZero() && !value.isNegative();
    }
}
