package com.devops00.spectra.upload.service.impl;


import com.devops00.spectra.common.exception.FileTypeException;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.service.FileService;
import com.devops00.spectra.upload.strategy.FileTypeValidator;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

/// 抽象文件类,主要是为了一些统一的处理
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/12/7 22:44
public abstract class AbstractFileService implements FileService {

    @Nullable
    protected FileTypeValidator validator;

    @Nullable
    protected FileUploadProperties properties;

    @Override
    public void verify(@Nullable MultipartFile file) {
        // 检查文件是否为空
        if (file == null || file.isEmpty()) {
            throw new FileTypeException("上传的文件不能为空");
        }
        // 使用策略模式进行文件类型验证
        if (validator == null || !validator.validate(file)) {
            throw new FileTypeException("此类文件不允许上传");
        }
        // 文件大小
        if (properties == null || file.getSize() > properties.getChunkSize()) {
            throw new FileTypeException("文件大小超过阈值");
        }
    }


}
