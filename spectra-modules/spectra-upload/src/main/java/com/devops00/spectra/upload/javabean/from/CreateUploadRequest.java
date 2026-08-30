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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建或恢复上传会话请求。 */
@Data
public class CreateUploadRequest {

    @NotBlank(message = "原始文件名不能为空")
    @Size(max = 255, message = "原始文件名不能超过255个字符")
    private String originalName;

    @NotBlank(message = "文件类型不能为空")
    @Size(max = 128, message = "文件类型不能超过128个字符")
    private String contentType;

    @NotNull(message = "文件大小不能为空")
    @PositiveOrZero(message = "文件大小不能为负数")
    private Long size;

    @NotBlank(message = "文件摘要不能为空")
    @Pattern(regexp = "(?i)^[0-9a-f]{64}$", message = "文件摘要必须是64位SHA-256十六进制字符串")
    private String contentSha256;

    @NotBlank(message = "文件类型编码不能为空")
    @Size(max = 80, message = "文件类型编码不能超过80个字符")
    private String fileTypeCode;
}
