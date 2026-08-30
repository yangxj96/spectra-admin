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

package com.devops00.spectra.upload.health;

import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.storage.FileStorageProvider;
import com.devops00.spectra.upload.storage.StorageHealth;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 文件存储健康检查。
 *
 * <p>本地存储检查上传目录和临时目录是否可写；其他存储类型由对应 Provider 自身负责连通性检查。</p>
 */
@Component("fileStorage")
@RequiredArgsConstructor
public class FileStorageHealthIndicator implements HealthIndicator {

    private final FileUploadProperties uploadProperties;
    private final List<FileStorageProvider> providers;

    @Override
    public Health health() {
        var provider = providers.stream().filter(value -> value.type() == uploadProperties.getDefaultStorage()).findFirst();
        if (provider.isEmpty()) {
            return Health.down().withDetail("provider", uploadProperties.getDefaultStorage()).build();
        }
        StorageHealth health = provider.get().health();
        return health.available()
                ? Health.up().withDetail("provider", provider.get().type()).build()
                : Health.down().withDetail("provider", provider.get().type()).withDetail("detail", health.detail()).build();
    }
}
