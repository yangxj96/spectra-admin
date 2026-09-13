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

import java.util.UUID;

/**
 * 角色分配、权限和授权访问范围之间的关联实体。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */

@Data
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sec_assignment_permission_boundary", schema = "spectra_security")
public class AssignmentPermissionBoundary extends BaseEntity {

    /**
     * 访问范围所属的角色分配 ID。
     */
    @TableField(value = "assignment_id")
    private UUID assignmentId;

    /**
     * 该角色分配在此范围内可使用的权限 ID。
     */
    @TableField(value = "permission_id")
    private UUID permissionId;

    /**
     * 该角色分配访问权限时适用的授权范围 ID。
     */
    @TableField(value = "scope_id")
    private UUID scopeId;
}
