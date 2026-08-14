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

package com.devops00.spectra.core.authorization;

import com.devops00.spectra.common.exception.DataException;

/**
 * Phase 4 期间冻结旧的用户角色、角色权限和用户 Scope 写入口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
public final class LegacyAuthorizationWriteGuard {

    private LegacyAuthorizationWriteGuard() {
    }

    public static void reject(String operation) {
        throw new DataException(operation + " 已冻结，请使用 /security/authorization 的 Preview/Apply API");
    }
}
