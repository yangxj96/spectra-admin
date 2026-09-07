/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentApplyFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentChangeFrom;
import com.devops00.spectra.core.security.authorization.javabean.from.AuthorizationAssignmentRemovalFrom;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationChangePreviewVO;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/** 授权 Assignment 入口的影响分析与审计变更用例拆分测试。 */
class AuthorizationAssignmentChangeServiceSplitTest {

    @Test
    void routesPreviewAndChangesToTheirIndependentUseCases() {
        var impactService = mock(AuthorizationImpactService.class);
        var auditService = mock(AuthorizationAuditService.class);
        var service = new AuthorizationAssignmentChangeServiceImpl(impactService, auditService);
        var targetUserId = UUID.randomUUID();
        var change = mock(AuthorizationAssignmentChangeFrom.class);
        var preview = mock(AuthorizationChangePreviewVO.class);
        when(impactService.preview(targetUserId, change)).thenReturn(preview);

        assertThat(service.preview(targetUserId, change)).isSameAs(preview);

        var apply = mock(AuthorizationAssignmentApplyFrom.class);
        var removal = mock(AuthorizationAssignmentRemovalFrom.class);
        service.apply(targetUserId, apply);
        service.revoke(targetUserId, removal);
        service.ensureDefaultUserRole(targetUserId);

        verify(impactService).preview(targetUserId, change);
        verify(auditService).apply(targetUserId, apply);
        verify(auditService).revoke(targetUserId, removal);
        verify(auditService).ensureDefaultUserRole(targetUserId);
    }
}
