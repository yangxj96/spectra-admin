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

package com.devops00.spectra.core.system.javabean.from;

import com.devops00.spectra.common.base.Verify;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.groups.Default;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

/// 菜单保存接口
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
@Data
@AllArgsConstructor
@NoArgsConstructor
public class MenuSaveFrom {

    /// 数据id
    @NotNull(message = "ID不能为空", groups = Verify.Update.class)
    @Null(message = "新增时不能有ID存在", groups = Verify.Insert.class)
    private UUID id;

    /// 父级ID
    private UUID pid;

    /// 图标
    @NotNull(message = "图标不能为空", groups = {Verify.Insert.class, Verify.Update.class, Default.class})
    private String icon;

    /// 名称
    @NotBlank(message = "菜单名称不能为空", groups = {Verify.Insert.class, Verify.Update.class, Default.class})
    private String name;

    /// 请求路径
    @NotBlank(message = "请求路径不能为空", groups = {Verify.Insert.class, Verify.Update.class, Default.class})
    private String path;

    /// 组件路径,为空则使用布局组件
    private String component;

    /// 布局
    private String layout;

    /// 是否显示菜单
    private Boolean hide;

    /// 菜单元数据
    private Map<String, Object> metadata;

    /// 排序
    @NotNull(message = "排序不能为空", groups = {Verify.Insert.class, Verify.Update.class, Default.class})
    private Integer sort;
}
