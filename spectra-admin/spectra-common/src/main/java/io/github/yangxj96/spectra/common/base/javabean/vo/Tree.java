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

package io.github.yangxj96.spectra.common.base.javabean.vo;

import jakarta.annotation.Nullable;

import java.util.List;

/**
 * 所有树形结构 VO 的通用接口
 *
 * @param <T> 具体类型
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
public interface Tree<T> {

    Long getId();

    void setId(Long id);

    @Nullable
    Long getPid();

    void setPid(@Nullable Long pid);

    @Nullable
    List<T> getChildren();

    void setChildren(@Nullable List<T> children);
}
