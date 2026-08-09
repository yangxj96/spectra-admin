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

package com.devops00.spectra.common.constant;

import com.baomidou.mybatisplus.annotation.EnumValue;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.Getter;

/**
 * 数据范围类型
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/28 18:00
 */
@Getter
public enum DataScopeType {

    GLOBAL(0, "全局"),

    SELF(1, "本人"),

    DEPT(2, "部门"),

    DEPT_AND_CHILDREN(3, "部门及子部门"),

    CUSTOM(4, "自定义");

    @EnumValue
    @JsonValue
    private final Integer code;

    private final String desc;

    DataScopeType(Integer code, String desc) {
        this.code = code;
        this.desc = desc;
    }
}
