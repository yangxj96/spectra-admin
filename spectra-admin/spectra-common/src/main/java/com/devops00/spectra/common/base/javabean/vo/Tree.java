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

package com.devops00.spectra.common.base.javabean.vo;


import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

/// 所有树形结构 VO 的通用接口
///
/// @param <T> 具体类型
/// @author Jack Young
/// @version 1.0
/// @since 2025-6-14
public interface Tree<T> {

    /// 获取ID
    UUID getId();

    /// 设置ID
    void setId(UUID id);

    /// 获取父级ID
    @Nullable
    UUID getPid();

    /// 设置父级ID
    void setPid(@Nullable UUID pid);

    /// 获取下级
    @Nullable
    List<T> getChildren();

    /// 设置下级
    void setChildren(@Nullable List<T> children);
}
