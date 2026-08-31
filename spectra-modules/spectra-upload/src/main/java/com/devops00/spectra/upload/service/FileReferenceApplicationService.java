/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.service;

import com.devops00.spectra.common.port.file.FileReferenceCommand;
import com.devops00.spectra.common.port.file.FileReferenceKey;
import com.devops00.spectra.common.port.file.FileReferenceService;
import com.devops00.spectra.common.port.file.FileReferenceView;
import com.devops00.spectra.upload.javabean.entity.FileReference;
import com.devops00.spectra.upload.mapper.FileReferenceMapper;
import com.devops00.spectra.common.port.file.FileReferencePermissionChecker;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileReferenceApplicationService implements FileReferenceService {

    private final FileReferenceMapper referenceMapper;
    private final FileAssetApplicationService assetService;
    private final SecurityContextAccessor securityContextAccessor;
    private final List<FileReferencePermissionChecker> permissionCheckers;

    @Override
    @Transactional
    public FileReferenceView register(FileReferenceCommand command) {
        requireBusinessPermission(command.referenceType(), command.referenceId());
        assetService.requireReadyForReference(command.fileAssetId(), null);
        FileReference existing = referenceMapper.findByKey(command.fileAssetId(), command.referenceType(), command.referenceId(), command.purpose());
        if (existing != null)
            return view(existing);
        var reference = new FileReference();
        reference.setId(UUID.randomUUID());
        reference.setFileAssetId(command.fileAssetId());
        reference.setReferenceType(command.referenceType());
        reference.setReferenceId(command.referenceId());
        reference.setPurpose(command.purpose());
        reference.setDisplayName(command.displayName());
        referenceMapper.insert(reference);
        return view(reference);
    }

    @Override
    @Transactional
    public void remove(FileReferenceKey key) {
        requireBusinessPermission(key.referenceType(), key.referenceId());
        referenceMapper.softDeleteByBusinessKeyAndPurpose(key.referenceType(), key.referenceId(), key.purpose());
    }

    @Override
    @Transactional
    public void removeById(UUID referenceId) {
        FileReference reference = referenceMapper.selectById(referenceId);
        if (reference == null || reference.getDeleted() != null)
            return;
        requireBusinessPermission(reference.getReferenceType(), reference.getReferenceId());
        referenceMapper.softDeleteById(referenceId);
    }

    @Override
    @Transactional
    public void removeByReference(String referenceType, UUID referenceId) {
        requireBusinessPermission(referenceType, referenceId);
        referenceMapper.softDeleteByBusinessKey(referenceType, referenceId);
    }

    private FileReferenceView view(FileReference reference) {
        return new FileReferenceView(reference.getId(), reference.getFileAssetId(), reference.getReferenceType(),
                reference.getReferenceId(), reference.getPurpose(), reference.getDisplayName());
    }

    private void requireBusinessPermission(String referenceType, UUID referenceId) {
        UUID userId = securityContextAccessor.currentUserId();
        if (userId == null
                || permissionCheckers.stream()
                        .noneMatch(checker -> checker.supports(referenceType)
                                && checker.canRead(referenceType, referenceId, userId))) {
            throw new com.devops00.spectra.upload.api.FileUploadException(
                    com.devops00.spectra.upload.api.FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED,
                    "business reference access denied");
        }
    }
}
