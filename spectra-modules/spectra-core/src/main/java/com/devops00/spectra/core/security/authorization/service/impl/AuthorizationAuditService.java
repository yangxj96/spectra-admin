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
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentRemovalFrom;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

/**
 * 授权 Assignment 变更、审计和安全失效通知用例。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Service
@RequiredArgsConstructor
public class AuthorizationAuditService {

    private final AuthorizationAssignmentChangeServiceSupport support;

    @Transactional
    public void apply(UUID targetUserId, AuthorizationAssignmentApplyFrom from) {
        support.apply(targetUserId, from);
    }

    @Transactional
    public void revoke(UUID targetUserId, AuthorizationAssignmentRemovalFrom from) {
        support.revoke(targetUserId, from);
    }

    @Transactional
    public void ensureDefaultUserRole(UUID targetUserId) {
        support.ensureDefaultUserRole(targetUserId);
    }
}
