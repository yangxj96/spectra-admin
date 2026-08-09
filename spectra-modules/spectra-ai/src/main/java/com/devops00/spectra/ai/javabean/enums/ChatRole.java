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

package com.devops00.spectra.ai.javabean.enums;

import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 对话消息角色枚举
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/26
 */
public enum ChatRole {

    /**
     * 用户消息。
     */
    USER,

    /**
     * 助手消息。
     */
    ASSISTANT,

    /**
     * 系统消息。
     */
    SYSTEM;

    @JsonValue
    public String toValue() {
        return name().toLowerCase();
    }
}
