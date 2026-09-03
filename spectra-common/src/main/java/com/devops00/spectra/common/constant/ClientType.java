/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.common.constant;

import lombok.Getter;

/**
 * 登录客户端类型。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
@Getter
public enum ClientType {
    /** Web 浏览器。 */
    WEB("web", "Web端"),
    /** 移动 App。 */
    APP("app", "App端"),
    /** 小程序。 */
    MINI("mini", "小程序");

    private final String name;
    private final String description;

    ClientType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /**
     * 按客户端名称解析，未匹配时使用 Web。
     *
     * @param name 客户端名称
     * @return 客户端类型
     */
    public static ClientType fromName(String name) {
        if (name == null || name.isBlank()) {
            return WEB;
        }
        for (ClientType clientType : values()) {
            if (clientType.name.equalsIgnoreCase(name)) {
                return clientType;
            }
        }
        return WEB;
    }
}
