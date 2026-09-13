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

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentRemovalFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationChangePreviewVO;
import com.devops00.spectra.core.security.authorization.service.AuthorizationAssignmentChangeService;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * 授权 Assignment 公开入口，只负责路由影响分析和审计变更用例。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
@Primary
public class AuthorizationAssignmentChangeServiceImpl implements AuthorizationAssignmentChangeService {

    private final AuthorizationImpactService impactService;

    private final AuthorizationAuditService auditService;

    public AuthorizationAssignmentChangeServiceImpl(AuthorizationImpactService impactService,
                                                    AuthorizationAuditService auditService) {
        this.impactService = impactService;
        this.auditService = auditService;
    }

    @Override
    public AuthorizationChangePreviewVO preview(UUID targetUserId, AuthorizationAssignmentChangeFrom from) {
        return impactService.preview(targetUserId, from);
    }

    @Override
    public void apply(UUID targetUserId, AuthorizationAssignmentApplyFrom from) {
        auditService.apply(targetUserId, from);
    }

    @Override
    public void revoke(UUID targetUserId, AuthorizationAssignmentRemovalFrom from) {
        auditService.revoke(targetUserId, from);
    }

    @Override
    public void ensureDefaultUserRole(UUID targetUserId) {
        auditService.ensureDefaultUserRole(targetUserId);
    }
}
