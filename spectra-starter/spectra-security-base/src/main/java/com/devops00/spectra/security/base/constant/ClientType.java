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

package com.devops00.spectra.security.base.constant;


import lombok.Getter;

/// 客户端类型（登录端）
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/6/26
@Getter
public enum ClientType {
    /// Web浏览器
    WEB("web", "Web端"),
    /// 移动App
    APP("app", "App端"),
    /// 小程序
    MINI("mini", "小程序"),
    /// H5页面
    H5("h5", "H5端");

    private final String name;
    private final String description;

    ClientType(String name, String description) {
        this.name = name;
        this.description = description;
    }

    /// 根据名称解析，未匹配默认 WEB
    public static ClientType fromName(String name) {
        if (name == null || name.isBlank()) {
            return WEB;
        }
        for (var ct : values()) {
            if (ct.name.equalsIgnoreCase(name)) {
                return ct;
            }
        }
        return WEB;
    }
}
