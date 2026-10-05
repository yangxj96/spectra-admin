package com.devops00.spectra.core.audit.policy;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditContext;
import com.devops00.spectra.common.audit.AuditRecord;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DefaultAuditVisibilityPolicyTest {

    private final DefaultAuditVisibilityPolicy policy = new DefaultAuditVisibilityPolicy();

    @Test
    void auditorCanViewNonHighRiskEventsAcrossUsers() {
        var auditor = authentication("ROLE_AUDIT");
        var event = event(AuditCategory.SECURITY, "USER_PROFILE_READ", UUID.randomUUID());

        assertTrue(policy.canViewAllNonHighRisk(auditor));
        assertTrue(policy.canView(auditor, event));
    }

    @Test
    void auditorCannotViewHighRiskEventsEvenWhenTheyAreOperationEvents() {
        var auditor = authentication("ROLE_AUDIT");
        var event = event(AuditCategory.OPERATION, "SECURITY_PASSWORD_RESET", UUID.randomUUID());

        assertFalse(policy.canViewHighRisk(auditor));
        assertFalse(policy.canView(auditor, event));
    }

    @Test
    void rootCanViewHighRiskEvents() {
        var root = authentication("ROLE_DEV_OPS");
        var event = event(AuditCategory.SECURITY, "SECURITY_PASSWORD_RESET", UUID.randomUUID());

        assertTrue(policy.canViewHighRisk(root));
        assertTrue(policy.canView(root, event));
    }

    private static UsernamePasswordAuthenticationToken authentication(String role) {
        return UsernamePasswordAuthenticationToken.authenticated(
                UUID.randomUUID(),
                "n/a",
                List.of(new SimpleGrantedAuthority(role)));
    }

    private static AuditRecord event(AuditCategory category, String eventType, UUID targetId) {
        return new AuditRecord(
                UUID.randomUUID(),
                category,
                eventType,
                targetId,
                AuditRecord.Result.SUCCEEDED,
                null,
                AuditContext.empty(),
                Map.of(),
                Map.of(),
                null, AuditRecord.HttpSummary.empty(), null);
    }
}
