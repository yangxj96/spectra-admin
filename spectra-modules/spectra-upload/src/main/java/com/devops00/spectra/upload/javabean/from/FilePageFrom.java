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

package com.devops00.spectra.upload.javabean.from;

import com.devops00.spectra.upload.javabean.constant.UploadType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/// 文件分页查询参数
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/4 16:00
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FilePageFrom {

    /// 原始文件名(模糊搜索)
    private String originalName;

    /// 存储类型(LOCAL/S3)
    private UploadType storageType;

}