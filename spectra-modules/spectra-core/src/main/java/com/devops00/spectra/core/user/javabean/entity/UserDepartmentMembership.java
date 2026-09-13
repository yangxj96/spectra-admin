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
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 用户与部门之间的主部门或关联部门关系。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_user_department_membership", schema = "spectra_core")
public class UserDepartmentMembership extends BaseEntity {

    /**
     * 建立该部门关系的用户 ID。
     */
    @TableField(value = "user_id")
    private UUID userId;

    /**
     * 与用户建立关系的部门 ID。
     */
    @TableField(value = "department_id")
    private UUID departmentId;

    /**
     * 用户与该部门关系的类型，例如主部门或关联部门。
     */
    @TableField(value = "membership_type")
    private String membershipType;
}
