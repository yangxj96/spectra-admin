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

package com.devops00.spectra.core.authorization.controller;

import com.devops00.spectra.core.authorization.service.AuthorizationAssignmentQueryService;
import com.devops00.spectra.core.authorization.vo.AuthorizationAssignmentView;
import com.devops00.spectra.log.base.annotation.ULog;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * 目标授权模型只读查询入口。RoleAssignment 写入仍由 Phase 4 GrantBoundary 流程统一接管。
 */
@RestController
@RequestMapping("/security/authorization")
@RequiredArgsConstructor
public class AuthorizationController {

    private final AuthorizationAssignmentQueryService queryService;

    @ULog("'查询用户授权实例'")
    @GetMapping(value = "/users/{userId}/assignments", version = "2.0.0+")
    @PreAuthorize("hasPermission(null ,'role:read')")
    public List<AuthorizationAssignmentView> assignments(@PathVariable UUID userId) {
        return queryService.findByUserId(userId);
    }
}
