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
import com.devops00.spectra.core.upload.javabean.constant.TransportMode;
import com.devops00.spectra.core.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.core.upload.javabean.from.CreateUploadRequest;
import com.devops00.spectra.core.upload.javabean.vo.UploadSessionVO;
import com.devops00.spectra.core.upload.mapper.FileAssetMapper;
import com.devops00.spectra.core.upload.mapper.FileTypeMapper;
import com.devops00.spectra.core.upload.mapper.FileUploadSessionMapper;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.properties.LocalProperties;
import com.devops00.spectra.core.upload.properties.S3Properties;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import com.devops00.spectra.core.upload.storage.FileStorageProviderRegistry;
import com.devops00.spectra.core.upload.storage.StorageMultipart;
import com.devops00.spectra.core.upload.storage.local.LocalFileStorageProvider;
import com.devops00.spectra.core.upload.validator.FileDeclarationValidator;
import com.devops00.spectra.framework.properties.SystemProperties;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证 LOCAL 分片地址的上传标识与会话标识一致。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class UploadSessionStorageIdentityTest {

    private static FileUploadProperties properties() {
        var properties = mock(FileUploadProperties.class);
        when(properties.getDefaultStorage()).thenReturn(StorageProviderType.LOCAL);
        when(properties.getMaxFileSize()).thenReturn(1024L * 1024 * 1024);
        when(properties.getChunkSize()).thenReturn(5L * 1024 * 1024);
        when(properties.getMinChunkSize()).thenReturn(5L * 1024 * 1024);
        when(properties.getMaxChunkSize()).thenReturn(64L * 1024 * 1024);
        when(properties.getMaxParts()).thenReturn(10_000);
        when(properties.getParallelism()).thenReturn(3);
        when(properties.getMaxConcurrentTasksPerUser()).thenReturn(3);
        when(properties.getTaskTtl()).thenReturn(Duration.ofHours(24));
        when(properties.getIdleTimeout()).thenReturn(Duration.ofHours(2));
        return properties;
    }

    private static CreateUploadRequest request() {
        var request = new CreateUploadRequest();
        request.setOriginalName("a.txt");
        request.setContentType("text/plain");
        request.setSize(1024L);
        request.setContentSha256("a".repeat(64));
        request.setFileTypeCode("TEXT");
        return request;
    }

    @Test
    void localStorageSessionIdMatchesUploadSessionId() {
        var provider = mock(FileStorageProvider.class);
        when(provider.type()).thenReturn(StorageProviderType.LOCAL);
        when(provider.createMultipart(any(), anyString(), anyString(), anyInt()))
                .thenAnswer(invocation -> new StorageMultipart(invocation.getArgument(1), invocation.getArgument(2),
                        invocation.getArgument(0).toString()));

        var registry = mock(FileStorageProviderRegistry.class);
        when(registry.require(StorageProviderType.LOCAL)).thenReturn(provider);

        var sessions = mock(FileUploadSessionMapper.class);
        when(sessions.insert(any(FileUploadSession.class))).thenReturn(1);

        var parts = mock(UploadPartService.class);
        when(parts.findBySessionId(any())).thenReturn(List.of());

        var converter = mock(FileUploadConverter.class);
        when(converter.toUploadSessionVO(any(FileUploadSession.class))).thenReturn(new UploadSessionVO());

        var service = new UploadSessionService(properties(), mock(S3Properties.class), mock(FileTypeMapper.class),
                mock(FileAssetMapper.class), sessions, registry, mock(FileDeclarationValidator.class), parts, converter);

        service.create(request(), UUID.randomUUID());

        var captor = ArgumentCaptor.forClass(FileUploadSession.class);
        verify(sessions).insert(captor.capture());
        var session = captor.getValue();
        assertEquals(session.getId().toString(), session.getProviderUploadId());
        assertEquals(TransportMode.LOCAL_PROXY, session.getTransportMode());
    }

    @Test
    void localPartTargetUsesUploadSessionId() {
        var local = mock(LocalProperties.class);
        when(local.getStorageRoot()).thenReturn("uploads");
        when(local.getStagingRoot()).thenReturn("temp");
        var system = mock(SystemProperties.class);
        when(system.getBaseDir()).thenReturn("target/test-files");
        var provider = new LocalFileStorageProvider(local, system);

        UUID uploadId = UUID.randomUUID();
        var target = provider.createPartTarget(new FileStorageProvider.PartTargetInput(
                new StorageMultipart("local", "assets/" + uploadId + "/content.bin", uploadId.toString()),
                2, 1024L, "a".repeat(64), Instant.now().plusSeconds(60), 1));

        assertEquals("/file/uploads/" + uploadId + "/parts/2/content", target.url());
        assertTrue(target.url().contains(uploadId.toString()));
    }
}
