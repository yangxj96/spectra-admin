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
import com.devops00.spectra.upload.javabean.from.FileUploadChunkFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadFrom;
import com.devops00.spectra.upload.javabean.from.FileUploadPreFrom;
import com.devops00.spectra.upload.javabean.vo.FileUploadChunkVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadPreVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadStatusVO;
import com.devops00.spectra.upload.javabean.vo.FileUploadVO;

/// 文件业务层
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/6/19
public interface FileUploadService {

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

    /// 获取文件上传扎ungtai
    ///
    /// @param uploadId 上传ID
    /// @return 文件上传状态
    FileUploadStatusVO getStatus(String uploadId);
}
