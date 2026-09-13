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

package com.devops00.spectra.core.system.javabean.from;

import com.devops00.spectra.core.system.javabean.enums.ConfiguredCategory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 系统配置分类批量修改入参。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguredBatchFrom {

    /** 本次提交的配置业务分类。 */
    @NotNull(message = "配置分类不能为空")
    private ConfiguredCategory category;

    /** 当前分类的完整表单项。 */
    @Valid
    @NotEmpty(message = "至少提交一项系统配置")
    @Size(max = 100, message = "单次最多保存100项系统配置")
    private List<ConfiguredBatchItemFrom> items;
}
