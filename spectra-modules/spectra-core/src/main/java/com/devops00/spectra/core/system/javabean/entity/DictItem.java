/*
 *  Copyright 2018-2025 yangxj96
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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/// 字典-字典数据
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-18
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_dict_item")
public class DictItem extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// 字典类型ID
    @TableField(value = "gid")
    private UUID gid;

    /// 标签
    @TableField(value = "label")
    private String label;

    /// 值
    @TableField(value = "value")
    private String value;

    /// 排序
    @TableField(value = "sort")
    private Short sort;

    /// 状态
    @TableField(value = "state")
    private Short state;

    /// 是否默认
    @TableField(value = "default_flag")
    private Boolean defaultFlag;

    /// 备注
    @TableField(value = "remark")
    private String remark;
}
