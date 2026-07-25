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
import com.devops00.spectra.common.constant.RegionLevel;
import lombok.*;

import java.io.Serializable;
import java.util.UUID;

/// GB/T 2260的行政区域数据表
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/1/30 11:45
@Data
@Builder
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_region", schema = "spectra_core")
public class Region extends BaseEntity implements Serializable {

    /// 区域名称
    @TableField(value = "name")
    private String name;

    /// 区域全称，如 北京市/北京市/东城区
    @TableField(value = "full_name")
    private String fullName;

    /// 简称
    @TableField(value = "short_name")
    private String shortName;

    /// 区域编码
    @TableField(value = "code")
    private String code;

    /// 区域路径，如 /110000/110100/110101
    @TableField(value = "path")
    private String path;

    /// 上级ID
    @TableField(value = "pid")
    private UUID pid;

    /// 行政区划层级:1省 2地级市 3县级 4乡级 5村级
    @TableField(value = "level")
    private RegionLevel level;

    /// 状态：true-启用 false-停用
    @TableField(value = "status")
    private Boolean status;

    /// 排序
    @TableField(value = "sort")
    private Integer sort;

}
