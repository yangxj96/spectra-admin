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
import com.devops00.spectra.framework.persistence.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * 部门之间的祖先与后代闭包关系；{@code depth} 表示两者之间的层级距离。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/13
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@TableName(value = "sys_department_closure", schema = "spectra_core")
public class DepartmentClosure extends BaseEntity {

    /**
     * 层级关系中的祖先部门 ID。
     */
    @TableField(value = "ancestor_id")
    private UUID ancestorId;

    /**
     * 层级关系中的后代部门 ID。
     */
    @TableField(value = "descendant_id")
    private UUID descendantId;

    /**
     * 后代部门与祖先部门之间的层级距离；直属关系为 1。
     */
    @TableField(value = "depth")
    private Integer depth;
}
