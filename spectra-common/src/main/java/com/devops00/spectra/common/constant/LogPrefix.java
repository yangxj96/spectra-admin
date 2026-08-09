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

import org.jspecify.annotations.NullMarked;

/// 统一日志前缀定义
///
/// 用于规范不同模块日志输出格式
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/2/17 23:14
@NullMarked
public enum LogPrefix {

    CORE("核心"),
    SECURITY("安全"),
    STORAGE("文件存储"),
    SERIALIZATION("序列化"),
    KAPTCHA("验证码"),
    WEB("WEB"),
    PERSISTENCE("持久化"),
    REDIS("Redis"),
    CACHE(
            "缓存"),
    LOG("日志"),
    AI("AI");

    private final String value;

    LogPrefix(String value) {
        this.value = value;
    }

    /// 获取带中括号的标准前缀
    public String p() {
        return "[" + value + "] ";
    }

    /// 拼接日志内容
    public String f(String message) {
        return p() + message;
    }
}
