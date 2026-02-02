package io.github.yangxj96.spectra.core.configure.assembler;


import org.jspecify.annotations.NonNull;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 过滤器
 *
 * @author Jack Young
 * @version 1.0
 * @since 2026/2/2 16:26
 */
@Component
public class NameFillExecutor {

    private final ApplicationContext applicationContext;

    public NameFillExecutor(ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }


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

    private @NonNull Field getField(@NonNull Class<?> clazz, String fieldName) {
        try {
            return clazz.getDeclaredField(fieldName);
        } catch (NoSuchFieldException e) {
            throw new IllegalStateException(
                    "sourceField 不存在: " + fieldName, e
            );
        }
    }

    private Object getValue(@NonNull Field field, Object target) {
        try {
            return field.get(target);
        } catch (IllegalAccessException e) {
            throw new IllegalStateException(e);
        }
    }

}
