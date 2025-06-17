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

package com.yangxj96.spectra.core.javabean.entity;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.yangxj96.spectra.common.base.BaseEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;
import lombok.experimental.SuperBuilder;

import java.io.Serial;
import java.io.Serializable;

/**
 * 菜单表
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Data
@EqualsAndHashCode(callSuper = true)
@Accessors(chain = true)
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "db_system.t_menu")
public class Menu extends BaseEntity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;


    /**
     * 父级ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    @TableField(value = "pid")
    private Long pid;

    /**
     * 图标
     */
    @TableField(value = "icon")
    private String icon;

    /**
     * 名称
     */
    @TableField(value = "\"name\"")
    private String name;

    /**
     * 请求路径
     */
    @TableField(value = "\"path\"")
    private String path;

    /**
     * 组件路径,为空则使用布局组件
     */
    @TableField(value = "component")
    private String component;

    /**
     * 布局
     */
    @TableField(value = "layout")
    private String layout;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;
}
