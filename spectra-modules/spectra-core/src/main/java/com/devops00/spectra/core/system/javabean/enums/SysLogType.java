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

package com.devops00.spectra.core.system.javabean.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * Core 操作日志类型。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Getter
@AllArgsConstructor
public enum SysLogType implements IEnum<Integer> {

    /** 常规日志，主要是接口调用相关。 */
    GENERAL(0, "常规日志"),

    /** 安全日志；安全事实实际写入独立安全审计表。 */
    SAFETY(1, "安全日志"),

    /** 系统异常日志。 */
    SYSTEM_ERROR(2, "系统异常日志"),

    /** 定时任务等自动化操作日志。 */
    AUTOMATE(3, "自动化日志");

    private final Integer value;

    @JsonValue
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }
}
