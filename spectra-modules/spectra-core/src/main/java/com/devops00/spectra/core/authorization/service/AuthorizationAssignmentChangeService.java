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

package com.devops00.spectra.core.authorization.service;

import com.devops00.spectra.core.authorization.javabean.from.AuthorizationAssignmentApplyFrom;
import com.devops00.spectra.core.authorization.javabean.from.AuthorizationAssignmentChangeFrom;
import com.devops00.spectra.core.authorization.javabean.vo.AuthorizationChangePreviewVO;

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
     */
    AuthorizationChangePreviewVO preview(UUID targetUserId, AuthorizationAssignmentChangeFrom from);

    /**
     * 校验 Preview token 并原子应用 RoleAssignment 与其 Boundary。
     */
    void apply(UUID targetUserId, AuthorizationAssignmentApplyFrom from);
}
