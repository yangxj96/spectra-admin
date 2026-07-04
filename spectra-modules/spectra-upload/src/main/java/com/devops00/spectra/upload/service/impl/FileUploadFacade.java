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

package com.devops00.spectra.upload.service.impl;

import com.devops00.spectra.upload.configure.FileUploadServiceRegistry;
import com.devops00.spectra.upload.javabean.constant.UploadType;
import com.devops00.spectra.upload.javabean.entity.FileInfo;
import com.devops00.spectra.upload.javabean.entity.FileUploadTask;
import com.devops00.spectra.upload.javabean.from.FileUploadChunkFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadPreFrom;
import com.devops00.spectra.upload.javabean.vo.FileUploadChunkVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadPreVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadVO;
import com.devops00.spectra.upload.properties.FileUploadProperties;
import com.devops00.spectra.upload.service.FileInfoService;
import com.devops00.spectra.upload.service.FileUploadTaskService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.UUID;

/// 对外门面
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/2 10:59
@Service
@RequiredArgsConstructor
public class FileUploadFacade {

    private final FileUploadServiceRegistry registry;

    private final FileUploadProperties properties;

    private final FileUploadTaskService fileUploadTaskService;

    private final FileInfoService fileInfoService;

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

    /// 从任务表获取类型
    private UploadType getTypeFromTask(String uploadId) {
        FileUploadTask task = fileUploadTaskService.findByUploadId(uploadId);
        if (task == null) {
            throw new IllegalArgumentException("上传任务不存在");
        }
        return task.getStorageType();
    }

    /// 文件预览
    ///
    /// @param id 文件ID
    public void preview(UUID id) {
        FileInfo info = fileInfoService.getById(id);
        if (info == null) {
            return;
        }
        switch (info.getStorageType()) {
            case LOCAL -> registry.getByType(UploadType.LOCAL).preview(info);
            case S3 -> registry.getByType(UploadType.S3).preview(info);
            default -> throw new RuntimeException("未识别的存储方式");
        }
    }

    /// 根据文件信息读取文件返回输入流
    ///
    /// @param fileInfo 文件信息
    public InputStream openStream(FileInfo fileInfo) {
        return switch (fileInfo.getStorageType()) {
            case LOCAL -> registry.getByType(UploadType.LOCAL).openStream(fileInfo);
            case S3 -> registry.getByType(UploadType.S3).openStream(fileInfo);
        };
    }

    /// 下载文件
    ///
    /// @param id 文件ID
    public void download(UUID id) {
        FileInfo info = fileInfoService.getById(id);
        if (info == null) {
            throw new IllegalArgumentException("文件不存在");
        }
        switch (info.getStorageType()) {
            case LOCAL -> registry.getByType(UploadType.LOCAL).download(info);
            case S3 -> registry.getByType(UploadType.S3).download(info);
            default -> throw new RuntimeException("未识别的存储方式");
        }
    }
}