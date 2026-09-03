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

package com.devops00.spectra.core.upload.health;

import com.devops00.spectra.common.health.DependencyHealthContributor;
import com.devops00.spectra.common.health.DependencyHealthResult;
import com.devops00.spectra.common.health.DependencyHealthStatus;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import com.devops00.spectra.core.upload.storage.StorageHealth;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.List;

/**
 * 文件存储健康检查。
 *
 * <p>本地存储检查上传目录和临时目录是否可写；其他存储类型由对应 Provider 自身负责连通性检查。</p>
 */
@Component("fileStorage")
@RequiredArgsConstructor
public class FileStorageHealthIndicator implements DependencyHealthContributor {

    private static final Duration TIMEOUT = Duration.ofSeconds(5);

    private final FileUploadProperties uploadProperties;
    private final List<FileStorageProvider> providers;

    @Override
    public String contributorName() {
        return "file-storage";
    }

    @Override
    public String moduleName() {
        return "upload";
    }

    @Override
    public String dependencyType() {
        return "OBJECT_STORAGE";
    }

    @Override
    public Duration timeout() {
        return TIMEOUT;
    }

    @Override
    public DependencyHealthResult check() {
        var start = System.nanoTime();
        var checkedAt = Instant.now();
        var provider = providers.stream().filter(value -> value.type() == uploadProperties.getDefaultStorage()).findFirst();
        if (provider.isEmpty()) {
            return result(DependencyHealthStatus.DOWN, start, checkedAt,
                    "STORAGE_PROVIDER_NOT_REGISTERED", "默认对象存储 Provider 未注册");
        }
        try {
            StorageHealth health = provider.get().health();
            var status = DependencyHealthStatus.fromAvailability(health.available());
            return result(status, start, checkedAt, health.errorCode(),
                    health.available() ? "OBJECT_STORAGE_REACHABLE" : "OBJECT_STORAGE_UNAVAILABLE");
        } catch (RuntimeException exception) {
            return result(DependencyHealthStatus.DOWN, start, checkedAt,
                    "STORAGE_CHECK_FAILED", "对象存储检查失败");
        }
    }

    private DependencyHealthResult result(DependencyHealthStatus status, long start, Instant checkedAt,
                                          String errorCode, String safeSummary) {
        return new DependencyHealthResult(contributorName(), moduleName(), dependencyType(), status,
                Duration.ofNanos(System.nanoTime() - start), checkedAt, errorCode, safeSummary);
    }
}
