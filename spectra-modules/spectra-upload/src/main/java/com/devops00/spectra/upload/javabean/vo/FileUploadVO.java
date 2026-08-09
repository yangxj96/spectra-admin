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

import java.util.UUID;

import lombok.Data;

/// 普通上传的上传结果
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/1 16:42
@Data
public class FileUploadVO {

    /// 请求地址
    private String url;

    /// 文件主键，供业务表建立附件关联
    private UUID fileId;
}
