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

package com.devops00.spectra.framework.assembler.converter;

import java.util.UUID;

/**
 * UUID作为主键的转换器
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/4/20 11:41
 */
public class UuidConverter implements IdConverter<UUID> {

    /**
     * 将业务标识转换为字符串表示。
     *
     * @param id 待转换的业务标识。
     * @return 返回 UUID 业务标识的标准字符串；id 为 null 时返回 null，不返回空字符串。
     */
    @Override
    public String toString(UUID id) {
        return id == null ? null : id.toString();
    }

    /**
     * 将字符串解析为业务标识。
     *
     * @param value 待转换为 UUID 业务标识的标准 UUID 文本。
     * @return 返回字符串解析出的 UUID 业务标识；value 为 null 时返回 null，格式非法时抛出 IllegalArgumentException。
     */
    @Override
    public UUID fromString(String value) {
        return value == null ? null : UUID.fromString(value);
    }
}
