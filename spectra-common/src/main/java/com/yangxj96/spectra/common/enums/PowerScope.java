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
 * 权限范围
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Getter
@AllArgsConstructor
public enum PowerScope implements IEnum<Short> {

    GLOBAL((short) 0, "全局"),
    SELF((short) 1, "本级"),
    SELF_INCLUDE_CHILDREN((short) 2, "本级包含下级");

    private final short value;

    @JsonValue
    private final String desc;

    @Override
    public Short getValue() {
        return this.value;
    }
}
