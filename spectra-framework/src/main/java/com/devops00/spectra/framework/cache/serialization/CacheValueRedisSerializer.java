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

package com.devops00.spectra.framework.cache.serialization;

import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;
import org.jspecify.annotations.Nullable;
import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

/**
 * 当前 framework 缓存值的显式 serializer。
 *
 * <p>缓存目前只保存部门名称 Map 和部门 UUID 集合，因此使用窄 envelope 保留这两种类型的语义。
 * 未识别的旧值或非法值按 cache miss 处理，不把 payload 交给项目类多态反序列化。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/8
 */
public final class CacheValueRedisSerializer implements RedisSerializer<Object> {

    private static final String TYPE = "type";

    private static final String VALUE = "value";

    private static final String UUID_STRING_MAP = "uuid-string-map";

    private static final String UUID_COLLECTION = "uuid-collection";

    private static final TypeReference<Map<String, Object>> ENVELOPE_TYPE = new TypeReference<>() {
    };

    private final ObjectMapper mapper;

    /** 创建使用独立 cache mapper 的 serializer。 */
    public CacheValueRedisSerializer(ObjectMapper mapper) {
        this.mapper = Objects.requireNonNull(mapper, "mapper").rebuild().build();
    }

    /**
     * 将对象序列化为 Redis 可存储的字节数组。
     *
     * @param value 待写入缓存的 UUID 到字符串 Map 或 UUID 集合；其他类型不属于当前缓存契约。
     * @return 返回可写入 Redis 的 JSON 字节数组；value 为 null 时返回 null，序列化失败时抛出 {@code SerializationException}。
     * @throws SerializationException 依赖不可用或输入不满足组件约束时抛出。
     */
    @Override
    public byte[] serialize(Object value) throws SerializationException {
        if (value == null) {
            return new byte[0];
        }
        Map<String, Object> envelope = new LinkedHashMap<>();
        if (value instanceof Map<?, ?> map) {
            envelope.put(TYPE, UUID_STRING_MAP);
            envelope.put(VALUE, encodeMap(map));
        } else if (value instanceof Collection<?> collection) {
            envelope.put(TYPE, UUID_COLLECTION);
            envelope.put(VALUE, encodeCollection(collection));
        } else {
            throw new SerializationException("不支持的缓存值类型: " + value.getClass().getName());
        }
        try {
            return mapper.writeValueAsBytes(envelope);
        } catch (Exception exception) {
            throw new SerializationException("缓存值序列化失败", exception);
        }
    }

    /**
     * 将 Redis 字节数组恢复为受控的业务对象。
     *
     * @param source 从 Redis 读取的缓存 JSON 字节数组。
     * @return 返回从受控 envelope 恢复的 UUID 到字符串 Map 或 UUID 列表；source 为 null、空数组、旧格式或损坏内容时返回 null 作为 cache miss，不把异常 payload 交给调用方。
     * @throws SerializationException 依赖不可用或输入不满足组件约束时抛出。
     */
    @Override
    public Object deserialize(byte[] source) throws SerializationException {
        if (source == null || source.length == 0) {
            return null;
        }
        try {
            Map<String, Object> envelope = mapper.readValue(source, ENVELOPE_TYPE);
            Object type = envelope.get(TYPE);
            Object value = envelope.get(VALUE);
            if (UUID_STRING_MAP.equals(type)) {
                return decodeMap(value);
            }
            if (UUID_COLLECTION.equals(type)) {
                return decodeCollection(value);
            }
            // 旧格式、未知版本和带 @class 的 payload 都自然失效为 cache miss。
            return null;
        } catch (Exception exception) {
            // 缓存不是安全事实源；损坏或迁移期旧值不阻塞业务请求，等待自然重建。
            return null;
        }
    }

    /**
     * 处理缓存值Redis相关数据。
     */
    private static Map<String, String> encodeMap(Map<?, ?> source) {
        Map<String, String> result = new LinkedHashMap<>();
        source.forEach((key, value) -> result.put(toUuid(key), toNullableText(value)));
        return result;
    }

    /**
     * 处理缓存值Redis相关数据。
     */
    private static List<String> encodeCollection(Collection<?> source) {
        List<String> result = new ArrayList<>(source.size());
        for (Object value : source) {
            result.add(toUuid(value));
        }
        return result;
    }

    /**
     * 处理缓存值Redis相关数据。
     */
    private static @Nullable Map<UUID, String> decodeMap(Object value) {
        if (!(value instanceof Map<?, ?> source)) {
            return null;
        }
        Map<UUID, String> result = new LinkedHashMap<>();
        for (Map.Entry<?, ?> entry : source.entrySet()) {
            result.put(UUID.fromString(String.valueOf(entry.getKey())), toNullableText(entry.getValue()));
        }
        return result;
    }

    /**
     * 处理缓存值Redis相关数据。
     */
    private static @Nullable List<UUID> decodeCollection(Object value) {
        if (!(value instanceof Collection<?> source)) {
            return null;
        }
        List<UUID> result = new ArrayList<>(source.size());
        for (Object item : source) {
            result.add(UUID.fromString(String.valueOf(item)));
        }
        return result;
    }

    /**
     * 转换UUID。
     */
    private static String toUuid(Object value) {
        if (!(value instanceof UUID uuid)) {
            throw new SerializationException("缓存值只允许 UUID 元素");
        }
        return uuid.toString();
    }

    /**
     * 转换缓存值Redis。
     */
    private static @Nullable String toNullableText(Object value) {
        if (value == null) {
            return null;
        }
        if (!(value instanceof String text)) {
            throw new SerializationException("缓存 Map 的值只允许字符串");
        }
        return text;
    }
}
