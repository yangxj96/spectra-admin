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

import com.google.common.collect.Maps;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Object 相关工具类
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/26 10:49
 */
@NullMarked
public final class ObjUtils {

    private ObjUtils() {
        // 工具类禁止实例化
    }

    /**
     * obj转list
     *
     * @param obj   obj对象
     * @param clazz 具体类型
     * @param <T>   具体类型
     * @return 转换后的结果
     */
    public static <T> List<T> castList(Object obj, Class<T> clazz) {
        if (!(obj instanceof List<?> list)) {
            return Collections.emptyList();
        }

        List<T> result = new ArrayList<>(list.size());
        for (Object o : list) {
            result.add(clazz.cast(o));
        }
        return result;
    }

    /**
     * Object → Map<K, V>
     *
     * @param obj        对象
     * @param keyClass   key类型
     * @param valueClass value类型
     * @param <K>        key类型
     * @param <V>        value类型
     * @return 转换后的map
     */
    public static <K, V> Map<K, V> castMap(Object obj, Class<K> keyClass, Class<V> valueClass) {
        if (!(obj instanceof Map<?, ?> rawMap)) {
            return Collections.emptyMap();
        }

        Map<K, V> result = Maps.newHashMapWithExpectedSize(rawMap.size());
        for (Map.Entry<?, ?> entry : rawMap.entrySet()) {
            try {
                K key = keyClass.cast(entry.getKey());
                V value = valueClass.cast(entry.getValue());
                result.put(key, value);
            } catch (ClassCastException e) {
                throw new IllegalArgumentException("Map 转换失败, key=" + entry.getKey() + ", value=" + entry.getValue(), e);
            }
        }
        return result;
    }

    /**
     * object转list map
     *
     * @param obj    对象
     * @param kClazz k类型
     * @param vClazz v类型
     * @param <K>    class
     * @param <V>    class
     * @return 转换后的List Map
     */
    public static <K, V> List<Map<K, V>> castListMap(Object obj, Class<K> kClazz, Class<V> vClazz) {
        if (!(obj instanceof List<?> list)) {
            return Collections.emptyList();
        }
        List<Map<K, V>> result = new ArrayList<>(list.size());
        for (Object item : list) {
            Map<K, V> map = castMap(item, kClazz, vClazz);
            if (!map.isEmpty()) {
                result.add(map);
            }
        }
        return result;
    }

    /**
     * Object 转换为 Map String Object
     *
     * @param obj 对象
     * @return {@code Map<String,Object} key为string,value为object
     */
    public static Map<String, Object> castStrObjMap(Object obj) {
        return castMap(obj, String.class, Object.class);
    }
}
