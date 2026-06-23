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

package com.devops00.spectra.upload.javabean.domain;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/// 文件魔数规则
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/4/17 9:42
@Getter
@Setter
@ToString
public class MagicRule {

    /**
     * 魔数（十六进制字符串）
     * 示例: FFD8FF
     */
    private String bytes;

    /**
     * 偏移量
     */
    private Integer offset = 0;

    /**
     * 可选：描述
     */
    private String description;

    /**
     * 编译后的字节（不入库）
     */
    private transient byte[] compiled;
}