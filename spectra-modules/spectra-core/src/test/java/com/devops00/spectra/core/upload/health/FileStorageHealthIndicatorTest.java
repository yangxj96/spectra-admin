/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.upload.health;

import com.devops00.spectra.common.health.DependencyHealthStatus;
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import com.devops00.spectra.core.upload.storage.StorageHealth;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** 文件存储公共健康协议回归。 */
class FileStorageHealthIndicatorTest {

    @Test
    void shouldReturnCommonResultForSelectedProvider() {
        var properties = new FileUploadProperties();
        var provider = mock(FileStorageProvider.class);
        when(provider.type()).thenReturn(StorageProviderType.LOCAL);
        when(provider.health()).thenReturn(StorageHealth.available("LOCAL_STORAGE_REACHABLE"));

        var indicator = new FileStorageHealthIndicator(properties, List.of(provider));

        var result = indicator.check();
        assertEquals(DependencyHealthStatus.UP, result.status());
        assertEquals("OBJECT_STORAGE", result.dependencyType());
    }

    @Test
    void shouldHideProviderFailureDetailFromCommonResult() {
        var properties = new FileUploadProperties();
        var provider = mock(FileStorageProvider.class);
        when(provider.type()).thenReturn(StorageProviderType.LOCAL);
        when(provider.health()).thenReturn(StorageHealth.unavailable("LOCAL_STORAGE_UNAVAILABLE"));

        var indicator = new FileStorageHealthIndicator(properties, List.of(provider));

        var result = indicator.check();
        assertEquals(DependencyHealthStatus.DOWN, result.status());
        assertEquals("LOCAL_STORAGE_UNAVAILABLE", result.errorCode());
        assertEquals("OBJECT_STORAGE_UNAVAILABLE", result.safeSummary());
    }
}
