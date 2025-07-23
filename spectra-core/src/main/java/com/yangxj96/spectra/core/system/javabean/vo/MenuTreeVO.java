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

package com.yangxj96.spectra.core.system.javabean.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;
import com.yangxj96.spectra.common.base.javabean.vo.Tree;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 菜单树形VO
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuTreeVO implements Tree<MenuTreeVO>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 数据id.
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long id;

    /**
     * 父级ID
     */
    @JsonSerialize(using = ToStringSerializer.class)
    private Long pid;

    /**
     * 图标
     */
    private String icon;

    /**
     * 名称
     */
    private String name;

    /**
     * 请求路径
     */
    private String path;

    /**
     * 组件路径,为空则使用布局组件
     */
    private String component;

    /**
     * 布局
     */
    private String layout;

    /**
     * 排序
     */
    private Integer sort;

    /**
     * 子级
     */
    private List<MenuTreeVO> children = new ArrayList<>();

}
