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

package com.devops00.spectra.core.security.authorization.service;

import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentRemovalFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationChangePreviewVO;

import java.util.UUID;

/**
 * RoleAssignment Preview/Apply 应用服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public interface AuthorizationAssignmentChangeService {

    /**
     * 生成短期、绑定版本的变更预览。
     *
     * @param targetUserId 待变更角色分配的目标用户唯一标识。
     * @param from         待授予角色、授权边界和有效期等变更字段。
     * @return 返回授权变更预览的校验预览结果；输入不合法或当前用户无权操作时抛出业务异常，不返回 null。
     */
    AuthorizationChangePreviewVO preview(UUID targetUserId, AuthorizationAssignmentChangeFrom from);

    /**
     * 校验 Preview token 并原子应用 RoleAssignment 与其 Boundary。
     *
     * @param targetUserId 要应用角色分配变更的目标用户唯一标识。
     * @param from         绑定预览令牌并确认授权版本的申请字段。
     */
    void apply(UUID targetUserId, AuthorizationAssignmentApplyFrom from);

    /**
     * 撤销目标用户的一个活动 RoleAssignment。
     *
     * @param targetUserId 要撤销角色分配的目标用户唯一标识。
     * @param from         待撤销角色分配的唯一标识及撤销原因。
     */
    void revoke(UUID targetUserId, AuthorizationAssignmentRemovalFrom from);

    /**
     * 确保目标用户拥有系统自动维护的普通用户基础角色。
     *
     * @param targetUserId 需要补齐系统默认普通用户角色的用户唯一标识。
     */
    void ensureDefaultUserRole(UUID targetUserId);
}
