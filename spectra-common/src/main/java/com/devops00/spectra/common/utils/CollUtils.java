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

package com.devops00.spectra.common.utils;


import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

/// 集合工具类
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/11/25 14:43
public final class CollUtils {

    private CollUtils() {
        // 工具类禁止实例化
    }

    /// 判断集合是否为 null 或 empty
    ///
    /// @param collection 可能为 null 的集合
    /// @return true 如果 collection == null 或 collection.isEmpty()
    public static boolean isEmpty(@Nullable Collection<?> collection) {
        return collection == null || collection.isEmpty();
    }

    /// 判断集合是否非 null 且非 empty
    ///
    /// @param collection 可能为 null 的集合
    /// @return true 如果 collection != null 且 !collection.isEmpty()
    public static boolean isNotEmpty(@Nullable Collection<?> collection) {
        return !isEmpty(collection);
    }

    /// 判断 Map 是否为 null 或 empty
    ///
    /// @param map 可能为 null 的 Map
    /// @return true 如果 map == null 或 map.isEmpty()
    public static boolean isEmpty(@Nullable Map<?, ?> map) {
        return map == null || map.isEmpty();
    }

    /// 判断 Map 是否非 null 且非 empty
    ///
    /// @param map 可能为 null 的 Map
    /// @return true 如果 map != null 且 !map.isEmpty()
    public static boolean isNotEmpty(@Nullable Map<?, ?> map) {
        return !isEmpty(map);
    }

    /// 安全获取集合大小（null 安全）
    ///
    /// @param collection 可能为 null 的集合
    /// @return 0 如果 collection 为 null，否则返回 collection.size()
    public static int size(@Nullable Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }

    /// 安全获取 Map 大小（null 安全）
    ///
    /// @param map 可能为 null 的 Map
    /// @return 0 如果 map 为 null，否则返回 map.size()
    public static int size(@Nullable Map<?, ?> map) {
        return map == null ? 0 : map.size();
    }

}
