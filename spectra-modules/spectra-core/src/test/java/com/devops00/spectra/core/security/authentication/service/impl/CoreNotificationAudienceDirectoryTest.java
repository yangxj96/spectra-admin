package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.notification.NotificationAudience;
import com.devops00.spectra.core.security.authorization.mapper.RoleAssignmentMapper;
import com.devops00.spectra.core.security.authorization.mapper.SecurityRoleMapper;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class CoreNotificationAudienceDirectoryTest {

    @Test
    void deduplicatesDepartmentAudienceAcrossPrimaryAndAssociatedMemberships() {
        var departmentId = UUID.randomUUID();
        var userId = UUID.randomUUID();
        var anotherUserId = UUID.randomUUID();
        var departmentService = mock(DepartmentService.class);
        var membershipMapper = mock(UserDepartmentMembershipMapper.class);
        when(departmentService.getSelfAndDescendantIds(departmentId)).thenReturn(List.of(departmentId));
        when(membershipMapper.selectUserIdsByDepartmentIds(List.of(departmentId), UserStatus.ACTIVE.name()))
                .thenReturn(List.of(userId, anotherUserId, userId));
        var directory = new CoreNotificationAudienceDirectory(departmentService, membershipMapper,
                mock(RoleAssignmentMapper.class), mock(SecurityRoleMapper.class));

        var result = directory.resolve(new NotificationAudience(List.of(userId), List.of(departmentId), List.of()));

        assertEquals(List.of(userId, anotherUserId), result);
    }
}
