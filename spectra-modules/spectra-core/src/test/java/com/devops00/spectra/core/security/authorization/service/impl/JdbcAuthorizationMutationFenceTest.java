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

package com.devops00.spectra.core.security.authorization.service.impl;

import com.devops00.spectra.core.user.javabean.constant.UserStatus;
import com.devops00.spectra.core.user.javabean.entity.User;
import com.devops00.spectra.core.user.mapper.UserMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/** 验证授权写事务的用户行锁在身份和数据库故障时拒绝。 */
class JdbcAuthorizationMutationFenceTest {

    private final UserMapper mapper = mock(UserMapper.class);

    private final JdbcAuthorizationMutationFence fence = new JdbcAuthorizationMutationFence(mapper);

    private final UUID userId = UUID.randomUUID();

    @AfterEach
    void clearTransactionFlag() {
        TransactionSynchronizationManager.setActualTransactionActive(false);
    }

    @Test
    void rejectsMissingTransactionBeforeDatabaseAccess() {
        assertThrows(IllegalStateException.class, () -> fence.lockUser(userId));
        verifyNoInteractions(mapper);
    }

    @Test
    void rejectsMissingOrDisabledUser() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        assertThrows(AccessDeniedException.class, () -> fence.lockUser(null));
        when(mapper.selectActiveByIdsForUpdate(List.of(userId))).thenReturn(List.of());
        assertThrows(AccessDeniedException.class, () -> fence.lockUser(userId));
        var disabled = new User();
        disabled.setStatus(UserStatus.DISABLED);
        when(mapper.selectActiveByIdsForUpdate(List.of(userId))).thenReturn(List.of(disabled));
        assertThrows(AccessDeniedException.class, () -> fence.lockUser(userId));
    }

    @Test
    void acceptsActiveUserAndPropagatesDatabaseFailure() {
        TransactionSynchronizationManager.setActualTransactionActive(true);
        var active = new User();
        active.setStatus(UserStatus.ACTIVE);
        when(mapper.selectActiveByIdsForUpdate(List.of(userId))).thenReturn(List.of(active));
        fence.lockUser(userId);
        var failure = new IllegalStateException("synthetic row lock failure");
        when(mapper.selectActiveByIdsForUpdate(List.of(userId))).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class, () -> fence.lockUser(userId)));
    }
}
