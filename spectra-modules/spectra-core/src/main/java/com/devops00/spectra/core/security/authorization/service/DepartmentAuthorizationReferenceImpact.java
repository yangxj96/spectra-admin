/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.core.security.authorization.service;

import java.util.Set;
import java.util.UUID;

/**
 * 部门合并对授权引用的影响摘要。
 *
 * @param assignmentCount          被改写的活动 Assignment 数量
 * @param profileCount             被改写的未删除授权方案数量
 * @param accessRuleCount          被改写的 Access 部门规则数量
 * @param grantRuleCount           被改写的 Grant 部门规则数量
 * @param profileAccessScopeCount  被改写的方案 Access Scope 数量
 * @param profileGrantScopeCount   被改写的方案 Grant Scope 数量
 * @param expandsEffectiveAuthority 是否可能扩大有效授权范围
 * @param affectedUserIds          使用受影响活动 Assignment 的用户 ID
 * @param stateFingerprint         授权引用快照摘要，用于 Apply 检查 Preview 后的变化
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
public record DepartmentAuthorizationReferenceImpact(
        int assignmentCount,
        int profileCount,
        int accessRuleCount,
        int grantRuleCount,
        int profileAccessScopeCount,
        int profileGrantScopeCount,
        boolean expandsEffectiveAuthority,
        Set<UUID> affectedUserIds,
        String stateFingerprint) {

    public DepartmentAuthorizationReferenceImpact {
        affectedUserIds = Set.copyOf(affectedUserIds);
    }
}
