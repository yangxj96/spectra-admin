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

import java.io.Serial;
import java.util.UUID;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import com.devops00.spectra.common.base.BaseEntity;
import com.devops00.spectra.core.system.javabean.enums.MenuType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * 菜单表
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/6/14 00:00
 */
@Data
@EqualsAndHashCode(callSuper = true)
@AllArgsConstructor
@NoArgsConstructor
@TableName(value = "sys_menu", schema = "spectra_core")
public class Menu extends BaseEntity {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 父级ID
     */
    @TableField(value = "pid")
    private UUID pid;

    /**
     * 图标
     */
    @TableField(value = "icon")
    private String icon;

    /**
     * 菜单节点类型
     */
    @TableField(value = "menu_type")
    private MenuType menuType;

    /**
     * 对应前端命名路由
     */
    @TableField(value = "route_name")
    private String routeName;

    /**
     * 名称
     */
    @TableField(value = "name")
    private String name;

    /**
     * 排序
     */
    @TableField(value = "sort")
    private Integer sort;
}
