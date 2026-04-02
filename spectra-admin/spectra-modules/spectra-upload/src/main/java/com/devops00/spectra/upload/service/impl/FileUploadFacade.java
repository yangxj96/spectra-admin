package com.devops00.spectra.upload.service.impl;

import com.devops00.spectra.upload.configure.FileUploadServiceRegistry;
import com.devops00.spectra.upload.javabean.constant.UploadType;
import com.devops00.spectra.upload.javabean.entity.FileUploadTask;
import com.devops00.spectra.upload.javabean.from.FileUploadChunkFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadPreFrom;
import com.devops00.spectra.upload.javabean.vo.FileUploadChunkVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadPreVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadStatusVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadVO;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.service.FileUploadTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/// 对外门面
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/2 10:59
@Service
@RequiredArgsConstructor
public class FileUploadFacade {

    private final FileUploadServiceRegistry registry;

    private final FileUploadProperties properties;

    private final FileUploadTaskService fileUploadTaskService;

    /// 预处理
    public FileUploadPreVO pre(FileUploadPreFrom from) {
        return registry.getByType(properties.getDefaultStorage()).pre(from);
    }

    /// 直接上传
    public FileUploadVO upload(FileUploadFrom from) {
        return registry.getByType(properties.getDefaultStorage()).upload(from);
    }

    /// 分片上传
    public FileUploadChunkVO chunk(FileUploadChunkFrom from) {
        // 分片任务没存储上传类型，因为上传都是用默认，只有下载才寻要根据数据库选择
        return registry.getByType(properties.getDefaultStorage()).chunk(from);
    }

    /// 合并
    public FileUploadVO merge(String uploadId) {
        UploadType type = getTypeFromTask(uploadId);
        return registry.getByType(type).merge(uploadId);
    }

    /// 状态查询
    public FileUploadStatusVO getStatus(String uploadId) {
        return registry.getByType(properties.getDefaultStorage()).getStatus(uploadId);
    }

    /// 从任务表获取类型
    private UploadType getTypeFromTask(String uploadId) {
        FileUploadTask task = fileUploadTaskService.findByUploadId(uploadId);
        if (task == null) {
            throw new IllegalArgumentException("上传任务不存在");
        }
        return task.getStorageType();
    }

}