package com.devops00.spectra.upload.configure;

import com.devops00.spectra.upload.javabean.constant.UploadType;
import com.devops00.spectra.upload.service.FileUploadService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/// 文件上传服务注册
@Component
public class FileUploadServiceRegistry {

    /// 支持的上传服务
    private final Map<UploadType, FileUploadService> serviceMap;

    /// 启动时候收集缓存
    public FileUploadServiceRegistry(List<FileUploadService> services) {
        this.serviceMap = services.stream()
                .collect(Collectors.toMap(FileUploadService::getType, v -> v));
    }

    /// 根据类型获取上传服务
    public FileUploadService getByType(UploadType type) {
        FileUploadService service = serviceMap.get(type);
        if (service == null) {
            throw new IllegalArgumentException("不支持的存储类型: " + type);
        }
        return service;
    }
}