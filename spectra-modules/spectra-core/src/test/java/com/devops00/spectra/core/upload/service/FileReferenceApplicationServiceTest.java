/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */
package com.devops00.spectra.core.upload.service;

import com.devops00.spectra.common.port.file.FileReferenceCommand;
import com.devops00.spectra.common.port.file.FileReferenceView;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.upload.javabean.entity.FileReference;
import com.devops00.spectra.core.upload.mapper.FileReferenceMapper;
import com.devops00.spectra.core.upload.security.FileReferencePermissionResolver;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FileReferenceApplicationServiceTest {

    @Test
    void registerDelegatesBusinessPermissionToResolverWithCurrentUser() {
        UUID assetId = UUID.randomUUID();
        UUID referenceId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        FileReferenceCommand command = new FileReferenceCommand(assetId, "oa:document", referenceId, "CONTENT", "document.pdf");
        FileReference existing = new FileReference();
        existing.setId(UUID.randomUUID());
        existing.setFileAssetId(assetId);
        existing.setReferenceType(command.referenceType());
        existing.setReferenceId(referenceId);
        existing.setPurpose(command.purpose());
        existing.setDisplayName(command.displayName());

        FileReferenceMapper referenceMapper = mock(FileReferenceMapper.class);
        FileAssetApplicationService assetService = mock(FileAssetApplicationService.class);
        SecurityContextAccessor securityContextAccessor = mock(SecurityContextAccessor.class);
        FileReferencePermissionResolver permissionResolver = mock(FileReferencePermissionResolver.class);
        when(securityContextAccessor.currentUserId()).thenReturn(userId);
        when(referenceMapper.findByKey(assetId, command.referenceType(), referenceId, command.purpose()))
                .thenReturn(existing);

        var service = new FileReferenceApplicationService(referenceMapper, assetService, securityContextAccessor,
                permissionResolver);

        FileReferenceView actual = service.register(command);

        assertEquals(existing.getId(), actual.referenceId());
        assertEquals(assetId, actual.fileAssetId());
        verify(permissionResolver).requireReadable(command.referenceType(), referenceId, userId);
        verify(assetService).requireReadyForReference(assetId, null);
    }

    @Test
    void removeByIdChecksPermissionAfterLoadingReference() {
        UUID referenceId = UUID.randomUUID();
        UUID businessId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        FileReference reference = new FileReference();
        reference.setId(referenceId);
        reference.setReferenceType("oa:document");
        reference.setReferenceId(businessId);

        FileReferenceMapper referenceMapper = mock(FileReferenceMapper.class);
        SecurityContextAccessor securityContextAccessor = mock(SecurityContextAccessor.class);
        FileReferencePermissionResolver permissionResolver = mock(FileReferencePermissionResolver.class);
        when(securityContextAccessor.currentUserId()).thenReturn(userId);
        when(referenceMapper.selectById(referenceId)).thenReturn(reference);

        var service = new FileReferenceApplicationService(referenceMapper, mock(FileAssetApplicationService.class),
                securityContextAccessor, permissionResolver);

        service.removeById(referenceId);

        verify(permissionResolver).requireReadable("oa:document", businessId, userId);
        verify(referenceMapper).softDeleteById(referenceId);
    }
}
