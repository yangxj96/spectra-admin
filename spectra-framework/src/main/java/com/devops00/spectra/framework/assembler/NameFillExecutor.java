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

package com.devops00.spectra.framework.assembler;

import com.devops00.spectra.framework.assembler.converter.IdConverter;
import org.jspecify.annotations.NonNull;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * NameFill 注解的执行器
 * <p>
 * 该组件负责在 VO 转换完成后，
 * 根据字段上的 {@link NameFill} 注解，
 * 批量完成「ID → Name」的查询与回填。
 * <p>
 * 执行时机建议：
 * <ul>
 * <li>Controller 返回结果前</li>
 * <li>Assembler / Converter 阶段</li>
 * </ul>
 * <p>
 * 设计特点：
 * <ul>
 * <li>基于反射 + 注解驱动</li>
 * <li>批量收集 ID，避免 N+1 查询</li>
 * <li>Lookup 实现可自由接入缓存 / DB / RPC</li>
 * </ul>
 * <p>
 * 使用边界：
 * <ul>
 * <li>仅适用于 VO 列表填充</li>
 * <li>不适用于强实时一致性要求的业务字段</li>
 * <li>不保证顺序，仅保证语义正确</li>
 * </ul>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/2 16:26
 */
@Component
public class NameFillExecutor {

    private final NameLookupRegistry lookupRegistry;

    /**
     * 创建名称填充执行器。
     *
     * @param lookupRegistry 按 Lookup 实现类型解析名称查询 Bean 的注册表
     */
    public NameFillExecutor(NameLookupRegistry lookupRegistry) {
        this.lookupRegistry = lookupRegistry;
    }

    /**
     * 对 VO 列表执行 NameFill 注解填充
     *
     * @param list 需要根据其中 ID 字段批量补充展示名称的 VO 列表；null 或空列表不会触发查询
     * @param <T>  VO 类型
     * @throws IllegalAccessException 无法访问需要填充的实体字段时抛出。
     */
    public <T> void fill(List<T> list) throws IllegalAccessException {
        if (list == null || list.isEmpty()) {
            return;
        }

        // 获取 VO 类
        Class<?> voClass = list.getFirst().getClass();

        // 遍历字段
        for (Field targetField : voClass.getDeclaredFields()) {

            NameFill fillName = targetField.getAnnotation(NameFill.class);
            if (fillName == null) {
                continue;
            }

            // source 字段
            Field sourceField = getField(voClass, fillName.sourceField());

            targetField.setAccessible(true);
            sourceField.setAccessible(true);

            // 按注解声明的实现类型取得 Lookup Bean
            NameLookup<?> lookup = lookupRegistry.require(fillName.lookup());

            Class<?> idType = lookup.idType();

            // 收集 ID
            Set<Object> ids = new HashSet<>();
            for (T vo : list) {
                Object id = getValue(sourceField, vo);
                if (idType.isInstance(id)) {
                    ids.add(id);
                }
            }

            if (ids.isEmpty()) {
                continue;
            }

            // 查询 nameMap
            @SuppressWarnings("unchecked")
            Map<Object, String> rawMap = ((NameLookup<Object>) lookup).getNameMap(ids);

            if (rawMap == null || rawMap.isEmpty()) {
                continue;
            }

            // 关键：统一 key 类型（兼容 Redis String key）
            Map<Object, String> nameMap = normalizeKeyType(rawMap, lookup);

            // 回填
            for (T vo : list) {
                Object id = getValue(sourceField, vo);
                if (id != null) {
                    targetField.set(vo, nameMap.get(id));
                }
            }
        }
    }

    /**
     * 统一 Map key 类型：
     * 如果缓存导致 key 变成 String，则转回 ID 类型
     *
     * @param rawMap Lookup 返回的名称映射；key 可能是 ID 类型或缓存序列化后的 String
     * @param lookup 提供目标 ID 类型转换器的名称查询 Bean
     * @return 已将 String key 转换回 ID 类型的名称映射；原映射使用 ID key 时直接返回原映射
     */
    @SuppressWarnings("unchecked")
    private Map<Object, String> normalizeKeyType(Map<Object, String> rawMap, NameLookup<?> lookup) {

        // 判断 key 类型
        Object firstKey = rawMap.keySet().iterator().next();

        // 如果本来就是正确类型，直接返回
        if (!(firstKey instanceof String)) {
            return rawMap;
        }

        // 使用 converter 转换回 ID
        IdConverter<Object> converter = (IdConverter<Object>) lookup.idConverter();

        Map<Object, String> result = new HashMap<>(rawMap.size());

        for (var entry : rawMap.entrySet()) {
            String key = (String) entry.getKey();
            Object realKey = converter.fromString(key);
            result.put(realKey, entry.getValue());
        }

        return result;
    }

    /**
     * 获取字段
     *
     * @param clazz     需要读取源字段的 VO 类型
     * @param fieldName 注解声明的源 ID 字段名称
     * @return VO 类型中声明的源字段
     * @throws IllegalStateException 当 VO 中不存在指定源字段时抛出
     */
    private @NonNull Field getField(@NonNull Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException("sourceField 不存在: " + fieldName, e);
        }
    }

    /**
     * 读取字段值
     *
     * @param field  需要读取值的源字段反射对象
     * @param target 当前正在组装、且包含源字段值的 VO 实例
     * @return 源字段当前保存的 ID 值；字段值为 null 时返回 null
     * @throws IllegalStateException 当字段无法访问时抛出
     */
    private Object getValue(@NonNull Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
