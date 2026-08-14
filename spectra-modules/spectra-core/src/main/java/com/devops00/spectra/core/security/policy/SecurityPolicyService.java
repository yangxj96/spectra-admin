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

package com.devops00.spectra.core.security.policy;

import com.devops00.spectra.core.security.policy.javabean.from.SecurityPasswordPolicyFrom;
import com.devops00.spectra.core.security.policy.javabean.from.SecuritySessionPolicyFrom;
import com.devops00.spectra.core.security.policy.javabean.vo.SecurityPasswordPolicyVO;
import com.devops00.spectra.core.security.policy.javabean.vo.SecuritySessionPolicyVO;

import java.util.List;
import java.util.UUID;

/** 安全策略查询与受审计修改服务。 */
public interface SecurityPolicyService {

    List<SecuritySessionPolicyVO> sessionPolicies();

    SecuritySessionPolicyVO modifySessionPolicy(UUID clientId, SecuritySessionPolicyFrom from);

    SecurityPasswordPolicyVO passwordPolicy();

    SecurityPasswordPolicyVO modifyPasswordPolicy(SecurityPasswordPolicyFrom from);
}
