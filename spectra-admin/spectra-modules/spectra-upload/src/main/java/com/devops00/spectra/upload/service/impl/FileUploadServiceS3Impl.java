package com.devops00.spectra.upload.service.impl;


import com.devops00.spectra.upload.javabean.constant.UploadType;
import com.devops00.spectra.upload.javabean.from.FileUploadChunkFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadPreFrom;
import com.devops00.spectra.upload.javabean.vo.FileUploadChunkVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadPreVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadStatusVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadVO;
import com.devops00.spectra.upload.service.FileUploadService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/// 文件上传服务-S3协议
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/4/2 10:59
@Slf4j
@Service("fileUploadServiceS3Impl")
public class FileUploadServiceS3Impl implements FileUploadService {
    @Override
    public UploadType getType() {
        return UploadType.S3;
    }

    @Override
    public FileUploadPreVO pre(FileUploadPreFrom from) {
        return null;
    }

    @Override
    public FileUploadVO upload(FileUploadFrom from) {
        return null;
    }

    @Override
    public FileUploadChunkVO chunk(FileUploadChunkFrom from) {
        return null;
    }

    @Override
    public FileUploadVO merge(String uploadId) {
        return null;
    }

    @Override
    public FileUploadStatusVO getStatus(String uploadId) {
        return null;
    }

    @Override
    public void preview(UUID fileId) {

    }
}
