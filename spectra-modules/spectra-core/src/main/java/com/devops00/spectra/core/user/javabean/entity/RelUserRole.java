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
import lombok.*;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/// 用户和角色中间表
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/11 00:00
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_rel_user_role", schema = "spectra_core")
public class RelUserRole extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// 用户ID
    @TableField(value = "user_id")
    private UUID userId;

    /// 角色ID
    @TableField(value = "role_id")
    private UUID roleId;

}
