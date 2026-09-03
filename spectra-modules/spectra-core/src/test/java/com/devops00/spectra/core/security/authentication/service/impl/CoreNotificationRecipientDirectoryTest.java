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

package com.devops00.spectra.core.security.authentication.service.impl;

import com.devops00.spectra.common.notification.NotificationRecipient;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.common.security.authorization.AuthorizationAssignment;
import com.devops00.spectra.common.security.authorization.AuthorizationScope;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.PermissionBoundary;
import com.devops00.spectra.common.security.authorization.ScopeMode;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 通知用户收件人展开的数据范围边界测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/14
 */
class CoreNotificationRecipientDirectoryTest {

    private static final UUID CURRENT_USER = UUID.randomUUID();

    private static final UUID RECIPIENT_USER = UUID.randomUUID();

    private static final UUID CURRENT_DEPARTMENT = UUID.randomUUID();

    private static final UUID OTHER_DEPARTMENT = UUID.randomUUID();

    private UserService userService;

    private AuthorizationSnapshotProvider authorizationSnapshotProvider;

    private DepartmentService departmentService;

    private SecurityContextAccessor security;

    private UserContactService userContactService;

    @BeforeEach
    void setUp() {
        userService = mock(UserService.class);
        authorizationSnapshotProvider = mock(AuthorizationSnapshotProvider.class);
        departmentService = mock(DepartmentService.class);
        security = mock(SecurityContextAccessor.class);
        var recipient = new User();
        recipient.setId(RECIPIENT_USER);
        recipient.setStatus(UserStatus.ACTIVE);
        recipient.setDepartmentId(CURRENT_DEPARTMENT);
        when(userService.getById(RECIPIENT_USER)).thenReturn(recipient);
        when(security.currentUserId()).thenReturn(CURRENT_USER);
        userContactService = mock(UserContactService.class);
        var phone = new UserContact();
        phone.setContactType(UserContactService.PHONE);
        phone.setContactValue("13800138000");
        var email = new UserContact();
        email.setContactType(UserContactService.EMAIL);
        email.setContactValue("recipient@example.com");
        when(userContactService.listActiveByUserId(RECIPIENT_USER)).thenReturn(List.of(phone, email));
    }

    @Test
    void shouldKeepGlobalScopeAbleToTargetAnyUser() {
        allowScope(AuthorizationScope.of(ScopeMode.ALL));

        var result = directory().resolve(List.of(RECIPIENT_USER));

        assertEquals(1, result.size());
        assertEquals(RECIPIENT_USER, result.getFirst().userId());
    }

    @Test
    void shouldRejectRecipientOutsideDepartmentScope() {
        var recipient = new User();
        recipient.setId(RECIPIENT_USER);
        recipient.setDepartmentId(OTHER_DEPARTMENT);
        when(userService.getById(RECIPIENT_USER)).thenReturn(recipient);
        allowScope(new AuthorizationScope(ScopeMode.RULES, Set.of(CURRENT_DEPARTMENT), false));

        var result = directory().resolve(List.of(RECIPIENT_USER));

        assertEquals(0, result.stream().filter(NotificationRecipient::active).count());
    }

    @Test
    void shouldAllowSystemTaskWithoutUserContext() {
        when(security.currentUserId()).thenReturn(null);

        var result = directory().resolve(List.of(RECIPIENT_USER));

        assertEquals(1, result.size());
    }

    @Test
    void shouldKeepSelfScopeBoundToCurrentUser() {
        allowScope(AuthorizationScope.of(ScopeMode.SELF));

        var result = directory().resolve(List.of(RECIPIENT_USER));

        assertEquals(0, result.stream().filter(NotificationRecipient::active).count());
        verify(userService, never()).getById(RECIPIENT_USER);
    }

    @Test
    void shouldHonorCustomDepartmentScope() {
        allowScope(new AuthorizationScope(ScopeMode.RULES, Set.of(CURRENT_DEPARTMENT), false));

        var result = directory().resolve(List.of(RECIPIENT_USER));

        assertEquals(1, result.stream().filter(NotificationRecipient::active).count());
    }

    private CoreNotificationRecipientDirectory directory() {
        return new CoreNotificationRecipientDirectory(userService, userContactService, authorizationSnapshotProvider,
                departmentService, security);
    }

    private void allowScope(AuthorizationScope scope) {
        when(authorizationSnapshotProvider.load(CURRENT_USER)).thenReturn(AuthorizationSnapshot.of(List.of(
                new AuthorizationAssignment(UUID.randomUUID(), "ROLE_TEST", 1,
                        Map.of("user:read", new PermissionBoundary("user:read", scope)), Map.of()))));
        when(departmentService.getById(CURRENT_DEPARTMENT)).thenReturn(null);
    }
}
