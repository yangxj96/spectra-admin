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

import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证 {@code FileStorageProviderRegistryTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class FileStorageProviderRegistryTest {

    @Test
    void shouldFindAndRequireRegisteredProvider() {
        FileStorageProvider provider = provider(StorageProviderType.LOCAL);
        var registry = new FileStorageProviderRegistry(List.of(provider));

        assertTrue(registry.find(StorageProviderType.LOCAL).isPresent());
        assertSame(provider, registry.find(StorageProviderType.LOCAL).orElseThrow());
        assertSame(provider, registry.require(StorageProviderType.LOCAL));
    }

    @Test
    void shouldReportMissingProviderThroughFindAndRequire() {
        var registry = new FileStorageProviderRegistry(List.of());

        assertTrue(registry.find(StorageProviderType.LOCAL).isEmpty());
        FileUploadException exception = assertThrows(FileUploadException.class,
                () -> registry.require(StorageProviderType.LOCAL));
        assertEquals(FileErrorCode.FILE_STORAGE_UNAVAILABLE, exception.getErrorCode());
    }

    @Test
    void shouldRejectDuplicateProviderTypes() {
        FileStorageProvider first = provider(StorageProviderType.LOCAL);
        FileStorageProvider second = provider(StorageProviderType.LOCAL);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> new FileStorageProviderRegistry(List.of(first, second)));

        assertTrue(exception.getMessage().contains(StorageProviderType.LOCAL.name()));
    }

    @Test
    void shouldTreatNullTypeAsUnavailable() {
        var registry = new FileStorageProviderRegistry(List.of());

        assertTrue(registry.find(null).isEmpty());
        FileUploadException exception = assertThrows(FileUploadException.class, () -> registry.require(null));
        assertEquals(FileErrorCode.FILE_STORAGE_UNAVAILABLE, exception.getErrorCode());
    }

    /**
     * 处理提供器相关数据。
     */
    private static FileStorageProvider provider(StorageProviderType type) {
        FileStorageProvider provider = mock(FileStorageProvider.class);
        when(provider.type()).thenReturn(type);
        return provider;
    }
}
