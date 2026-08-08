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

package com.devops00.spectra.oa.contract.javabean.from;

import java.util.UUID;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/// 合同版本保存参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Data
public class ContractVersionFrom {

    /// 文件 ID。
    @NotNull(message = "合同文件不能为空")
    private UUID fileId;

    /// 文件名称。
    private String fileName;

    /// 文件大小。
    private Long fileSize;

    /// 内容类型。
    private String contentType;

    /// 版本说明。
    private String versionNote;
}
