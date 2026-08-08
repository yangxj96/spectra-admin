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

package com.devops00.spectra.oa.asset.javabean.from;

import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

/// 资产分类保存参数。
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/8/8
@Data
public class AssetCategorySaveFrom {

    /// 父级 ID。
    private UUID pid;

    /// 编码。
    @NotBlank(message = "资产分类编码不能为空")
    private String code;

    /// 名称。
    @NotBlank(message = "资产分类名称不能为空")
    private String name;

    /// 资产类型。
    private String assetType = "FIXED";

    /// 排序号。
    private Integer sort = 0;

    /// 是否启用。
    private Boolean enabled = true;

    /// 描述。
    private String description;
}
