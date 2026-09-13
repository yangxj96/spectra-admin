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

package com.devops00.spectra.core.security.authorization.domain;

/**
 * 承载组织相关的不可变数据。
 *
 * @param beforeVersion             变更前的版本号
 * @param afterVersion              变更后的版本号
 * @param expandsEffectiveAuthority 本次变更是否扩大用户的有效权限
 * @param affectedAssignmentCount   本次变更影响的权限分配数量
 * @param affectedUserCount         本次变更影响的用户数量
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public record OrganizationChangeImpact(long beforeVersion,
                                       long afterVersion,
                                       boolean expandsEffectiveAuthority,
                                       int affectedAssignmentCount,
                                       int affectedUserCount) {

    public OrganizationChangeImpact {
        if (beforeVersion < 0 || afterVersion < beforeVersion) {
            throw new IllegalArgumentException("组织版本必须单调递增");
        }
        if (affectedAssignmentCount < 0 || affectedUserCount < 0) {
            throw new IllegalArgumentException("影响数量不能为负数");
        }
    }
}
