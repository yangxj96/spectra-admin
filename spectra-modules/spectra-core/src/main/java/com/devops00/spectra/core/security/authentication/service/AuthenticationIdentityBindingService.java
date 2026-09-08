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

import java.util.List;
import java.util.UUID;

/** 当前用户认证身份绑定用例。 */
public interface AuthenticationIdentityBindingService {

    /**
     * 查询或获取目标数据（{@code listByUserId}）。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @return 返回符合查询条件的认证身份列表；无匹配时返回空列表，不返回 null。
     */
    List<AuthenticationIdentity> listByUserId(UUID userId);

    /**
     * 更新或推进目标状态（{@code bindPhone}）。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @param phone  待验证或匹配的手机号码。
     * @param code   验证码或业务编码，用于匹配待处理记录。
     */
    void bindPhone(UUID userId, String phone, String code);

    /**
     * 更新或推进目标状态（{@code bindEmail}）。
     *
     * @param userId 目标用户的唯一标识，用于限定查询或变更范围。
     * @param email  待验证或匹配的电子邮箱地址。
     * @param code   验证码或业务编码，用于匹配待处理记录。
     */
    void bindEmail(UUID userId, String email, String code);

    /**
     * 更新或推进目标状态（{@code unbind}）。
     *
     * @param userId     目标用户的唯一标识，用于限定查询或变更范围。
     * @param identityId 目标认证身份的唯一标识。
     */
    void unbind(UUID userId, UUID identityId);
}
