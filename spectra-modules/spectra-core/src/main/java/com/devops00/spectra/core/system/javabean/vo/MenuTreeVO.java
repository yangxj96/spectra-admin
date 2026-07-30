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

package com.devops00.spectra.core.system.javabean.vo;

import com.devops00.spectra.common.base.javabean.vo.Tree;
import com.devops00.spectra.core.system.javabean.enums.MenuType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/// 菜单树形VO
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/14 00:00
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MenuTreeVO implements Tree<MenuTreeVO>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /// 数据id.
    private UUID id;

    /// 父级ID
    private UUID pid;

    /// 图标
    private String icon;

    /// 菜单节点类型
    private MenuType menuType;

    /// 对应前端命名路由
    private String routeName;

    /// 名称
    private String name;

    /// 排序
    private Integer sort;
    /// 子级
    private List<MenuTreeVO> children = new ArrayList<>();

}
