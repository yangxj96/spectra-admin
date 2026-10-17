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

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.core.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import com.devops00.spectra.core.upload.mapper.FileAssetMapper;
import com.devops00.spectra.core.upload.mapper.FileReferenceMapper;
import com.devops00.spectra.core.upload.mapper.FileTypeMapper;
import com.devops00.spectra.core.upload.mapper.FileUploadSessionMapper;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.security.FileReferencePermissionResolver;
import com.devops00.spectra.core.upload.storage.FileStorageProviderRegistry;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证引用资产时必须由创建者或持有就绪会话的操作者发起。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class FileAssetReferenceAuthorizationTest {

    private static FileAsset asset(UUID owner) {
        var asset = new FileAsset();
        asset.setId(UUID.randomUUID());
        asset.setCreatedBy(owner);
        asset.setStatus(FileAssetStatus.READY);
        asset.setSize(10L);
        asset.setContentSha256("a".repeat(64));
        asset.setContentType("text/plain");
        asset.setOriginalName("a.txt");
        asset.setFileTypeId(UUID.randomUUID());
        return asset;
    }

    private static FileAssetApplicationService service(FileAsset asset, FileUploadSessionMapper sessions) {
        var assets = mock(FileAssetMapper.class);
        when(assets.selectById(asset.getId())).thenReturn(asset);
        return new FileAssetApplicationService(assets, mock(FileReferenceMapper.class), mock(FileTypeMapper.class),
                sessions, mock(FileStorageProviderRegistry.class), mock(FileUploadProperties.class),
                mock(SecurityContextAccessor.class), mock(FileReferencePermissionResolver.class),
                mock(FileUploadConverter.class));
    }

    @Test
    void creatorCanReferenceAsset() {
        UUID owner = UUID.randomUUID();
        var asset = asset(owner);
        var result = service(asset, mock(FileUploadSessionMapper.class))
                .requireReadyForReference(asset.getId(), owner);
        assertEquals(asset.getId(), result.fileAssetId());
    }

    @Test
    void readySessionOwnerCanReferenceDeduplicatedAsset() {
        UUID owner = UUID.randomUUID();
        UUID uploader = UUID.randomUUID();
        var asset = asset(owner);
        var sessions = mock(FileUploadSessionMapper.class);
        when(sessions.countReadyOwner(asset.getId(), uploader)).thenReturn(1);

        var result = service(asset, sessions).requireReadyForReference(asset.getId(), uploader);

        assertEquals(asset.getId(), result.fileAssetId());
    }

    @Test
    void unrelatedOperatorIsRejected() {
        UUID owner = UUID.randomUUID();
        var asset = asset(owner);
        var exception = assertThrows(FileUploadException.class,
                () -> service(asset, mock(FileUploadSessionMapper.class))
                        .requireReadyForReference(asset.getId(), UUID.randomUUID()));
        assertEquals(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, exception.getErrorCode());
    }

    @Test
    void missingOperatorIsRejected() {
        UUID owner = UUID.randomUUID();
        var asset = asset(owner);
        var exception = assertThrows(FileUploadException.class,
                () -> service(asset, mock(FileUploadSessionMapper.class))
                        .requireReadyForReference(asset.getId(), null));
        assertEquals(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, exception.getErrorCode());
    }
}
