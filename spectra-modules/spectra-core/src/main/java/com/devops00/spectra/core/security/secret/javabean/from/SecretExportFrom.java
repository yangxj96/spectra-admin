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

package com.devops00.spectra.core.security.secret.javabean.from;

import jakarta.validation.constraints.Size;
import lombok.Data;

/**
 * 当前 ACTIVE 版本导出筛选入参。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Data
public class SecretExportFrom {

    /** 可选密钥分类；为空表示导出全部可导出分类。 */
    @Size(max = 40, message = "密钥分类长度不能超过 40 个字符")
    private String category;
}
