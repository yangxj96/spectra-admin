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

import com.devops00.spectra.core.security.authentication.javabean.entity.AuthenticationIdentity;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public interface AuthenticationIdentityService {

    /**
     * 查询或获取目标数据（{@code findPasswordIdentity}）。
     *
     * @param identifier 待匹配的认证标识，例如用户名、手机号或邮箱。
     * @return 返回符合条件的认证身份；未找到匹配记录时返回 null。
     */
    @Nullable
    AuthenticationIdentity findPasswordIdentity(String identifier);

    /**
     * 按目标认证方式查找当前有效身份，业务认证不得回退到旧 Account 表。
     *
     * @param methodCode 认证方式编码，用于选择身份校验策略。
     * @param identifier 待匹配的认证标识，例如用户名、手机号或邮箱。
     * @return 返回符合条件的认证身份；未找到匹配记录时返回 null。
     */
    @Nullable
    AuthenticationIdentity findIdentity(String methodCode, String identifier);

    /**
     * 创建或构建目标数据（{@code createPasswordIdentity}）。
     *
     * @param userId     目标用户的唯一标识，用于限定查询或变更范围。
     * @param identifier 待匹配的认证标识，例如用户名、手机号或邮箱。
     * @return 返回保存后的认证身份；校验或写入失败时抛出业务异常，不返回 null。
     */
    AuthenticationIdentity createPasswordIdentity(UUID userId, String identifier);

    /**
     * 创建或重新激活非密码认证身份。
     *
     * @param userId     目标用户的唯一标识，用于限定查询或变更范围。
     * @param methodCode 认证方式编码，用于选择身份校验策略。
     * @param identifier 待匹配的认证标识，例如用户名、手机号或邮箱。
     * @return 返回保存后的认证身份；校验或写入失败时抛出业务异常，不返回 null。
     */
    AuthenticationIdentity createIdentity(UUID userId, String methodCode, String identifier);

    /**
     * 更新或推进目标状态（{@code updatePasswordIdentifier}）。
     *
     * @param userId     目标用户的唯一标识，用于限定查询或变更范围。
     * @param identifier 待匹配的认证标识，例如用户名、手机号或邮箱。
     */
    void updatePasswordIdentifier(UUID userId, String identifier);

    /**
     * 更新或推进目标状态（{@code revokeByUserId}）。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     */
    void revokeByUserId(UUID userId);

    /**
     * 撤销指定认证方式的所有身份。
     *
     * @param userId     目标用户的唯一标识，用于限定查询或变更范围。
     * @param methodCode 认证方式编码，用于选择身份校验策略。
     */
    void revokeByUserIdAndMethod(UUID userId, String methodCode);

    /**
     * 返回用户当前有效的目标认证身份。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @return 返回符合查询条件的认证身份列表；无匹配时返回空列表，不返回 null。
     */
    List<AuthenticationIdentity> listByUserId(UUID userId);

    /**
     * 仅撤销归属于指定用户的单个目标认证身份。
     *
     * @param userId     目标用户的唯一标识，用于限定查询或变更范围。
     * @param identityId 目标认证身份的唯一标识。
     */
    void revokeByUserIdAndId(UUID userId, UUID identityId);
}
