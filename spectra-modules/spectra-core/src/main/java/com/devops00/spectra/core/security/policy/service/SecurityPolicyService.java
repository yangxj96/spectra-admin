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

package com.devops00.spectra.core.security.policy.service;

import com.devops00.spectra.core.security.policy.javabean.from.SecurityPasswordPolicyFrom;
import com.devops00.spectra.core.security.policy.javabean.from.SecuritySessionPolicyFrom;
import com.devops00.spectra.core.security.policy.javabean.vo.SecurityPasswordPolicyVO;
import com.devops00.spectra.core.security.policy.javabean.vo.SecuritySessionPolicyVO;

import java.util.List;
import java.util.UUID;

/** 安全策略查询与受审计修改服务。 */
public interface SecurityPolicyService {

    /**
     * 查询或获取目标数据（{@code sessionPolicies}）。
     *
     * @return 返回符合查询条件的会话策略列表；无匹配时返回空列表，不返回 null。
     */
    List<SecuritySessionPolicyVO> sessionPolicies();

    /**
     * 更新或推进目标状态（{@code modifySessionPolicy}）。
     *
     * @param clientId 要修改会话策略的客户端唯一标识。
     * @param from     会话有效期、并发会话数和刷新策略等客户端安全配置字段。
     * @return 返回保存后的会话策略；校验或写入失败时抛出业务异常，不返回 null。
     */
    SecuritySessionPolicyVO modifySessionPolicy(UUID clientId, SecuritySessionPolicyFrom from);

    /**
     * 查询或获取目标数据（{@code passwordPolicy}）。
     *
     * @return 返回密码策略；处理失败时抛出业务异常，不返回 null。
     */
    SecurityPasswordPolicyVO passwordPolicy();

    /**
     * 更新或推进目标状态（{@code modifyPasswordPolicy}）。
     *
     * @param from 密码长度、复杂度、失败锁定和凭据有效期等密码安全配置字段。
     * @return 返回保存后的密码策略；校验或写入失败时抛出业务异常，不返回 null。
     */
    SecurityPasswordPolicyVO modifyPasswordPolicy(SecurityPasswordPolicyFrom from);
}
