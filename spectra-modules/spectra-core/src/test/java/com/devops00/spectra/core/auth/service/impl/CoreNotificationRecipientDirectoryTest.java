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

package com.devops00.spectra.core.auth.service.impl;

import com.devops00.spectra.common.constant.DataScopeType;
import com.devops00.spectra.common.mybatis.DataScopeProvider;
import com.devops00.spectra.common.notification.NotificationRecipient;
import com.devops00.spectra.core.auth.javabean.constant.AccountStatus;
import com.devops00.spectra.core.auth.javabean.entity.Account;
import com.devops00.spectra.core.auth.service.AccountService;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.service.UserService;
import com.devops00.spectra.security.base.holder.SecHolderStrategy;
import com.devops00.spectra.security.base.holder.SecUtil;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

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

    private AccountService accountService;

    private UserService userService;

    private DataScopeProvider dataScopeProvider;

    private SecHolderStrategy security;

    @BeforeEach
    void setUp() {
        accountService = mock(AccountService.class);
        userService = mock(UserService.class);
        dataScopeProvider = mock(DataScopeProvider.class);
        security = mock(SecHolderStrategy.class);
        SecUtil.setHolder(security);
        var account = new Account();
        account.setStatus(AccountStatus.ACTIVE.getCode());
        account.setVerified((short) 1);
        when(accountService.listByUserId(RECIPIENT_USER)).thenReturn(List.of(account));
        var recipient = new User();
        recipient.setId(RECIPIENT_USER);
        recipient.setDepartmentId(CURRENT_DEPARTMENT);
        when(userService.getById(RECIPIENT_USER)).thenReturn(recipient);
        when(security.getCurrentUserId()).thenReturn(CURRENT_USER);
    }

    @AfterEach
    void tearDown() {
        SecUtil.setHolder(null);
    }

    @Test
    void shouldKeepGlobalScopeAbleToTargetAnyUser() {
        when(dataScopeProvider.resolve(CURRENT_USER))
                .thenReturn(new DataScopeProvider.EffectiveScope(DataScopeType.GLOBAL, CURRENT_DEPARTMENT, List.of()));

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
        when(dataScopeProvider.resolve(CURRENT_USER))
                .thenReturn(new DataScopeProvider.EffectiveScope(DataScopeType.DEPT, CURRENT_DEPARTMENT,
                        List.of(CURRENT_DEPARTMENT)));

        var result = directory().resolve(List.of(RECIPIENT_USER));

        assertEquals(0, result.stream().filter(NotificationRecipient::active).count());
    }

    @Test
    void shouldAllowSystemTaskWithoutUserContext() {
        when(security.getCurrentUserId()).thenReturn(null);

        var result = directory().resolve(List.of(RECIPIENT_USER));

        assertEquals(1, result.size());
    }

    @Test
    void shouldKeepSelfScopeBoundToCurrentUser() {
        when(dataScopeProvider.resolve(CURRENT_USER))
                .thenReturn(new DataScopeProvider.EffectiveScope(DataScopeType.SELF, CURRENT_DEPARTMENT, List.of()));

        var result = directory().resolve(List.of(RECIPIENT_USER));

        assertEquals(0, result.stream().filter(NotificationRecipient::active).count());
        verify(accountService, never()).listByUserId(RECIPIENT_USER);
    }

    @Test
    void shouldHonorCustomDepartmentScope() {
        when(dataScopeProvider.resolve(CURRENT_USER))
                .thenReturn(new DataScopeProvider.EffectiveScope(DataScopeType.CUSTOM, CURRENT_DEPARTMENT,
                        List.of(CURRENT_DEPARTMENT)));

        var result = directory().resolve(List.of(RECIPIENT_USER));

        assertEquals(1, result.stream().filter(NotificationRecipient::active).count());
    }

    private CoreNotificationRecipientDirectory directory() {
        return new CoreNotificationRecipientDirectory(accountService, userService, dataScopeProvider);
    }
}
