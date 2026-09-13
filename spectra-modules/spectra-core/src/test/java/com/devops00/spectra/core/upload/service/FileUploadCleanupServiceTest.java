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

package com.devops00.spectra.core.upload.service;

import com.devops00.spectra.core.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import com.devops00.spectra.core.upload.storage.FileStorageProviderRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证删除文件资产时先清理外部存储，再删除数据库记录；失败时保留记录并安排重试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class FileUploadCleanupServiceTest {

    private static final Instant NOW = Instant.parse("2026-08-31T00:00:00Z");

    @Test
    void finalizesAssetOnlyAfterProviderDeleteAndRetriesDeletingAssetOnFailure() {
        var transactionService = mock(FileUploadCleanupTransactionService.class);
        var registry = mock(FileStorageProviderRegistry.class);
        var localProvider = mock(FileStorageProvider.class);
        var s3Provider = mock(FileStorageProvider.class);
        var success = asset(StorageProviderType.LOCAL, "asset-local");
        var failed = asset(StorageProviderType.S3, "asset-s3");
        when(transactionService.claimExpiredSessions(any(), any(), any(), eq(100))).thenReturn(List.of());
        when(transactionService.claimSessionCleanupCandidates(any(), any(), eq(100))).thenReturn(List.of());
        when(transactionService.markOrphans(any(), any(), eq(100))).thenReturn(2);
        when(transactionService.claimAssetCleanupCandidates(any(), any(), eq(100))).thenReturn(List.of(success, failed));
        when(registry.require(StorageProviderType.LOCAL)).thenReturn(localProvider);
        when(registry.require(StorageProviderType.S3)).thenReturn(s3Provider);
        doThrow(new IllegalStateException("delete unavailable"))
                .when(s3Provider)
                .delete("bucket", "asset-s3");
        var cleanupService = new FileUploadCleanupService(new FileUploadProperties(), transactionService, registry);

        var result = cleanupService.cleanupBatch(NOW);

        InOrder order = inOrder(localProvider, s3Provider, transactionService);
        order.verify(localProvider).delete("bucket", "asset-local");
        order.verify(transactionService).finishAsset(success.getId());
        order.verify(s3Provider).delete("bucket", "asset-s3");
        order.verify(transactionService).scheduleAssetRetry(eq(failed.getId()), eq(NOW.plus(Duration.ofMinutes(5))));
        verify(transactionService).finishAsset(success.getId());
        verify(transactionService).scheduleAssetRetry(eq(failed.getId()), eq(NOW.plus(Duration.ofMinutes(5))));
        assertEquals(2L, result.orphanedAssets());
        assertEquals(1L, result.deletedAssets());
        assertEquals(1L, result.assetRetryScheduled());
    }

    /**
     * 创建清理中的文件资产。
     */
    private static FileAsset asset(StorageProviderType provider, String key) {
        var asset = new FileAsset();
        asset.setId(UUID.randomUUID());
        asset.setStatus(FileAssetStatus.DELETING);
        asset.setStorageProvider(provider);
        asset.setStorageContainer("bucket");
        asset.setStorageKey(key);
        return asset;
    }
}
