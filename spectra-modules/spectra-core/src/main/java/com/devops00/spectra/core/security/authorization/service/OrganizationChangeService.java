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
     */
    long currentOrganizationVersion();

    /**
     * 处理内部业务逻辑（{@code preview}）。
     */
    OrganizationChangePreviewVO preview(UUID departmentId, OrganizationChangeFrom from);

    /**
     * 更新或推进目标状态（{@code apply}）。
     */
    void apply(UUID departmentId, OrganizationChangeApplyFrom from);

    /**
     * 处理内部业务逻辑（{@code previewCreate}）。
     */
    OrganizationChangePreviewVO previewCreate(OrganizationChangeFrom from);

    /**
     * 更新或推进目标状态（{@code applyCreate}）。
     */
    void applyCreate(OrganizationCreateApplyFrom from);
}
