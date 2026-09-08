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

package com.devops00.spectra.core.security.authentication.service;

import com.devops00.spectra.core.security.authentication.javabean.entity.PasswordCredential;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.util.UUID;

public interface PasswordCredentialService {

    /**
     * 查询或获取目标数据（{@code getByUserId}）。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @return 返回符合条件的密码凭据；未找到匹配记录时返回 null。
     */
    @Nullable
    PasswordCredential getByUserId(UUID userId);

    /**
     * 创建或构建目标数据（{@code createOrReplace}）。
     *
     * @param userId       目标用户的唯一标识，用于限定查询或变更范围。
     * @param passwordHash 已按安全策略生成的密码哈希，不包含明文密码。
     * @param mustChange   是否要求用户下次登录时修改密码。
     */
    void createOrReplace(UUID userId, String passwordHash, boolean mustChange);

    /**
     * 更新或推进目标状态（{@code updatePassword}）。
     *
     * @param userId       目标用户的唯一标识，用于限定查询或变更范围。
     * @param passwordHash 已按安全策略生成的密码哈希，不包含明文密码。
     * @param mustChange   是否要求用户下次登录时修改密码。
     * @param expiresAt    新密码凭据的失效时间；为 null 表示凭据不设置独立失效时间。
     */
    void updatePassword(UUID userId, String passwordHash, boolean mustChange, @Nullable Instant expiresAt);
}
