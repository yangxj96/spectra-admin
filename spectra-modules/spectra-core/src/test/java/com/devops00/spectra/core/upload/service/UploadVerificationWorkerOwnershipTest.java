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

import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.core.upload.javabean.constant.UploadSessionStatus;
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import com.devops00.spectra.core.upload.javabean.entity.FileType;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.core.upload.mapper.FileAssetMapper;
import com.devops00.spectra.core.upload.mapper.FileTypeMapper;
import com.devops00.spectra.core.upload.mapper.FileUploadPartMapper;
import com.devops00.spectra.core.upload.mapper.FileUploadSessionMapper;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import com.devops00.spectra.core.upload.storage.FileStorageProviderRegistry;
import com.devops00.spectra.core.upload.storage.StorageObject;
import com.devops00.spectra.core.upload.storage.StorageObjectMetadata;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证异步校验创建资产时继承会话所有者，不依赖空安全上下文。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class UploadVerificationWorkerOwnershipTest {

    @Test
    void createdAssetInheritsSessionOwner() throws Exception {
        UUID uploadId = UUID.randomUUID();
        UUID owner = UUID.randomUUID();
        byte[] payload = "owner-inheritance".getBytes(StandardCharsets.UTF_8);
        String sha256 = HexFormat.of().formatHex(MessageDigest.getInstance("SHA-256").digest(payload));

        var session = new FileUploadSession();
        session.setId(uploadId);
        session.setOwnerUserId(owner);
        session.setStatus(UploadSessionStatus.VERIFYING);
        session.setStorageProvider(StorageProviderType.S3);
        session.setStorageContainer("bucket");
        session.setStagingKey("key");
        session.setProviderUploadId("upload-id");
        session.setOriginalName("a.txt");
        session.setDeclaredContentType("text/plain");
        session.setSize((long) payload.length);
        session.setContentSha256(sha256);

        var sessionMapper = mock(FileUploadSessionMapper.class);
        when(sessionMapper.selectForUpdate(uploadId)).thenReturn(session);
        var partMapper = mock(FileUploadPartMapper.class);
        when(partMapper.findBySessionId(uploadId)).thenReturn(List.of());
        var fileTypeMapper = mock(FileTypeMapper.class);
        var fileType = new FileType();
        fileType.setId(UUID.randomUUID());
        when(fileTypeMapper.findEnabledByContentType("text/plain")).thenReturn(fileType);
        var fileAssetMapper = mock(FileAssetMapper.class);
        when(fileAssetMapper.findReady(anyString(), anyLong())).thenReturn(null);
        when(fileAssetMapper.insert(any(FileAsset.class))).thenAnswer(invocation -> {
            ((FileAsset) invocation.getArgument(0)).setId(UUID.randomUUID());
            return 1;
        });
        var provider = mock(FileStorageProvider.class);
        when(provider.open("bucket", "key", null, null)).thenReturn(new StorageObject(
                new ByteArrayInputStream(payload),
                new StorageObjectMetadata(payload.length, "text/plain", sha256, null)));
        var registry = mock(FileStorageProviderRegistry.class);
        when(registry.require(StorageProviderType.S3)).thenReturn(provider);

        var worker = new UploadVerificationWorker(mock(FileUploadProperties.class), fileTypeMapper,
                fileAssetMapper, sessionMapper, partMapper, registry);
        worker.verify(uploadId);

        var captor = ArgumentCaptor.forClass(FileAsset.class);
        verify(fileAssetMapper).insert(captor.capture());
        assertEquals(owner, captor.getValue().getCreatedBy());
        assertEquals(owner, captor.getValue().getUpdatedBy());
    }
}
