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

package com.devops00.spectra.upload.javabean.vo;


import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

/// 文件上传-预处理-VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/1 16:42
@Getter
@Setter
public class FileUploadPreVO {

    /// 文件是否已经存在，存在就不在继续了
    private boolean exists;

    /// 文件ID
    private UUID fileId;

    /// 是否需要分片
    private boolean multipart;

    /// 文件上传ID
    private String uploadId;

    /// 分片大小
    private long chunkSize;

}
