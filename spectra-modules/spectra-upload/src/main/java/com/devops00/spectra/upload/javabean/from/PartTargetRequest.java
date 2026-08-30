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
import jakarta.validation.constraints.Positive;
import lombok.Data;

/** 请求分片上传目标。 */
@Data
public class PartTargetRequest {

    @NotNull(message = "分片大小不能为空")
    @Positive(message = "分片大小必须为正数")
    private Long partSize;

    @NotBlank(message = "分片摘要不能为空")
    @Pattern(regexp = "(?i)^[0-9a-f]{64}$", message = "分片摘要必须是64位SHA-256十六进制字符串")
    private String partSha256;
}
