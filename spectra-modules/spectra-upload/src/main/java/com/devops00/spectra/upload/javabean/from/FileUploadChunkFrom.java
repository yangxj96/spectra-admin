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

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

/// 文件分片上传参数
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/7 23:18
@Data
public class FileUploadChunkFrom {

    /// 需要上传的文件
    private MultipartFile file;

    /// 上传ID
    private String uploadId;

    /// 文件名称
    private String fileName;

    /// hash值
    private String hash;

    /// 总分片数
    private Integer count;

    /// 当前idx
    private Integer index;
}
