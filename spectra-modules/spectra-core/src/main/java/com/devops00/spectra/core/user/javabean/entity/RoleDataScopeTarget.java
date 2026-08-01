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

package com.devops00.spectra.core.user.javabean.entity;


import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

/// 角色数据范围(自定义数据范围的时候使用)
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/12/23 11:24
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_role_data_scope_target", schema = "spectra_core")
public class RoleDataScopeTarget extends BaseEntity implements Serializable {

    /// 角色ID
    @TableField(value = "role_id")
    private UUID roleId;

    /// 目标ID
    @TableField(value = "target_id")
    private UUID targetId;

    /// 目标类型
    @TableField(value = "target_type")
    private Integer targetType;
}
