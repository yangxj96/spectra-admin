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

package com.devops00.spectra.security.base.holder;

import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

/**
 * 从当前身份源重新加载安全主体。
 *
 * <p>Refresh Token 只负责证明会话连续性，不能用 Redis 中的旧 SecurityUser 快照恢复权限。
 * 具体业务模块通过该端口重新读取账号、用户、角色和权限；没有实现时上层必须拒绝刷新。</p>
 */
@NullMarked
@FunctionalInterface
public interface SecurityUserLoader {

    /**
     * 按用户 ID 加载当前安全主体。
     *
     * @param userId 用户 ID
     * @return 最新安全主体；不存在、禁用或无法验证时返回 {@code null}
     */
    @Nullable
    SecurityUser load(UUID userId);
}
