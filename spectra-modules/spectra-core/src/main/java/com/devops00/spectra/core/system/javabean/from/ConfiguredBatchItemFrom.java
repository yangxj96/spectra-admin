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

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 系统配置批量修改项。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfiguredBatchItemFrom {

    /** 配置项 ID。 */
    @NotNull(message = "配置项ID不能为空")
    private UUID id;

    /** 配置值。 */
    @NotNull(message = "配置值不能为空")
    private String value;

    /** 配置说明。 */
    private String remarks;
}
