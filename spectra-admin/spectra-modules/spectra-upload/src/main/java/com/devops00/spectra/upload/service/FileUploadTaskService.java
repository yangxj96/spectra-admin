package com.devops00.spectra.upload.service;


import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.upload.javabean.entity.FileUploadTask;

/// 文件上传-上传任务 服务
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/2 11:10
public interface FileUploadTaskService extends BaseService<FileUploadTask> {


    /// 根据上传ID查询上传任务信息
    ///
    /// @param uploadId 上传ID
    /// @return 任务信息
    FileUploadTask findByUploadId(String uploadId);

    /// 根据任务ID增加分片上传计数
    ///
    /// @param taskId 任务id
    void incrUploadedChunks(String taskId);
}
