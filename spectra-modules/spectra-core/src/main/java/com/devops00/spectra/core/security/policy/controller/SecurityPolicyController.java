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

package com.devops00.spectra.core.security.policy.controller;

import com.devops00.spectra.core.security.policy.javabean.from.SecurityPasswordPolicyFrom;
import com.devops00.spectra.core.security.policy.javabean.from.SecuritySessionPolicyFrom;
import com.devops00.spectra.core.security.policy.javabean.vo.SecurityPasswordPolicyVO;
import com.devops00.spectra.core.security.policy.javabean.vo.SecuritySessionPolicyVO;
import com.devops00.spectra.core.security.policy.service.SecurityPolicyService;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** 安全会话和密码策略管理接口。 */
@Slf4j
@Validated
@RestController
@RequestMapping("/security/policy")
@RequiredArgsConstructor
public class SecurityPolicyController {

    private final SecurityPolicyService policyService;

    /**
     * 查询或获取目标数据（{@code sessionPolicies}）。
     */
    @ULog("'查询会话策略'")
    @GetMapping(value = "/session", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:session-policy:update')")
    public List<SecuritySessionPolicyVO> sessionPolicies() {
        return policyService.sessionPolicies();
    }

    /**
     * 更新或推进目标状态（{@code modifySessionPolicy}）。
     */
    @ULog("'修改会话策略'")
    @PutMapping(value = "/session/{clientId}", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:session-policy:update')")
    public SecuritySessionPolicyVO modifySessionPolicy(@PathVariable UUID clientId,
                                                       @Validated @RequestBody SecuritySessionPolicyFrom from) {
        return policyService.modifySessionPolicy(clientId, from);
    }

    /**
     * 查询或获取目标数据（{@code passwordPolicy}）。
     */
    @ULog("'查询密码策略'")
    @GetMapping(value = "/password", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:password-policy:update')")
    public SecurityPasswordPolicyVO passwordPolicy() {
        return policyService.passwordPolicy();
    }

    /**
     * 更新或推进目标状态（{@code modifyPasswordPolicy}）。
     */
    @ULog("'修改密码策略'")
    @PutMapping(value = "/password", version = "1.0.0")
    @PreAuthorize("hasPermission(null, 'security:password-policy:update')")
    public SecurityPasswordPolicyVO modifyPasswordPolicy(@Validated @RequestBody SecurityPasswordPolicyFrom from) {
        return policyService.modifyPasswordPolicy(from);
    }
}
