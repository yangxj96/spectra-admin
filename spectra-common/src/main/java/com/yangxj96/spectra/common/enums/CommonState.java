/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.common.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * <p>
 * 通用状态
 * </p>
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/18
 */
@Getter
@AllArgsConstructor
public enum CommonState implements IEnum<Short> {

    /**
     * 启用
     */
    ENABLE((short) 0, "启用"),

    /**
     * 禁用
     */
    DISABLE((short) 1, "禁用");

    private final short value;

    @JsonValue
    private final String desc;

    @Override
    public Short getValue() {
        return this.value;
    }
}
