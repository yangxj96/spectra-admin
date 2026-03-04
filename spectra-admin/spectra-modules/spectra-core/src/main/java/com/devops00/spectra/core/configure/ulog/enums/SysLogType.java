/*
 *  Copyright 2018-2025 yangxj96
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

package com.devops00.spectra.core.configure.ulog.enums;

import com.baomidou.mybatisplus.annotation.IEnum;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.AllArgsConstructor;
import lombok.Getter;

/// 日志类型
///
/// @author Jack Young
/// @version 1.0
/// @since 2025-11-11
@Getter
@AllArgsConstructor
public enum SysLogType implements IEnum<Integer> {

    /**
     * 常规日志,主要是接口调用相关
     */
    GENERAL(0, "常规日志"),

    /**
     * 安全日志,账号登录,登出,改密码,封号等
     */
    SAFETY(1, "安全日志"),

    /**
     * 系统出现异常的时候进行记录
     */
    SYSTEM_ERROR(2, "系统异常日志"),

    /**
     * 定时任务等自动化操作的日志
     */
    AUTOMATE(3, "自动化日志");

    private final Integer value;

    @JsonValue
    private final String desc;

    @Override
    public Integer getValue() {
        return this.value;
    }
}
