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

package com.devops00.spectra.security.base.root;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;

/**
 * 统一 Root 判定策略。
 * <p>
 * 该策略只负责识别系统 Root，不代表跳过审计、Session 或数据边界检查。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@FunctionalInterface
public interface RootAuthorizationPolicy {

    String ROOT_ROLE = "ROLE_DEV_OPS";

    /**
     * 判断主体是否为 Root。
     */
    boolean isRoot(Authentication authentication);

    /**
     * 要求当前主体为 Root。
     */
    default void requireRoot(Authentication authentication) {
        if (!isRoot(authentication)) {
            throw new AccessDeniedException("需要 DEV_OPS Root 权限");
        }
    }
}
