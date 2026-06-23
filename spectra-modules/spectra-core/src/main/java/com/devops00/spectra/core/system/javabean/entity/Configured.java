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

package com.devops00.spectra.core.system.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.common.constant.ConfiguredValueType;
import lombok.*;

import java.io.Serializable;

/// 系统配置表
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/6 00:00
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_config")
public class Configured extends BaseEntity implements Serializable {

    /// 配置key
    @TableField(value = "key")
    private String key;

    /// 配置VALUE
    @TableField(value = "value")
    private String value;

    /// 值类型
    @TableField(value = "type")
    private ConfiguredValueType type;

    /// 字典组CODE,可能会有选项之类的,直接关联一个字典做下拉选项
    @TableField(value = "dict_code")
    private String dictCode;

    /// 备注说明
    @TableField(value = "remarks")
    private String remarks;

}