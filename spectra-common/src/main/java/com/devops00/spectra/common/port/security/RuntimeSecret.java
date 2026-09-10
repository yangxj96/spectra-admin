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

package com.devops00.spectra.common.port.security;

import java.util.Objects;

/**
 * 运行时密钥的最小公共值对象，不包含管理元数据或持久化字段。
 *
 * @param code        已注册的密钥编码
 * @param version     当前生效版本
 * @param value       运行时明文；只允许在后端可信调用链中短暂存在
 * @param fingerprint 密钥内容指纹，不可用于还原明文
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/10
 */
public record RuntimeSecret(String code, int version, String value, String fingerprint) {

    /**
     * 校验运行时密钥字段。
     */
    public RuntimeSecret {
        Objects.requireNonNull(code, "密钥编码不能为空");
        Objects.requireNonNull(value, "运行时密钥值不能为空");
        Objects.requireNonNull(fingerprint, "密钥指纹不能为空");
        if (code.isBlank() || value.isBlank() || fingerprint.isBlank()) {
            throw new IllegalArgumentException("运行时密钥字段不能为空");
        }
        if (version < 1) {
            throw new IllegalArgumentException("密钥版本必须大于 0");
        }
    }
}
