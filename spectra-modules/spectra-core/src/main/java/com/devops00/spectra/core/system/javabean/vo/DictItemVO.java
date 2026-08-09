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

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/// 字典数据VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/18 00:00
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DictItemVO implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// 数据id.
    private UUID id;

    /// 字典类型ID
    private UUID gid;

    /// 标签
    private String label;

    /// 值
    private String value;

    /// 排序
    private Short sort;

    /// 状态
    private Short state;

    /// 是否默认
    private Boolean defaultFlag;

    /// 备注
    private String remark;
}
