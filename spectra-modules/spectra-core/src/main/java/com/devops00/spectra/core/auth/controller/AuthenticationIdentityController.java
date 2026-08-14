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

package com.devops00.spectra.core.auth.controller;

import com.devops00.spectra.core.auth.javabean.from.BindEmailFrom;
import com.devops00.spectra.core.auth.javabean.from.BindPhoneFrom;
import com.devops00.spectra.core.auth.javabean.vo.AuthenticationIdentityVO;
import com.devops00.spectra.core.auth.service.AuthenticationIdentityBindingService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/** 目标认证身份绑定控制器。 */
@Slf4j
@RestController
@RequestMapping("/security/identities")
@RequiredArgsConstructor
public class AuthenticationIdentityController {

    private final AuthenticationIdentityBindingService bindingService;

    private final SecurityContextAccessor securityContextAccessor;

    @ULog("'获取认证身份列表'")
    @GetMapping(version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'account:read')")
    public List<AuthenticationIdentityVO> list() {
        UUID userId = securityContextAccessor.currentUserId();
        return bindingService.listByUserId(userId).stream().map(identity -> {
            var vo = new AuthenticationIdentityVO();
            vo.setId(identity.getId());
            vo.setMethodCode(identity.getMethodCode());
            vo.setProviderCode(identity.getProviderCode());
            vo.setState(identity.getState());
            vo.setVerifiedAt(identity.getVerifiedAt());
            vo.setCurrent(false);
            return vo;
        }).toList();
    }

    @ULog("'绑定手机号认证身份'")
    @PostMapping(value = "/phone", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'account:update')")
    public void bindPhone(@Validated @RequestBody BindPhoneFrom params) {
        bindingService.bindPhone(securityContextAccessor.currentUserId(), params.getPhone(), params.getCode());
    }

    @ULog("'绑定邮箱认证身份'")
    @PostMapping(value = "/email", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'account:update')")
    public void bindEmail(@Validated @RequestBody BindEmailFrom params) {
        bindingService.bindEmail(securityContextAccessor.currentUserId(), params.getEmail(), params.getCode());
    }

    @ULog("'撤销认证身份'")
    @DeleteMapping(value = "/{identityId}", version = "1.0.0+")
    @PreAuthorize("hasPermission(null ,'account:update')")
    public void unbind(@PathVariable UUID identityId) {
        bindingService.unbind(securityContextAccessor.currentUserId(), identityId);
    }
}
