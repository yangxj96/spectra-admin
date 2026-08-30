/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.upload.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.upload.javabean.entity.FileAsset;
import com.devops00.spectra.upload.javabean.entity.FileReference;
import com.devops00.spectra.upload.javabean.converter.FileUploadConverter;
import com.devops00.spectra.upload.javabean.from.FileReferencePageRequest;
import com.devops00.spectra.upload.javabean.vo.FileReferenceAdminVO;
import com.devops00.spectra.upload.mapper.FileAssetMapper;
import com.devops00.spectra.upload.mapper.FileReferenceMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/**
 * 文件引用管理查询服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026-08-31
 */
@Service
@RequiredArgsConstructor
public class FileReferenceAdminService {

    private final FileReferenceMapper referenceMapper;

    private final FileAssetMapper assetMapper;

    private final FileUploadConverter fileUploadConverter;

    /** 查询当前有效的业务引用；该服务不提供绕过业务权限的删除能力。 */
    @Transactional(readOnly = true)
    public IPage<FileReferenceAdminVO> page(Page<FileReference> page, FileReferencePageRequest request) {
        var query = new LambdaQueryWrapper<FileReference>()
                .eq(request != null && request.getFileAssetId() != null,
                        FileReference::getFileAssetId, request == null ? null : request.getFileAssetId())
                .eq(request != null && request.getReferenceId() != null,
                        FileReference::getReferenceId, request == null ? null : request.getReferenceId())
                .like(request != null && StringUtils.hasText(request.getReferenceType()),
                        FileReference::getReferenceType, request == null ? null : request.getReferenceType())
                .like(request != null && StringUtils.hasText(request.getPurpose()),
                        FileReference::getPurpose, request == null ? null : request.getPurpose())
                .like(request != null && StringUtils.hasText(request.getDisplayName()),
                        FileReference::getDisplayName, request == null ? null : request.getDisplayName())
                .orderByDesc(FileReference::getCreatedAt);
        return referenceMapper.selectPage(page, query).convert(reference -> {
            var vo = fileUploadConverter.toReferenceAdminVO(reference);
            FileAsset asset = assetMapper.selectById(reference.getFileAssetId());
            if (asset != null && asset.getDeleted() == null) {
                vo.setAssetOriginalName(asset.getOriginalName());
                vo.setAssetContentSha256(asset.getContentSha256());
                vo.setAssetSize(asset.getSize());
                vo.setAssetContentType(asset.getContentType());
            }
            return vo;
        });
    }
}
