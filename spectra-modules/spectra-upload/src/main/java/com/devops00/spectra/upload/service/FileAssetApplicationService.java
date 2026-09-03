/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.file.FileAccessContext;
import com.devops00.spectra.common.port.file.FileAssetPort;
import com.devops00.spectra.common.port.file.FileAssetSnapshot;
import com.devops00.spectra.common.port.file.FileDownload;
import com.devops00.spectra.upload.api.FileErrorCode;
import com.devops00.spectra.upload.api.FileUploadException;
import com.devops00.spectra.common.port.file.FileReferencePermissionChecker;
import com.devops00.spectra.upload.javabean.entity.FileReference;
import com.devops00.spectra.upload.javabean.constant.FileAssetStatus;
import com.devops00.spectra.upload.javabean.entity.FileAsset;
import com.devops00.spectra.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.upload.javabean.from.FileAssetPageRequest;
import com.devops00.spectra.upload.javabean.vo.FileAssetVO;
import com.devops00.spectra.upload.mapper.FileAssetMapper;
import com.devops00.spectra.upload.mapper.FileReferenceMapper;
import com.devops00.spectra.upload.mapper.FileTypeMapper;
import com.devops00.spectra.upload.configure.FileStorageProviderRegistry;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.storage.FileStorageProvider;
import com.devops00.spectra.upload.storage.StorageObject;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileAssetApplicationService implements FileAssetPort {

    private final FileAssetMapper assetMapper;
    private final FileReferenceMapper referenceMapper;
    private final FileTypeMapper typeMapper;
    private final FileStorageProviderRegistry providerRegistry;
    private final FileUploadProperties uploadProperties;
    private final SecurityContextAccessor securityContextAccessor;
    private final List<FileReferencePermissionChecker> permissionCheckers;
    private final FileUploadConverter fileUploadConverter;

    @Override
    @Transactional(readOnly = true)
    public FileAssetSnapshot requireReady(UUID fileAssetId) {
        return snapshot(requireAsset(fileAssetId));
    }

    @Override
    @Transactional(readOnly = true)
    public FileAssetSnapshot requireReadyForReference(UUID fileAssetId, UUID operatorId) {
        return snapshot(requireAsset(fileAssetId));
    }

    @Override
    @Transactional(readOnly = true)
    public FileDownload open(UUID fileAssetId, FileAccessContext context) {
        FileAsset asset = requireAsset(fileAssetId);
        authorize(asset, context);
        return openObject(asset, context.rangeStart(), context.rangeEnd());
    }

    @Transactional(readOnly = true)
    public FileDownload openForAdmin(UUID fileAssetId, FileAccessContext context) {
        FileAsset asset = requireAsset(fileAssetId);
        if (!isAdmin(context)) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, "file administration access denied");
        }
        return openObject(asset, context.rangeStart(), context.rangeEnd());
    }

    @Transactional(readOnly = true)
    public FileDownload openForAuthorizedUser(UUID fileAssetId, FileAccessContext context) {
        FileAsset asset = requireAsset(fileAssetId);
        if (isAdmin(context)) {
            return openObject(asset, context.rangeStart(), context.rangeEnd());
        }
        authorize(asset, context);
        return openObject(asset, context.rangeStart(), context.rangeEnd());
    }

    private FileDownload openObject(FileAsset asset, Long rangeStart, Long rangeEnd) {
        FileStorageProvider provider = providerRegistry.require(asset.getStorageProvider());
        StorageObject object = provider.open(asset.getStorageContainer(), asset.getStorageKey(), rangeStart, rangeEnd);
        long size = object.metadata().size();
        return new FileDownload(object.stream(), asset.getOriginalName(), asset.getContentType(), size,
                rangeStart, rangeEnd);
    }

    @Transactional(readOnly = true)
    public IPage<FileAssetVO> page(Page<FileAsset> page, FileAssetPageRequest request) {
        var query = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<FileAsset>()
                .like(StringUtils.hasText(request.getOriginalName()), FileAsset::getOriginalName, request.getOriginalName())
                .like(StringUtils.hasText(request.getContentSha256()), FileAsset::getContentSha256, request.getContentSha256())
                .like(StringUtils.hasText(request.getContentType()), FileAsset::getContentType, request.getContentType())
                .eq(request.getStorageProvider() != null, FileAsset::getStorageProvider, request.getStorageProvider())
                .eq(request.getStatus() != null, FileAsset::getStatus, request.getStatus())
                .isNull(FileAsset::getDeleted)
                .orderByDesc(FileAsset::getCreatedAt);
        IPage<FileAsset> assets = assetMapper.selectPage(page, query);
        return assets.convert(asset -> {
            var vo = fileUploadConverter.toAssetVO(asset);
            vo.setReferenceCount(referenceMapper.countByAssetId(asset.getId()));
            return vo;
        });
    }

    @Transactional
    public void delete(UUID fileAssetId) {
        FileAsset asset = requireAsset(fileAssetId);
        if (referenceMapper.countByAssetId(fileAssetId) > 0) {
            throw new FileUploadException(FileErrorCode.FILE_ASSET_IN_USE, "file asset still has business references");
        }
        if (asset.getStatus() == FileAssetStatus.DELETED)
            return;
        if (assetMapper.markDeleting(fileAssetId) != 1)
            return;
        providerRegistry.require(asset.getStorageProvider()).delete(asset.getStorageContainer(), asset.getStorageKey());
        assetMapper.markDeleted(fileAssetId);
    }

    private FileAsset requireAsset(UUID id) {
        FileAsset asset = assetMapper.selectById(id);
        if (asset == null || asset.getStatus() != FileAssetStatus.READY || asset.getDeleted() != null) {
            throw new FileUploadException(FileErrorCode.FILE_ASSET_NOT_READY, "file asset is not ready");
        }
        return asset;
    }

    private void authorize(FileAsset asset, FileAccessContext context) {
        if (context == null || context.userId() == null) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, "file access context is required");
        }
        UUID currentUserId = securityContextAccessor.currentUserId();
        if (!context.userId().equals(currentUserId)) {
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, "file access user does not match session");
        }
        if (StringUtils.hasText(context.referenceType()) && context.referenceId() != null) {
            FileReference reference = referenceMapper.findByKey(asset.getId(), context.referenceType(), context.referenceId(), "CONTENT");
            if (reference == null) {
                throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, "business reference does not point to asset");
            }
            for (FileReferencePermissionChecker checker : permissionCheckers) {
                if (checker.supports(context.referenceType()) && checker.canRead(context.referenceType(), context.referenceId(), context.userId())) {
                    return;
                }
            }
            throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, "business reference access denied");
        }
        if (asset.getCreatedBy() != null
                && asset.getCreatedBy().equals(context.userId())
                && asset.getCompletedAt() != null
                && asset.getCompletedAt().plus(uploadProperties.getOrphanRetention()).isAfter(java.time.Instant.now())) {
            return;
        }
        throw new FileUploadException(FileErrorCode.FILE_UPLOAD_PERMISSION_DENIED, "business reference is required");
    }

    private boolean isAdmin(FileAccessContext context) {
        UUID currentUserId = securityContextAccessor.currentUserId();
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean admin = authentication != null
                && authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority -> "file:admin:read".equals(authority.getAuthority())
                                || "file:admin:*".equals(authority.getAuthority())
                                || "*".equals(authority.getAuthority()));
        return currentUserId != null && context != null && currentUserId.equals(context.userId()) && admin;
    }

    private FileAssetSnapshot snapshot(FileAsset asset) {
        var type = typeMapper.findByIdIncludingDisabled(asset.getFileTypeId());
        return new FileAssetSnapshot(asset.getId(), asset.getOriginalName(), asset.getSize(), asset.getContentType(),
                asset.getContentSha256(), asset.getStatus().name(), type == null ? null : type.getCode());
    }

}
