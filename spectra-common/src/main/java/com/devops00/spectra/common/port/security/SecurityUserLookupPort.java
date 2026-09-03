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

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * 按 opaque Token 查询认证主体的端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
@NullMarked
public interface SecurityUserLookupPort {

    /** 查询 Token 对应的主体；无效或过期 Token 返回 {@code null}。 */
    @Nullable
    SecurityPrincipal findByToken(String token);
}
