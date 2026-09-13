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

package com.devops00.spectra.core.security.authorization.controller;

import com.devops00.spectra.common.audit.Audit;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationContextVO;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * 当前认证主体的授权上下文接口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@RestController
@RequestMapping("/security/context")
public class SecurityContextController {

    private final AuthorizationSnapshotProvider authorizationSnapshotProvider;

    private final SecurityContextAccessor securityContextAccessor;

    public SecurityContextController(AuthorizationSnapshotProvider authorizationSnapshotProvider,
                                     SecurityContextAccessor securityContextAccessor) {
        this.authorizationSnapshotProvider = authorizationSnapshotProvider;
        this.securityContextAccessor = securityContextAccessor;
    }

    /**
     * 查询或获取目标数据（{@code current}）。
     */
    @Audit("'查询授权上下文'")
    @GetMapping(version = "1.0.0")
    @PreAuthorize("isAuthenticated()")
    public AuthorizationContextVO current() {
        UUID userId = securityContextAccessor.currentUserId();
        if (userId == null) {
            throw new AccessDeniedException("当前认证主体缺少用户标识");
        }
        var snapshot = authorizationSnapshotProvider.load(userId);
        return new AuthorizationContextVO(snapshot.permissions(), snapshot.grantablePermissions());
    }
}
