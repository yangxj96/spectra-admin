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

package com.devops00.spectra.core.quartz.parameter;

import com.devops00.spectra.common.port.quartz.QuartzParameterSchema;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;

/** 校验并规范化 Quartz Job 的版本化 JSON 参数，阻断未知字段和凭据输入。 */
@Component
@RequiredArgsConstructor
public class QuartzJobParameterValidator {

    private final ObjectMapper objectMapper;

    /**
     * 校验参数 JSON 并返回只能保存非敏感字段的不可变快照。
     *
     * @param schema Job 声明的参数 schema
     * @param json   请求提供的 JSON 对象
     * @return 已校验参数快照；不会返回 null
     */
    public VersionedJsonJobParameters validate(QuartzParameterSchema schema, String json) {
        if (schema == null || json == null || json.isBlank()) {
            throw new IllegalArgumentException("Quartz Job 参数和 schema 不能为空");
        }
        final JsonNode root;
        try {
            root = objectMapper.readTree(json);
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Quartz Job 参数必须是合法 JSON", exception);
        }
        if (root == null || !root.isObject()) {
            throw new IllegalArgumentException("Quartz Job 参数必须是单个 JSON 对象");
        }
        var version = root.get("version");
        if (version == null || !version.isTextual() || !schema.version().equals(version.asString())) {
            throw new IllegalArgumentException("Quartz Job 参数 schema 版本不匹配");
        }
        var values = new LinkedHashMap<String, Object>();
        for (Map.Entry<String, JsonNode> field : root.properties()) {
            var name = field.getKey();
            if ("version".equals(name)) {
                continue;
            }
            var definition = schema.fields().get(name);
            if (looksSensitive(name) || definition != null && definition.sensitive()) {
                throw new IllegalArgumentException("Quartz Job 参数包含敏感字段: " + name);
            }
            if (definition == null && !schema.allowUnknownFields()) {
                throw new IllegalArgumentException("Quartz Job 参数包含未声明字段: " + name);
            }
            if (definition != null && !matches(definition.type(), field.getValue())) {
                throw new IllegalArgumentException("Quartz Job 参数字段类型不匹配: " + name);
            }
            values.put(name, objectMapper.convertValue(field.getValue(), Object.class));
        }
        schema.fields().forEach((name, definition) -> {
            if (definition.required() && !values.containsKey(name)) {
                throw new IllegalArgumentException("Quartz Job 缺少必填参数: " + name);
            }
        });
        try {
            var persisted = new LinkedHashMap<String, Object>();
            persisted.put("version", schema.version());
            persisted.putAll(values);
            return new VersionedJsonJobParameters(schema.version(), values, objectMapper.writeValueAsString(persisted));
        } catch (RuntimeException exception) {
            throw new IllegalArgumentException("Quartz Job 参数规范化失败", exception);
        }
    }

    /** 判断字段名是否属于凭据或密钥语义。 */
    private boolean looksSensitive(String name) {
        var normalized = name.toLowerCase(Locale.ROOT);
        return normalized.contains("password")
                || normalized.contains("passwd")
                || normalized.contains("token")
                || normalized.contains("secret")
                || normalized.contains("credential")
                || normalized.contains("privatekey")
                || normalized.contains("accesskey")
                || normalized.contains("apikey")
                || normalized.contains("authorization");
    }

    /** 判断 JSON 节点是否匹配 schema 声明的封闭类型。 */
    private boolean matches(QuartzParameterSchema.ValueType type, JsonNode node) {
        return switch (type) {
            case STRING -> node.isTextual();
            case INTEGER, LONG -> node.isIntegralNumber();
            case NUMBER -> node.isNumber();
            case BOOLEAN -> node.isBoolean();
            case OBJECT -> node.isObject();
            case ARRAY -> node.isArray();
        };
    }
}
