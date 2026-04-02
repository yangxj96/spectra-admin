package com.devops00.spectra.upload.service;

import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.upload.javabean.entity.FileUploadChunk;

import java.util.List;

/// 文件分片信息服务
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/2 11:47
public interface FileUploadChunkService extends BaseService<FileUploadChunk> {

    /// 根据上传ID查询已经上传的分片
    ///
    /// @param uploadId 上传ID
    /// @return 已经上传的分片
    List<FileUploadChunk> findByUploadId(String uploadId);

    /// 根据上传id查询分片已经上传的数量
    ///
    /// @param uploadId 上传ID
    /// @return 已经上传的分片
    int countByUploadId(String uploadId);
}
