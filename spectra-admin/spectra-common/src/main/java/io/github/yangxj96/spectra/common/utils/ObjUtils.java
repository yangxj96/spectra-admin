package io.github.yangxj96.spectra.common.utils;


import com.google.common.collect.Maps;
import org.jspecify.annotations.NullMarked;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Object 相关工具类
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/12/26 10:49
 */
@NullMarked
public final class ObjUtils {

    private ObjUtils() {
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
                throw new IllegalArgumentException(
                        "Map 转换失败, key=" + entry.getKey()
                                + ", value=" + entry.getValue(),
                        e
                );
            }
        }
        return result;
    }

    /**
     * object转list map
     *
     * @param obj    对象
     * @param kCalzz k类型
     * @param vCalzz v类型
     * @param <K>    kclass
     * @param <V>    vclass
     * @return 转换后的List Map
     */
    public static <K, V> List<Map<K, V>> castListMap(Object obj, Class<K> kCalzz, Class<V> vCalzz) {
        if (!(obj instanceof List<?> list)) {
            return Collections.emptyList();
        }
        List<Map<K, V>> result = new ArrayList<>(list.size());
        for (Object item : list) {
            Map<K, V> map = castMap(item, kCalzz, vCalzz);
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
     * @return {@link Map } key为string,value为object
     */
    public static Map<String, Object> castStrObjMap(Object obj) {
        return castMap(obj, String.class, Object.class);
    }


}
