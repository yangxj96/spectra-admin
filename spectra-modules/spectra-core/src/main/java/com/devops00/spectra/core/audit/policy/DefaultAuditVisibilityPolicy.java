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

package com.devops00.spectra.core.audit.policy;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/**
 * 封装默认审计相关的业务规则和判定策略。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
public class DefaultAuditVisibilityPolicy implements AuditVisibilityPolicy {

    @Override
    public boolean canView(Authentication viewer, AuditRecord event) {
        if (viewer == null || !viewer.isAuthenticated() || event == null) {
            return false;
        }
        if (event.category() == AuditCategory.OPERATION || canViewHighRisk(viewer)) {
            return true;
        }
        if (isHighRiskEvent(event.eventType())) {
            return false;
        }
        if (canViewAllNonHighRisk(viewer)) {
            return true;
        }
        var viewerId = viewerId(viewer);
        return viewerId != null
                && (viewerId.equals(event.context().operatorId())
                        || viewerId.equals(event.targetId()));
    }
}
