package com.devops00.spectra.framework.assembler;


import com.devops00.spectra.framework.assembler.converter.IdConverter;
import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.*;


/// NameFill 注解的执行器
///
/// 该组件负责在 VO 转换完成后，
/// 根据字段上的 {@link NameFill} 注解，
/// 批量完成「ID → Name」的查询与回填。
///
/// 执行时机建议：
/// * Controller 返回结果前
/// * Assembler / Converter 阶段
///
/// 设计特点：
/// * 基于反射 + 注解驱动
/// * 批量收集 ID，避免 N+1 查询
/// * Lookup 实现可自由接入缓存 / DB / RPC
///
/// 使用边界：
/// * 仅适用于 VO 列表填充
/// * 不适用于强实时一致性要求的业务字段
/// * 不保证顺序，仅保证语义正确
///
/// @author Jack Young
/// @version 1.0
/// @since 2026/2/2 16:26
@Component
public class NameFillExecutor {

    /// Spring 上下文，用于按类型获取 NameLookup 实现
    private final ApplicationContext applicationContext;

    public NameFillExecutor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    /// 对 VO 列表执行 NameFill 注解填充
    ///
    /// @param list 需要填充的lies
    /// @param <T>  ID类型
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

            // 获取 Lookup Bean
            NameLookup<?> lookup =
                    applicationContext.getBean(fillName.lookup());

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

    /// 统一 Map key 类型：
    /// 如果缓存导致 key 变成 String，则转回 ID 类型
    ///
    /// @param rawMap 行map
    /// @param lookup lookup
    /// @return 转换后的map
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

    /// 获取字段
    ///
    /// @param clazz     clz
    /// @param fieldName 字段名称
    private @NonNull Field getField(@NonNull Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(
                    "sourceField 不存在: " + fieldName, e
            );
        }
    }

    /// 读取字段值
    ///
    /// @param field  字段
    /// @param target 目标对象
    private Object getValue(@NonNull Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }
}
