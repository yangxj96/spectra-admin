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

import com.devops00.spectra.core.security.authorization.javabean.from.RoleAuthorizationApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.RoleAuthorizationChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.RoleAuthorizationChangePreviewVO;
import com.devops00.spectra.core.security.authorization.javabean.vo.RoleAuthorizationStateVO;

import java.util.UUID;

/**
 * Role 高风险授权变更的 Preview/Apply 服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public interface RoleAuthorizationChangeService {

    /**
     * 查询或获取目标数据（{@code current}）。
     *
     * @param roleId 要读取授权状态的角色唯一标识。
     * @return 返回符合条件的角色授权状态详情；记录不存在或当前用户不可见时抛出业务异常，不返回 null。
     */
    RoleAuthorizationStateVO current(UUID roleId);

    /**
     * 处理内部业务逻辑（{@code preview}）。
     *
     * @param roleId 待预览授权变更的角色唯一标识。
     * @param from   待授予或撤销的权限、数据范围和授权等级字段。
     * @return 返回角色授权变更预览的校验预览结果；输入不合法或当前用户无权操作时抛出业务异常，不返回 null。
     */
    RoleAuthorizationChangePreviewVO preview(UUID roleId, RoleAuthorizationChangeFrom from);

    /**
     * 更新或推进目标状态（{@code apply}）。
     *
     * @param roleId 待应用授权变更的角色唯一标识。
     * @param from   绑定预览令牌并确认角色授权版本的申请字段。
     */
    void apply(UUID roleId, RoleAuthorizationApplyFrom from);
}
