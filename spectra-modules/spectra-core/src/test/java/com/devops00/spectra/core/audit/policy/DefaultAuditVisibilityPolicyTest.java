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
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * 验证 {@code DefaultAuditVisibilityPolicyTest} 的主要行为、边界条件和回归约束。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class DefaultAuditVisibilityPolicyTest {

    private final DefaultAuditVisibilityPolicy policy = new DefaultAuditVisibilityPolicy();

    @Test
    void operationRowsNeedAuditPermissionAtControllerButAreNotSubjectToSecurityVisibilityRules() {
        var reader = authentication(UUID.randomUUID(), "ROLE_USER");
        assertTrue(policy.canView(reader, event(AuditCategory.OPERATION,
                "SECURITY_ROOT_POLICY_CHANGED", UUID.randomUUID(), UUID.randomUUID())));
    }

    @Test
    void securityRowsKeepHighRiskAndOperatorTargetVisibilityRules() {
        UUID operator = UUID.randomUUID();
        UUID other = UUID.randomUUID();
        var root = authentication(UUID.randomUUID(), "ROLE_DEV_OPS");
        var systemAdmin = authentication(UUID.randomUUID(), "ROLE_SYSTEM_ADMIN");
        var owner = authentication(operator);

        assertTrue(policy.canView(root, event(AuditCategory.SECURITY, "SECURITY_ROOT_POLICY_CHANGED", operator, other)));
        assertFalse(policy.canView(systemAdmin,
                event(AuditCategory.SECURITY, "SECURITY_ROOT_POLICY_CHANGED", operator, other)));
        assertTrue(policy.canView(systemAdmin, event(AuditCategory.SECURITY, "USER_PROFILE_UPDATED", other, null)));
        assertTrue(policy.canView(owner, event(AuditCategory.SECURITY, "USER_PROFILE_UPDATED", operator, other)));
        assertFalse(policy.canView(owner, event(AuditCategory.SECURITY, "USER_PROFILE_UPDATED", other, null)));
    }

    @Test
    void unauthenticatedAndUnresolvableOrdinaryPrincipalFailClosedForSecurityRows() {
        var anonymous = new TestingAuthenticationToken("anonymousUser", null);
        anonymous.setAuthenticated(false);
        var unresolved = new TestingAuthenticationToken("not-a-uuid", null, "ROLE_USER");
        unresolved.setAuthenticated(true);
        var event = event(AuditCategory.SECURITY, "USER_PROFILE_UPDATED", UUID.randomUUID(), UUID.randomUUID());

        assertFalse(policy.canView(anonymous, event));
        assertFalse(policy.canView(unresolved, event));
    }

    /**
     * 处理身份认证对象相关数据。
     */
    private static TestingAuthenticationToken authentication(UUID principal, String... authorities) {
        var authentication = new TestingAuthenticationToken(principal, null, authorities);
        authentication.setAuthenticated(true);
        return authentication;
    }

    /**
     * 处理事件相关数据。
     */
    private static AuditRecord event(AuditCategory category, String type, UUID operator, UUID target) {
        return new AuditRecord(UUID.randomUUID(), category, type, target, AuditRecord.Result.SUCCEEDED,
                Instant.now(), new AuditContext(operator, null, null, "WEB", "127.0.0.1", "test"),
                Map.of("safe", "value"), Map.of(), null);
    }
}
