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

package com.devops00.spectra.upload.service;

import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.upload.javabean.entity.FileUploadTask;

/// 文件上传-上传任务 服务
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/2 11:10
public interface FileUploadTaskService extends BaseService<FileUploadTask> {

    /// 根据上传ID查询上传任务信息
    ///
    /// @param uploadId 上传ID
    /// @return 任务信息
    FileUploadTask findByUploadId(String uploadId);
}
