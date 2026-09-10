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

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

/** 创建密钥待启用版本入参。 */
@Data
public class SecretVersionCreateFrom {

    /** 密钥值；只在请求体短暂存在，响应不会回显。 */
    @NotBlank(message = "密钥值不能为空")
    @Size(max = 16_384, message = "密钥值长度不能超过 16384 个字符")
    private String value;

    /** 版本来源，管理页面默认为手工创建。 */
    @Size(max = 20, message = "版本来源长度不能超过 20 个字符")
    private String source = "MANUAL";
}
