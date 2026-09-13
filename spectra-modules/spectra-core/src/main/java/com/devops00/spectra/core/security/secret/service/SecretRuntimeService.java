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

package com.devops00.spectra.core.security.secret.service;

import com.devops00.spectra.common.exception.SecuritySecretUnavailableException;
import com.devops00.spectra.common.port.security.RuntimeSecret;

import java.util.Optional;

/**
 * 受控运行时密钥读取端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
public interface SecretRuntimeService {

    /**
     * 查找当前 ACTIVE 版本；状态无法确认时抛出 fail-closed 异常。
     *
     * @param code 已注册密钥编码。
     * @return 当前 ACTIVE 的运行时密钥；没有启用版本时返回空 Optional。
     */
    Optional<RuntimeSecret> findActive(String code);

    /**
     * 要求当前 ACTIVE 版本存在并可解密。
     *
     * @param code 已注册密钥编码。
     * @return 当前 ACTIVE 版本的密钥明文，仅供受控运行时调用方使用。
     */
    default String requireActiveValue(String code) {
        return findActive(code)
                .orElseThrow(() -> new SecuritySecretUnavailableException("密钥当前未启用: " + code, null))
                .value();
    }
}
