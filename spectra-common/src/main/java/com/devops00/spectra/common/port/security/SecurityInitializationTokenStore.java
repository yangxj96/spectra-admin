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

package com.devops00.spectra.common.port.security;

import java.util.Optional;

/**
 * 系统初始化 Token 存储端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
public interface SecurityInitializationTokenStore {

    /** 仅在当前不存在初始化 Token 时写入摘要。 */
    boolean putIfAbsent(String digest);

    /** 读取当前初始化 Token 摘要。 */
    Optional<String> getDigest();

    /** 清理初始化 Token。 */
    void clear();
}
