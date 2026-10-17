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

package com.devops00.spectra.core.upload.service;

import org.junit.jupiter.api.Test;
import org.springframework.core.task.TaskExecutor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证过期校验会话会被重新投递，不依赖原始进程内线程池任务。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class UploadVerificationRecoveryServiceTest {

    private static final Instant NOW = Instant.parse("2026-10-16T00:00:00Z");

    @Test
    void staleSessionsAreRedispatched() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        var transactions = mock(FileUploadCleanupTransactionService.class);
        when(transactions.claimStaleVerifications(NOW.minus(UploadVerificationRecoveryService.LEASE), NOW, 100))
                .thenReturn(List.of(first, second));
        var worker = mock(UploadVerificationWorker.class);
        var executor = mock(TaskExecutor.class);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(executor).execute(any(Runnable.class));

        int dispatched = new UploadVerificationRecoveryService(transactions, worker, executor).recoverStale(NOW);

        assertEquals(2, dispatched);
        verify(worker).verify(first);
        verify(worker).verify(second);
    }

    @Test
    void noCandidatesDispatchNothing() {
        var transactions = mock(FileUploadCleanupTransactionService.class);
        when(transactions.claimStaleVerifications(any(), eq(NOW), eq(100))).thenReturn(List.of());
        var worker = mock(UploadVerificationWorker.class);
        var executor = mock(TaskExecutor.class);

        int dispatched = new UploadVerificationRecoveryService(transactions, worker, executor).recoverStale(NOW);

        assertEquals(0, dispatched);
        verify(worker, never()).verify(any());
    }
}
