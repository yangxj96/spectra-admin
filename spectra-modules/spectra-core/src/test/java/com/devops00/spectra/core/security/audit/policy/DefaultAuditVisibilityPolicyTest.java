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

import com.devops00.spectra.security.base.audit.AuditResult;
import com.devops00.spectra.security.base.audit.SecurityAuditEvent;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultAuditVisibilityPolicyTest {

    private final DefaultAuditVisibilityPolicy policy = new DefaultAuditVisibilityPolicy();

    @Test
    void rootCanViewHighRiskButSystemAdminCannot() {
        var root = authentication(UUID.randomUUID(), "ROLE_DEV_OPS");
        var systemAdmin = authentication(UUID.randomUUID(), "ROLE_SYSTEM_ADMIN");
        var event = event("SECURITY_ROOT_POLICY_CHANGED", UUID.randomUUID(), UUID.randomUUID());

        assertTrue(policy.canView(root, event));
        assertFalse(policy.canView(systemAdmin, event));
    }

    @Test
    void systemAdminCanViewNormalEventsButOrdinaryUserOnlySeesOwnEvents() {
        UUID operatorId = UUID.randomUUID();
        UUID otherId = UUID.randomUUID();
        var systemAdmin = authentication(UUID.randomUUID(), "ROLE_SYSTEM_ADMIN");
        var owner = authentication(operatorId);
        var other = authentication(otherId);
        var ownEvent = event("USER_PROFILE_UPDATED", operatorId, otherId);
        var unrelatedEvent = event("USER_PROFILE_UPDATED", otherId, UUID.randomUUID());

        assertTrue(policy.canView(systemAdmin, unrelatedEvent));
        assertTrue(policy.canView(owner, ownEvent));
        assertFalse(policy.canView(owner, unrelatedEvent));
        assertFalse(policy.canView(other, event("USER_PROFILE_UPDATED", operatorId, UUID.randomUUID())));
    }

    @Test
    void unauthenticatedAndMissingPrincipalFailClosed() {
        var anonymous = new TestingAuthenticationToken("anonymousUser", null);
        anonymous.setAuthenticated(false);
        var event = event("USER_PROFILE_UPDATED", UUID.randomUUID(), UUID.randomUUID());

        assertFalse(policy.canView(anonymous, event));
        assertFalse(policy.canView(new TestingAuthenticationToken("not-a-uuid", null), event));
    }

    private static TestingAuthenticationToken authentication(UUID principal, String... authorities) {
        var authentication = new TestingAuthenticationToken(principal, null, authorities);
        authentication.setAuthenticated(true);
        return authentication;
    }

    private static SecurityAuditEvent event(String type, UUID operatorId, UUID targetId) {
        return new SecurityAuditEvent(UUID.randomUUID(), type, operatorId, targetId, "WEB", "127.0.0.1", "test",
                Map.of("safe", "value"), Map.of(), null, null, AuditResult.SUCCEEDED, "correlation");
    }
}
