package com.devops00.spectra.upload.service.impl;


import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.upload.javabean.constant.UploadType;
import com.devops00.spectra.upload.javabean.entity.FileUploadTask;
import com.devops00.spectra.upload.mapper.FileUploadTaskMapper;
import com.devops00.spectra.upload.service.FileUploadTaskService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/// 文件上传-上传任务 服务默认实现
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/2 11:10
@Slf4j
@Service
public class FileUploadTaskServiceImpl extends BaseServiceImpl<FileUploadTaskMapper, FileUploadTask> implements FileUploadTaskService {

    @Override
    public FileUploadTask findByUploadId(String uploadId) {
        return lambdaQuery()
                .eq(FileUploadTask::getUploadId, uploadId)
                .select(FileUploadTask::getStorageType)
                .oneOpt()
                .orElseThrow(() -> new IllegalArgumentException("上传任务不存在"));
    }

    @Override
    public void incrUploadedChunks(String taskId) {

    }
}
