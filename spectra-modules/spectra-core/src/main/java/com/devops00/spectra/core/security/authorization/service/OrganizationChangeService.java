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

import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationChangeApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.OrganizationCreateApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.OrganizationChangePreviewVO;

import java.util.UUID;

/**
 * 组织结构安全变更 Preview/Apply 服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public interface OrganizationChangeService {

    /**
     * 查询或获取目标数据（{@code currentOrganizationVersion}）。
     *
     * @return 返回当前组织结构版本号；尚未发生组织变更时返回初始版本值 0，不返回 null。
     */
    long currentOrganizationVersion();

    /**
     * 处理内部业务逻辑（{@code preview}）。
     *
     * @param departmentId 待变更组织节点的唯一标识。
     * @param from         部门名称、父部门、负责人和组织属性等变更字段。
     * @return 返回组织变更预览的校验预览结果；输入不合法或当前用户无权操作时抛出业务异常，不返回 null。
     */
    OrganizationChangePreviewVO preview(UUID departmentId, OrganizationChangeFrom from);

    /**
     * 更新或推进目标状态（{@code apply}）。
     *
     * @param departmentId 待应用组织变更的部门唯一标识。
     * @param from         绑定预览令牌并确认组织变更版本的申请字段。
     */
    void apply(UUID departmentId, OrganizationChangeApplyFrom from);

    /**
     * 处理内部业务逻辑（{@code previewCreate}）。
     *
     * @param from 待新建组织节点的名称、父部门、负责人和组织属性等字段。
     * @return 返回组织变更预览的校验预览结果；输入不合法或当前用户无权操作时抛出业务异常，不返回 null。
     */
    OrganizationChangePreviewVO previewCreate(OrganizationChangeFrom from);

    /**
     * 更新或推进目标状态（{@code applyCreate}）。
     *
     * @param from 绑定预览令牌并确认新建组织版本的申请字段。
     */
    void applyCreate(OrganizationCreateApplyFrom from);
}
