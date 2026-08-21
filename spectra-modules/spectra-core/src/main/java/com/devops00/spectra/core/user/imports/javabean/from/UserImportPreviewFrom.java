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

package com.devops00.spectra.core.user.imports.javabean.from;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

/** 用户导入 Preview 请求。文件解析后以固定模板行提交，正式写入只发生在 Apply。 */
@Data
public class UserImportPreviewFrom {

    @NotBlank(message = "导入幂等键不能为空")
    @Size(max = 120, message = "导入幂等键不能超过 120 个字符")
    private String idempotencyKey;

    @NotBlank(message = "文件名不能为空")
    @Size(max = 255, message = "文件名不能超过 255 个字符")
    private String fileName;

    @NotBlank(message = "文件摘要不能为空")
    @Size(max = 128, message = "文件摘要不能超过 128 个字符")
    private String fileHash;

    private boolean skipExisting;

    @NotEmpty(message = "导入数据不能为空")
    @Size(max = 2000, message = "单次最多导入 2000 行")
    @Valid
    private List<UserImportRowFrom> rows;
}
