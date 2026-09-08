/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.core.upload.service;

import com.devops00.spectra.common.port.file.FileAccessContext;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.upload.api.FileUploadException;
import com.devops00.spectra.core.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.core.upload.javabean.constant.StorageProviderType;
import com.devops00.spectra.core.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import com.devops00.spectra.core.upload.javabean.entity.FileReference;
import com.devops00.spectra.core.upload.mapper.FileAssetMapper;
import com.devops00.spectra.core.upload.mapper.FileReferenceMapper;
import com.devops00.spectra.core.upload.mapper.FileTypeMapper;
import com.devops00.spectra.core.upload.properties.FileUploadProperties;
import com.devops00.spectra.core.upload.security.FileReferencePermissionResolver;
import com.devops00.spectra.core.upload.storage.FileStorageProvider;
import com.devops00.spectra.core.upload.storage.FileStorageProviderRegistry;
import com.devops00.spectra.core.upload.storage.StorageObject;
import com.devops00.spectra.core.upload.storage.StorageObjectMetadata;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileAssetApplicationServiceTest {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void rejectsAuthorityWithNullNameWithoutThrowingNullPointerException() {
        var assetId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var assetMapper = mock(FileAssetMapper.class);
        var securityContextAccessor = mock(SecurityContextAccessor.class);
        var authentication = mock(Authentication.class);
        var asset = new FileAsset();
        asset.setId(assetId);
        asset.setStatus(FileAssetStatus.READY);

        when(assetMapper.selectById(assetId)).thenReturn(asset);
        when(securityContextAccessor.currentUserId()).thenReturn(userId);
        GrantedAuthority authority = () -> null;
        doReturn(List.of(authority)).when(authentication).getAuthorities();
        SecurityContextHolder.getContext().setAuthentication(authentication);

        var service = new FileAssetApplicationService(assetMapper, mock(FileReferenceMapper.class),
                mock(FileTypeMapper.class), mock(FileStorageProviderRegistry.class), mock(FileUploadProperties.class),
                securityContextAccessor, mock(FileReferencePermissionResolver.class), mock(FileUploadConverter.class));

        assertThrows(FileUploadException.class,
                () -> service.openForAdmin(assetId, FileAccessContext.user(userId)));
    }

    @Test
    void openDelegatesExistingBusinessReferencePermissionToResolver() {
        UUID assetId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        FileAsset asset = new FileAsset();
        asset.setId(assetId);
        asset.setStatus(FileAssetStatus.READY);
        asset.setStorageProvider(StorageProviderType.LOCAL);
        asset.setStorageContainer("container");
        asset.setStorageKey("key");
        asset.setOriginalName("document.pdf");
        asset.setContentType("application/pdf");
        FileReference reference = new FileReference();
        reference.setFileAssetId(assetId);
        reference.setReferenceType("oa:document");
        reference.setReferenceId(referenceId);
        reference.setPurpose("CONTENT");

        FileAssetMapper assetMapper = mock(FileAssetMapper.class);
        FileReferenceMapper referenceMapper = mock(FileReferenceMapper.class);
        FileStorageProviderRegistry providerRegistry = mock(FileStorageProviderRegistry.class);
        FileStorageProvider provider = mock(FileStorageProvider.class);
        SecurityContextAccessor securityContextAccessor = mock(SecurityContextAccessor.class);
        FileReferencePermissionResolver permissionResolver = mock(FileReferencePermissionResolver.class);
        when(assetMapper.selectById(assetId)).thenReturn(asset);
        when(referenceMapper.findByKey(assetId, "oa:document", referenceId, "CONTENT")).thenReturn(reference);
        when(securityContextAccessor.currentUserId()).thenReturn(userId);
        when(providerRegistry.require(StorageProviderType.LOCAL)).thenReturn(provider);
        when(provider.open("container", "key", null, null)).thenReturn(new StorageObject(
                new ByteArrayInputStream(new byte[]{1}), new StorageObjectMetadata(1, "application/pdf", null, null)));

        var service = new FileAssetApplicationService(assetMapper, referenceMapper, mock(FileTypeMapper.class),
                providerRegistry, mock(FileUploadProperties.class), securityContextAccessor, permissionResolver,
                mock(FileUploadConverter.class));

        service.open(assetId, new FileAccessContext(userId, "oa:document", referenceId, null, null));

        verify(permissionResolver).requireReadable("oa:document", referenceId, userId);
    }
}
