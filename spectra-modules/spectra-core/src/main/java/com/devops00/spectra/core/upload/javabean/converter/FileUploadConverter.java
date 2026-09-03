/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */
package com.devops00.spectra.core.upload.javabean.converter;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.framework.configure.mapstruct.GlobalMapperConfig;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.core.upload.javabean.entity.FileAsset;
import com.devops00.spectra.core.upload.javabean.entity.FileReference;
import com.devops00.spectra.core.upload.javabean.entity.FileType;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadPart;
import com.devops00.spectra.core.upload.javabean.entity.FileUploadSession;
import com.devops00.spectra.core.upload.javabean.vo.FileAssetVO;
import com.devops00.spectra.core.upload.javabean.vo.FileReferenceAdminVO;
import com.devops00.spectra.core.upload.javabean.vo.FileReferenceVO;
import com.devops00.spectra.core.upload.javabean.vo.FileTypePolicyVO;
import com.devops00.spectra.core.upload.javabean.vo.FileUploadAdminDetailVO;
import com.devops00.spectra.core.upload.javabean.vo.FileUploadAdminVO;
import com.devops00.spectra.core.upload.javabean.vo.FileUploadPartAdminVO;
import com.devops00.spectra.core.upload.javabean.vo.PartTargetVO;
import com.devops00.spectra.core.upload.javabean.vo.UploadSessionVO;
import com.devops00.spectra.core.upload.storage.PartTarget;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.List;

/** 文件上传领域实体和 HTTP 响应 VO 的统一转换器。 */
@Mapper(uses = TimeMapper.class, config = GlobalMapperConfig.class)
public interface FileUploadConverter {

    /** 将文件资产实体转换为管理响应。 */
    @Mapping(target = "fileAssetId", source = "id")
    @Mapping(target = "referenceCount", ignore = true)
    FileAssetVO toAssetVO(FileAsset source);

    /** 将文件资产分页转换为管理响应分页。 */
    @Mapping(target = "pages", ignore = true)
    Page<FileAssetVO> toAssetVOPage(Page<FileAsset> source);

    /** 将文件类型策略实体转换为管理响应。 */
    FileTypePolicyVO toTypePolicyVO(FileType source);

    /** 将文件类型策略分页转换为管理响应分页。 */
    @Mapping(target = "pages", ignore = true)
    Page<FileTypePolicyVO> toTypePolicyVOPage(Page<FileType> source);

    /** 将上传会话实体转换为管理列表响应。 */
    @Mapping(target = "uploadId", source = "id")
    FileUploadAdminVO toUploadAdminVO(FileUploadSession source);

    /** 将上传会话实体转换为管理详情响应。 */
    @Mapping(target = "uploadId", source = "id")
    @Mapping(target = "parts", ignore = true)
    FileUploadAdminDetailVO toUploadAdminDetailVO(FileUploadSession source);

    /** 将上传分片实体转换为管理响应。 */
    FileUploadPartAdminVO toUploadPartAdminVO(FileUploadPart source);

    /** 将上传分片实体列表转换为管理响应列表。 */
    List<FileUploadPartAdminVO> toUploadPartAdminVOList(List<FileUploadPart> source);

    /** 将文件引用实体转换为管理响应；资产冗余字段由应用服务补充。 */
    @Mapping(target = "referenceId", source = "id")
    @Mapping(target = "businessReferenceId", source = "referenceId")
    @Mapping(target = "assetOriginalName", ignore = true)
    @Mapping(target = "assetContentSha256", ignore = true)
    @Mapping(target = "assetSize", ignore = true)
    @Mapping(target = "assetContentType", ignore = true)
    FileReferenceAdminVO toReferenceAdminVO(FileReference source);

    /** 将文件引用实体转换为业务响应。 */
    @Mapping(target = "referenceId", source = "id")
    FileReferenceVO toReferenceVO(FileReference source);

    /** 将分片上传目标转换为 HTTP 响应。 */
    PartTargetVO toPartTargetVO(PartTarget source);

    /** 将上传会话实体转换为统一上传响应。 */
    @Mapping(target = "uploadId", source = "id")
    @Mapping(target = "errorCode", source = "failureCode")
    UploadSessionVO toUploadSessionVO(FileUploadSession source);

    /** 将已存在资产转换为秒传响应的基础字段。 */
    @Mapping(target = "uploadId", ignore = true)
    @Mapping(target = "result", ignore = true)
    @Mapping(target = "status", ignore = true)
    @Mapping(target = "verificationProgress", ignore = true)
    @Mapping(target = "fileAssetId", source = "id")
    @Mapping(target = "uploadedBytes", source = "size")
    UploadSessionVO toDeduplicatedUploadVO(FileAsset source);
}
