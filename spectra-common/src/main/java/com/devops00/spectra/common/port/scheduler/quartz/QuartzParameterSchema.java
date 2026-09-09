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

package com.devops00.spectra.common.port.scheduler.quartz;

import java.util.LinkedHashMap;
import java.util.Map;

/** Quartz Job 版本化 JSON 参数的字段和值类型声明。 */
public final class QuartzParameterSchema {

    /** 支持的 JSON 值类型。 */
    public enum ValueType {
        /** 字符串。 */
        STRING,
        /** JavaScript 安全范围内的整数。 */
        INTEGER,
        /** 长整数。 */
        LONG,
        /** 数值。 */
        NUMBER,
        /** 布尔值。 */
        BOOLEAN,
        /** JSON 对象。 */
        OBJECT,
        /** JSON 数组。 */
        ARRAY
    }

    /** 单个 JSON 字段定义。 */
    public record FieldDefinition(ValueType type, boolean required, boolean sensitive) {

        public FieldDefinition {
            if (type == null) {
                throw new IllegalArgumentException("参数字段类型不能为空");
            }
            if (sensitive) {
                throw new IllegalArgumentException("Quartz Job 参数不得声明敏感字段");
            }
        }
    }

    private final String version;
    private final Map<String, FieldDefinition> fields;
    private final boolean allowUnknownFields;

    /** 创建不可变参数 Schema。 */
    public QuartzParameterSchema(String version, Map<String, FieldDefinition> fields, boolean allowUnknownFields) {
        if (version == null || version.isBlank()) {
            throw new IllegalArgumentException("参数 Schema 版本不能为空");
        }
        var source = fields == null ? Map.<String, FieldDefinition>of() : fields;
        var copied = new LinkedHashMap<String, FieldDefinition>();
        source.forEach((name, definition) -> {
            if (name == null || name.isBlank() || definition == null) {
                throw new IllegalArgumentException("参数 Schema 字段定义不完整");
            }
            copied.put(name, definition);
        });
        this.version = version.trim();
        this.fields = Map.copyOf(copied);
        this.allowUnknownFields = allowUnknownFields;
    }

    /** 创建不接受业务参数的 v1 Schema。 */
    public static QuartzParameterSchema empty() {
        return new QuartzParameterSchema("1", Map.of(), false);
    }

    /**
     * 返回参数规则版本，执行历史会记录该版本用于解释参数摘要。
     *
     * @return 非空 Schema 版本
     */
    public String version() {
        return version;
    }

    /**
     * 返回允许提交的字段定义。
     *
     * @return 不可变字段定义映射
     */
    public Map<String, FieldDefinition> fields() {
        return fields;
    }

    /**
     * 返回未知字段是否可以通过参数校验。
     *
     * @return true 表示允许未声明字段，false 表示严格拒绝
     */
    public boolean allowUnknownFields() {
        return allowUnknownFields;
    }
}
