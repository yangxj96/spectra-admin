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

package com.devops00.spectra.security.base.audit;

import org.springframework.security.core.Authentication;

/**
 * Security Audit 查询可见性策略端口。
 * <p>
 * 查询端只能依赖该策略，不得在 Controller/Service 中散落 Root 或管理员特判。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@FunctionalInterface
public interface AuditVisibilityPolicy {

    /**
     * 判断当前主体是否可以看到指定审计事件。
     *
     * @param viewer 当前主体
     * @param event  审计事件
     * @return 是否可见
     */
    boolean canView(Authentication viewer, SecurityAuditEvent event);
}
