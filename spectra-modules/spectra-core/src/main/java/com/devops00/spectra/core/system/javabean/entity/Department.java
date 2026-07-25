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

import com.baomidou.mybatisplus.annotation.FieldStrategy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/// 组织机构
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/15 00:00
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_department", schema = "spectra_core")
public class Department extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// 上级ID
    @Nullable
    @TableField(value = "pid")
    private UUID pid;

    /// 名称
    @TableField(value = "name")
    private String name;

    /// 编码
    /// > 插入时候生成,后续不参与更新等操作
    @TableField(value = "code", insertStrategy = FieldStrategy.NOT_NULL, updateStrategy = FieldStrategy.NEVER)
    private String code;

    /// 组织机构类型
    ///
    /// 对应字典组:sys_organization_type
    @TableField(value = "type")
    private String type;

    /// 行政区划ID
    @TableField("region_id")
    private UUID regionId;

    /// 构建路径
    /// > 格式:比如总部/二级/三级/部门
    @TableField(value = "path")
    private String path;

    /// 排序,默认0
    @TableField(value = "sort")
    private Integer sort;

    /// 备注
    @TableField(value = "remark")
    private String remark;
}

