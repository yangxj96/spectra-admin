/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.audit.policy;

import com.devops00.spectra.common.audit.AuditCategory;
import com.devops00.spectra.common.audit.AuditRecord;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

/** Preserves security-event visibility while allowing audit readers to view operation events. */
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
