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

package com.devops00.spectra.core.system.javabean.vo;

import com.devops00.spectra.common.constant.ConfiguredValueType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/// 系统配置分页响应
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConfiguredVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// 主键ID
    private UUID id;

    /// 配置key
    private String key;

    /// 配置VALUE
    private String value;

    /// 值类型
    private ConfiguredValueType type;

    /// 字典code
    private String dictCode;

    /// 备注说明
    private String remarks;

}
