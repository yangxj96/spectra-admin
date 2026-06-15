/*
 *  Copyright 2018-2025 yangxj96
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

package com.devops00.spectra.upload.service;

import com.devops00.spectra.upload.javabean.constant.UploadType;
import com.devops00.spectra.upload.javabean.entity.FileInfo;
import com.devops00.spectra.upload.javabean.from.FileUploadChunkFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadPreFrom;
import com.devops00.spectra.upload.javabean.vo.FileUploadChunkVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadPreVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadVO;
import com.github.f4b6a3.uuid.UuidCreator;
import org.jspecify.annotations.Nullable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/// 文件业务层
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/19
public interface FileUploadService {

    /// 🎯 公用常量：规定按年月归类文件夹，如 "202606"
    DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMM");

    /// 当前实现类型
    UploadType getType();

    /// 文件预处理
    ///
    /// @param from 文件信息
    /// @return 预处理结果
    FileUploadPreVO pre(FileUploadPreFrom from);

    /// 文件保存
    ///
    /// @param from 文件直接保存的参数
    FileUploadVO upload(FileUploadFrom from);

    /// 保存分片
    ///
    /// @param from 文件分片参数
    FileUploadChunkVO chunk(FileUploadChunkFrom from);

    /// 文件合并
    ///
    /// @param uploadId 上传ID
    FileUploadVO merge(String uploadId);

    /**
     * 根据文件ID预览图片
     *
     * @param file 文件信息数据
     */
    void preview(FileInfo file);


    /// 生成带年月前缀的系统唯一文件名 (例如: "202606/019eca58-xxxx...")
    ///
    /// @param filename 源文件名称
    default String generatePathFilename(@Nullable String filename) {
        // 1. 动态获取当前年月前缀，如 "202606"
        String datePrefix = LocalDate.now().format(DATE_FORMATTER);
        // 2. 生成基于时间序的唯一 UUID 字符串
        String uuid = UuidCreator.getTimeOrderedEpoch().toString();
        // 3. 健壮性处理：防止 originalFilename 为 null 导致 getSuffix 内部或者后续拼接发生空指针
        String safeFilename = (filename == null) ? "" : filename;
        // 4. 获取后缀并拼装完整路径
        return datePrefix + "/" + uuid + getSuffix(safeFilename);
    }

    /// 根据 MultipartFile 获取后缀
    ///
    /// @param file 文件对象
    default String getSuffix(@Nullable MultipartFile file) {
        if (file == null) return "";
        return getSuffix(file.getOriginalFilename());
    }

    /// 根据文件名字符串获取后缀
    ///
    /// @param filename 文件名称
    default String getSuffix(@Nullable String filename) {
        if (filename == null) return "";
        int index = filename.lastIndexOf(".");
        if (index == -1 || index == filename.length() - 1) {
            return "";
        }
        return filename.substring(index);
    }
}
