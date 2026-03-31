package com.devops00.spectra.upload.service.impl;


import com.devops00.spectra.common.exception.FileTypeException;
import com.devops00.spectra.upload.javabean.from.FileChunkFrom;
import com.devops00.spectra.upload.javabean.from.FilePreprocessFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadFrom;
import com.devops00.spectra.upload.javabean.vo.FilePreprocessVO;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.service.FileUploadService;
import com.devops00.spectra.upload.strategy.FileTypeValidator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件存储-S3方式
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/3/31 14:16
 */
@Slf4j
@NullMarked
@RequiredArgsConstructor
@Service("fileUploadServiceS3Impl")
public class FileUploadServiceS3Impl implements FileUploadService {

    private final FileUploadProperties fileUploadProperties;

    private final FileTypeValidator validator;

    @Override
    public void verify(MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new FileTypeException("上传的文件不能为空");
        }
        // 使用策略模式进行文件类型验证
        if (!validator.validate(file)) {
            throw new FileTypeException("此类文件不允许上传");
        }
        // 文件大小
        if (file.getSize() > fileUploadProperties.getChunkSize()) {
            throw new FileTypeException("文件大小超过阈值");
        }
    }

    @Override
    public FilePreprocessVO preprocess(FilePreprocessFrom from) {
        return null;
    }

    @Override
    public void upload(FileUploadFrom from) {

    }

    @Override
    public void chunk(FileChunkFrom from) {

    }

    @Override
    public void merge(String md5) {

    }


}
