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

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.core.security.authentication.javabean.entity.PasswordCredential;
import com.devops00.spectra.core.security.authentication.mapper.PasswordCredentialMapper;
import com.devops00.spectra.core.security.root.LastEffectiveDevOpsGuard;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PasswordCredentialServiceClockTest {

    private static final Instant NOW = Instant.parse("2026-10-11T12:00:00Z");

    @Test
    void createUsesInjectedClockAndPreservesFirstChangeRequirement() {
        var mapper = mock(PasswordCredentialMapper.class);
        var guard = mock(LastEffectiveDevOpsGuard.class);
        when(mapper.insert(any(PasswordCredential.class))).thenReturn(1);
        var userId = UUID.randomUUID();

        service(mapper, guard).createOrReplace(userId, "synthetic-hash", true);

        var inserted = ArgumentCaptor.forClass(PasswordCredential.class);
        verify(mapper).insert(inserted.capture());
        assertEquals(userId, inserted.getValue().getUserId());
        assertEquals(NOW, inserted.getValue().getChangedAt());
        assertTrue(inserted.getValue().getMustChange());
        assertNull(inserted.getValue().getExpiresAt());
    }

    @Test
    void resetAndChangeRefreshAgeAndClearOldExpiryAndLock() {
        var mapper = mock(PasswordCredentialMapper.class);
        var guard = mock(LastEffectiveDevOpsGuard.class);
        var existing = oldCredential();
        when(mapper.selectOne(any())).thenReturn(existing);
        when(mapper.updateById(any(PasswordCredential.class))).thenReturn(1);
        var service = service(mapper, guard);

        service.updatePassword(existing.getUserId(), "reset-hash", true, null);
        assertEquals(NOW, existing.getChangedAt());
        assertEquals("reset-hash", existing.getPasswordHash());
        assertTrue(existing.getMustChange());
        assertNull(existing.getExpiresAt());
        assertNull(existing.getLockedUntil());
        assertEquals(0, existing.getFailedAttempts());

        service.updatePassword(existing.getUserId(), "changed-hash", false, null);
        assertEquals(NOW, existing.getChangedAt());
        assertEquals("changed-hash", existing.getPasswordHash());
        assertFalse(existing.getMustChange());
        var order = inOrder(guard, mapper);
        order.verify(guard).lockForChange();
        order.verify(mapper).updateById(existing);
        order.verify(guard).assertWithinLimits(null);
        order.verify(guard).lockForChange();
        order.verify(mapper).updateById(existing);
        order.verify(guard).assertWithinLimits(null);
    }

    @Test
    void failedWritesDoNotClaimSuccessOrCompleteRootCheck() {
        var mapper = mock(PasswordCredentialMapper.class);
        var guard = mock(LastEffectiveDevOpsGuard.class);
        var service = service(mapper, guard);
        assertThrows(DataSaveException.class,
                () -> service.createOrReplace(UUID.randomUUID(), "synthetic-hash", true));

        var existing = oldCredential();
        when(mapper.selectOne(any())).thenReturn(existing);
        assertThrows(EntityUpdateException.class,
                () -> service.updatePassword(existing.getUserId(), "synthetic-hash", false, null));
        verify(guard, never()).assertWithinLimits(any());
    }

    private PasswordCredentialServiceImpl service(PasswordCredentialMapper mapper, LastEffectiveDevOpsGuard guard) {
        return new PasswordCredentialServiceImpl(mapper, guard, Clock.fixed(NOW, ZoneOffset.UTC));
    }

    private PasswordCredential oldCredential() {
        var credential = new PasswordCredential();
        credential.setUserId(UUID.randomUUID());
        credential.setChangedAt(NOW.minusSeconds(172800));
        credential.setExpiresAt(NOW.minusSeconds(1));
        credential.setLockedUntil(NOW.plusSeconds(3600));
        credential.setFailedAttempts(4);
        return credential;
    }
}
