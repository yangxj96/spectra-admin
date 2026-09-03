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

package com.devops00.spectra.common.security.authorization;

import com.devops00.spectra.common.port.security.SecurityPrincipal;
import org.jspecify.annotations.Nullable;

/**
 * 统一 Root 判定策略。
 *
 * <p>该契约只识别系统 Root，不代表跳过审计、Session 或数据边界检查。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
@FunctionalInterface
public interface RootAuthorizationPolicy {

    /** 系统 Root 角色编码。 */
    String ROOT_ROLE = "ROLE_DEV_OPS";

    /** 判断安全主体是否为 Root。 */
    boolean isRoot(@Nullable SecurityPrincipal principal);
}
