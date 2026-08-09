/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.upload.configure;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.upload.javabean.constant.UploadType;
import com.devops00.spectra.upload.service.FileUploadService;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 文件上传服务注册
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/06/27 00:00
 */
@Component
public class FileUploadServiceRegistry {

    /**
     * 支持的上传服务
     */
    private final Map<UploadType, FileUploadService> serviceMap;

    /**
     * 启动时候收集缓存
     */
    public FileUploadServiceRegistry(List<FileUploadService> services) {
        this.serviceMap = services.stream().collect(Collectors.toMap(FileUploadService::getType, v -> v));
    }

    /**
     * 根据类型获取上传服务
     */
    public FileUploadService getByType(UploadType type) {
        FileUploadService service = serviceMap.get(type);
        if (service == null) {
            throw new DataException("不支持的存储类型: " + type);
        }
        return service;
    }
}