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

package com.devops00.spectra.core.security.audit.policy;

import com.devops00.spectra.security.base.audit.AuditVisibilityPolicy;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 安全审计查询可见性策略。
 * <p>
 * Root/break-glass 可查看全部事件；SYSTEM_ADMIN 只能查看非高风险事件；普通主体只能查看
 * 自己作为 operator 或 target 的非高风险事件。该规则同时被列表、详情和导出使用。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
@Component
public class DefaultAuditVisibilityPolicy implements AuditVisibilityPolicy {

    @Override
    public boolean canView(Authentication viewer, SecurityAuditEvent event) {
        if (viewer == null || !viewer.isAuthenticated() || event == null) {
            return false;
        }
        if (canViewHighRisk(viewer)) {
            return true;
        }
        if (isHighRiskEvent(event.eventType())) {
            return false;
        }
        if (canViewAllNonHighRisk(viewer)) {
            return true;
        }
        var viewerId = viewerId(viewer);
        return viewerId != null && (viewerId.equals(event.operatorId()) || viewerId.equals(event.targetId()));
    }
}
