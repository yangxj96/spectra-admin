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

import com.devops00.spectra.core.authorization.javabean.from.RoleAuthorizationApplyFrom;
import com.devops00.spectra.core.authorization.javabean.from.RoleAuthorizationChangeFrom;
import com.devops00.spectra.core.authorization.javabean.vo.RoleAuthorizationChangePreviewVO;
import com.devops00.spectra.core.authorization.javabean.vo.RoleAuthorizationStateVO;

import java.util.UUID;

/**
 * Role 高风险授权变更的 Preview/Apply 服务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public interface RoleAuthorizationChangeService {

    RoleAuthorizationStateVO current(UUID roleId);

    RoleAuthorizationChangePreviewVO preview(UUID roleId, RoleAuthorizationChangeFrom from);

    void apply(UUID roleId, RoleAuthorizationApplyFrom from);
}
