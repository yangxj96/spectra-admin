package com.devops00.spectra.upload.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.upload.javabean.entity.FileUploadChunk;
import com.devops00.spectra.upload.mapper.FileUploadChunkMapper;
import com.devops00.spectra.upload.service.FileUploadChunkService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

/// 文件分片信息服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/2 11:47
@Slf4j
@Service
public class FileUploadChunkServiceImpl extends BaseServiceImpl<FileUploadChunkMapper, FileUploadChunk> implements FileUploadChunkService {

    @Override
    public List<FileUploadChunk> findByUploadId(String uploadId) {
        return lambdaQuery()
                .eq(FileUploadChunk::getUploadId, uploadId)
                .list();
    }

    @Override
    public int countByUploadId(String uploadId) {
        return Math.toIntExact(lambdaQuery()
                .eq(FileUploadChunk::getUploadId, uploadId)
                .count());
    }
}
