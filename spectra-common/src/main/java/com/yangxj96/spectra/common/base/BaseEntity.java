/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.common.base;

import com.baomidou.mybatisplus.annotation.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 基础实体类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据id.
     */
    @TableId(value = "\"id\"", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 创建人
     */
    @TableField(value = "\"created_by\"", fill = FieldFill.INSERT)
    private Long createdBy;

    /**
     * 创建时间
     */
    @OrderBy(asc = true)
    @TableField(value = "\"created_at\"", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    /**
     * 更新人
     */
    @TableField(value = "\"updated_by\"", fill = FieldFill.INSERT_UPDATE)
    private Long updatedBy;

    /**
     * 更新时间
     */
    @TableField(value = "\"updated_at\"", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    /**
     * 删除标识
     */
    @TableField(value = "\"deleted\"")
    private LocalDateTime deleted;
}
