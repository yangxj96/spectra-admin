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

import java.util.Optional;

/**
 * 运行时密钥读取端口。实现必须在存储、解密或状态无法确认时 fail-closed。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/9/10
 */
@FunctionalInterface
public interface RuntimeSecretProvider {

    /**
     * 查询指定编码的当前启用密钥。
     *
     * @param code 已注册的密钥编码
     * @return 当前启用密钥；没有启用版本时返回空
     */
    Optional<RuntimeSecret> findActive(String code);
}
