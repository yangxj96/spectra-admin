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

package com.devops00.spectra.common.security.authorization;

import java.util.UUID;

/**
 * 认证请求使用的授权快照读取端口。
 *
 * <p>基础安全包只依赖这个窄端口，不依赖具体 JDBC/Redis 实现；业务模块负责提供
 * 当前用户的 Assignment-preserving 快照。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@FunctionalInterface
public interface AuthorizationSnapshotProvider {

    /**
     * 读取用户当前有效授权快照。读取失败必须由实现抛出异常，调用方不得降级为放行。
     */
    AuthorizationSnapshot load(UUID userId);
}
