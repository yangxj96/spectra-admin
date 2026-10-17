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

import com.devops00.spectra.common.port.file.FileAccessContext;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.upload.api.FileErrorCode;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.core.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import com.devops00.spectra.core.upload.javabean.entity.FileType;
import com.devops00.spectra.core.upload.mapper.FileAssetMapper;
import com.devops00.spectra.core.upload.mapper.FileReferenceMapper;
import com.devops00.spectra.core.upload.mapper.FileTypeMapper;
import com.devops00.spectra.core.upload.mapper.FileUploadSessionMapper;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.security.FileReferencePermissionResolver;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import com.devops00.spectra.core.upload.storage.FileStorageProviderRegistry;
import com.devops00.spectra.core.upload.storage.StorageObject;
import com.devops00.spectra.core.upload.storage.StorageObjectMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证预览/下载策略在文件类型关闭对应能力后拒绝普通用户，管理员可绕过。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class FileAssetAccessPolicyTest {

    private final UUID userId = UUID.randomUUID();
    private final FileAsset asset = asset(userId);

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    private static FileAsset asset(UUID userId) {
        var asset = new FileAsset();
        asset.setId(UUID.randomUUID());
        asset.setCreatedBy(userId);
        asset.setCompletedAt(Instant.now());
        asset.setStatus(FileAssetStatus.READY);
        asset.setSize(3L);
        asset.setContentType("application/zip");
        asset.setOriginalName("a.zip");
        asset.setFileTypeId(UUID.randomUUID());
        asset.setStorageProvider(StorageProviderType.LOCAL);
        asset.setStorageContainer("local");
        asset.setStorageKey("assets/x/content.bin");
        return asset;
    }

    private static FileType type(boolean preview, boolean download) {
        var type = new FileType();
        type.setId(UUID.randomUUID());
        type.setPreviewEnabled(preview);
        type.setDownloadEnabled(download);
        return type;
    }

    private FileAssetApplicationService service(FileType type) {
        var assets = mock(FileAssetMapper.class);
        when(assets.selectById(asset.getId())).thenReturn(asset);
        var types = mock(FileTypeMapper.class);
        when(types.findByIdIncludingDisabled(asset.getFileTypeId())).thenReturn(type);
        var accessor = mock(SecurityContextAccessor.class);
        when(accessor.currentUserId()).thenReturn(userId);
        var properties = mock(FileUploadProperties.class);
        when(properties.getOrphanRetention()).thenReturn(Duration.ofDays(7));
        var provider = mock(FileStorageProvider.class);
        when(provider.open(asset.getStorageContainer(), asset.getStorageKey(), null, null))
                .thenReturn(new StorageObject(new ByteArrayInputStream(new byte[3]),
                        new StorageObjectMetadata(3, "application/zip", null, null)));
        var registry = mock(FileStorageProviderRegistry.class);
        when(registry.require(StorageProviderType.LOCAL)).thenReturn(provider);
        return new FileAssetApplicationService(assets, mock(FileReferenceMapper.class), types,
                mock(FileUploadSessionMapper.class), registry, properties, accessor,
                mock(FileReferencePermissionResolver.class), mock(FileUploadConverter.class));
    }

    private FileAccessContext context() {
        return new FileAccessContext(userId, null, null, null, null);
    }

    @Test
    void previewRejectedWhenTypeDisablesPreview() {
        var service = service(type(false, true));
        var exception = assertThrows(FileUploadException.class,
                () -> service.openForAuthorizedUser(asset.getId(), context(), false));
        assertEquals(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, exception.getErrorCode());
    }

    @Test
    void downloadRejectedWhenTypeDisablesDownload() {
        var service = service(type(true, false));
        var exception = assertThrows(FileUploadException.class,
                () -> service.openForAuthorizedUser(asset.getId(), context(), true));
        assertEquals(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, exception.getErrorCode());
    }

    @Test
    void adminBypassesTypePolicy() {
        SecurityContextHolder.getContext()
                .setAuthentication(new UsernamePasswordAuthenticationToken("admin", "n/a",
                        List.of(new SimpleGrantedAuthority("file:admin:read"))));
        var service = service(type(false, false));

        var file = service.openForAuthorizedUser(asset.getId(), context(), false);

        assertEquals("a.zip", file.displayName());
    }
}
