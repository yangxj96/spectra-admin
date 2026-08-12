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
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.jspecify.annotations.Nullable;

import java.io.Serial;
import java.util.UUID;

/**
 * 字典-字典类型
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/18 00:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_dict_group", schema = "spectra_core")
public class DictGroup extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 父级ID
     */
    @Nullable
    @TableField(value = "pid")
    private UUID pid;

    /**
     * 字典名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 字典编码
     */
    @TableField(value = "code")
    private String code;

    /**
     * 字典状态
     */
    @TableField(value = "state")
    private Boolean state;

    /**
     * 备注
     */
    @TableField(value = "remark")
    private String remark;

    /**
     * 是否内置字段,为true则不允许他进行修改删除操作
     */
    @TableField(value = "builtin")
    private Boolean builtin;

    /**
     * 是否隐藏,为true则前端不可见
     */
    @TableField(value = "hide")
    private Boolean hide;
}
