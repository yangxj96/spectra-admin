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

package com.devops00.spectra.core.security.authorization.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.Instant;
import java.util.UUID;

/**
 * 用户与安全角色之间的有效分配关系。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_role_assignment", schema = "spectra_security")
public class RoleAssignment extends BaseEntity {

    /**
     * 获得角色分配的用户 ID。
     */
    @TableField(value = "user_id")
    private UUID userId;

    /**
     * 分配给用户的安全角色 ID。
     */
    @TableField(value = "role_id")
    private UUID roleId;

    /**
     * 角色分配当前的生命周期状态。
     */
    @TableField(value = "state")
    private String state;

    /**
     * 角色分配开始生效的时间；为空时不限制起始时间。
     */
    @TableField(value = "valid_from")
    private Instant validFrom;

    /**
     * 角色分配失效的时间；为空时不设置截止时间。
     */
    @TableField(value = "valid_until")
    private Instant validUntil;

    /**
     * 角色分配记录的乐观锁版本号。
     */
    @TableField(value = "version")
    private Long version;
}
