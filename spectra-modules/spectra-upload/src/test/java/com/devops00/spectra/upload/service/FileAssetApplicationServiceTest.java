/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.upload.service;

import com.devops00.spectra.common.port.file.FileAccessContext;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.upload.api.FileUploadException;
import com.devops00.spectra.upload.configure.FileStorageProviderRegistry;
import com.devops00.spectra.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.upload.javabean.entity.FileAsset;
import com.devops00.spectra.upload.mapper.FileAssetMapper;
import com.devops00.spectra.upload.mapper.FileReferenceMapper;
import com.devops00.spectra.upload.mapper.FileTypeMapper;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doReturn;
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
                securityContextAccessor, List.of(), mock(FileUploadConverter.class));

        assertThrows(FileUploadException.class,
                () -> service.openForAdmin(assetId, FileAccessContext.user(userId)));
    }
}
