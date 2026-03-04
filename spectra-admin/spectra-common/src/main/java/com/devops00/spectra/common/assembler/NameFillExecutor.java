package com.devops00.spectra.common.assembler;


import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


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
    /// 该方法会：
    /// 1. 扫描 VO 类中所有标注 {@link NameFill} 的字段</li>
    /// 2. 从 sourceField 中批量提取 ID</li>
    /// 3. 调用对应的 {@link NameLookup} 查询 name 映射</li>
    /// 4. 将查询结果回填到目标字段</li>
    ///
    /// @param list VO 列表（必须为同一类型）
    /// @param <T>  VO 类型
    /// @throws IllegalAccessException 反射访问异常（理论上不会发生）
    public <T> void fill(List<T> list) throws IllegalAccessException {
        if (list == null || list.isEmpty()) {
            return;
        }
        // 获取vo类
        Class<?> voClass = list.getFirst().getClass();
        // 字段遍历
        for (Field targetField : voClass.getDeclaredFields()) {
            // 如果没获取到注解则跳过
            NameFill fillName = targetField.getAnnotation(NameFill.class);
            if (fillName == null) {
                continue;
            }
            // 获取注解的值
            Field sourceField = getField(voClass, fillName.sourceField());

            targetField.setAccessible(true);
            sourceField.setAccessible(true);

            // 1. 获取 Lookup Bean
            NameLookup<?> lookup =
                    applicationContext.getBean(fillName.lookup());

            Class<?> idType = lookup.idType();

            // 2. 收集 ID（类型安全）
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

            // 3. 查询 nameMap（语义安全的 cast）
            @SuppressWarnings("unchecked")
            Map<Object, String> nameMap =
                    ((NameLookup<Object>) lookup).getNameMap(ids);

            // 4. 回填
            for (T vo : list) {
                Object id = getValue(sourceField, vo);
                if (id != null) {
                    targetField.set(vo, nameMap.get(id));
                }
            }
        }
    }

    /// 根据字段名获取 VO 中声明的字段
    ///
    /// @param clazz     VO 类
    /// @param fieldName 字段名
    /// @return Field 对象
    /// @throws IllegalStateException 当字段不存在时抛出
    private @NonNull Field getField(@NonNull Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(
                    "sourceField 不存在: " + fieldName, e
            );
        }
    }

    /// 从指定对象中读取字段值
    ///
    /// @param field  目标字段
    /// @param target 对象实例
    /// @return 字段值
    private Object getValue(@NonNull Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

}
