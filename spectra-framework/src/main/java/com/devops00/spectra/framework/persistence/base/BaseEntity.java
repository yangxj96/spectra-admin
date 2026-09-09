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

package com.devops00.spectra.framework.persistence.base;

import com.baomidou.mybatisplus.annotation.FieldFill;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.OrderBy;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.Version;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.Instant;
import java.util.UUID;

/**
 * 供数据库实体复用的持久化基类。
 *
 * <p>该类集中声明 UUID 主键、审计字段、软删除字段和乐观锁字段；新增实体只需继承本类并补充自身表字段，
 * 主键和审计字段的自动填充由 framework 的 MyBatis-Plus 配置负责。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025-6-14 00:00
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据 ID。新增时由全局 MyBatis-Plus MetaObjectHandler 生成 UUID v7。
     */
    @TableId(value = "id", type = IdType.INPUT)
    private UUID id;

    /**
     * 创建该记录的用户 ID；新增时由持久化填充器从当前安全上下文写入。
     */
    @TableField(value = "created_by", fill = FieldFill.INSERT)
    private UUID createdBy;

    /**
     * 记录首次持久化的时间；新增时由持久化填充器写入，按升序作为默认排序字段。
     */
    @OrderBy(asc = true, sort = 1)
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private Instant createdAt;

    /**
     * 最近一次修改该记录的用户 ID；新增和更新时由持久化填充器维护。
     */
    @TableField(value = "updated_by", fill = FieldFill.INSERT_UPDATE)
    private UUID updatedBy;

    /**
     * 最近一次修改记录的时间；新增和更新时由持久化填充器维护。
     */
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private Instant updatedAt;

    /**
     * 软删除标识。null 表示未删除，非 null 表示已删除，其值为删除时间。
     */
    @TableField(value = "deleted")
    private Instant deleted;

    /**
     * 乐观锁版本号；每次成功更新由 MyBatis-Plus 按版本条件递增，用于阻止并发覆盖。
     */
    @Version
    @TableField(value = "version")
    private Long version;
}
