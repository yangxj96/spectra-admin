/*
 *  Copyright 2018-2025 yangxj96
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

package com.devops00.spectra.core.system.javabean.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/// 菜单VO
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuVO {

    /// 数据id.
    private UUID id;

    /// 父级ID
    private UUID pid;

    /// 图标
    private String icon;

    /// 名称
    private String name;

    /// 请求路径
    private String path;

    /// 组件路径,为空则使用布局组件
    private String component;

    /// 布局
    private String layout;

    /// 排序
    private Integer sort;

    /// 是否显示菜单
    private Boolean hide;

    /// 菜单元数据
    private Map<String, Object> metadata;
}
