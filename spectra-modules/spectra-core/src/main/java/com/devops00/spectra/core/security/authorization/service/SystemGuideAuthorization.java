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

import com.devops00.spectra.security.base.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** 系统引导的 DEV_OPS 身份判断。 */
@Component
@RequiredArgsConstructor
public class SystemGuideAuthorization {

    private final AuthorizationSnapshotProvider authorizationSnapshotProvider;
    private final SecurityContextAccessor securityContextAccessor;

    /**
     * 判断当前用户是否为 DEV_OPS Root。
     *
     * @return 是否为 DEV_OPS
     */
    public boolean isDevOps() {
        UUID userId = securityContextAccessor.currentUserId();
        return userId != null && authorizationSnapshotProvider.load(userId).isRoot();
    }

    /** 断言当前用户为 DEV_OPS Root。 */
    public void assertDevOps() {
        if (!isDevOps()) {
            throw new AccessDeniedException("只有 DEV_OPS 可以完成系统设置引导");
        }
    }
}
