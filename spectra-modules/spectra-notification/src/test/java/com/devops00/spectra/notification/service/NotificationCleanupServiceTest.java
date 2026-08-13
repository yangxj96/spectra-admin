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

package com.devops00.spectra.notification.service;

import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.properties.NotificationCleanupProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 敏感载荷清理任务的开关、批量和匿名计数回归。
 */
class NotificationCleanupServiceTest {

    @Test
    void shouldDoNothingWhenCleanupIsDisabled() {
        var requests = mock(NotificationRequestMapper.class);
        var tasks = mock(NotificationTaskMapper.class);
        var service = new NotificationCleanupService(requests, tasks,
                new NotificationCleanupProperties(false, 1, 10, 60));

        var result = service.cleanupSensitivePayloads();

        assertEquals(new NotificationCleanupService.NotificationCleanupResult(0, 0), result);
        verify(requests, never()).clearSensitivePayloads(any(), any(), anyInt());
        verify(tasks, never()).clearSensitivePayloads(any(), any(), anyInt());
    }

    @Test
    void shouldClearRequestsAndTasksWithConfiguredBatchSize() {
        var requests = mock(NotificationRequestMapper.class);
        var tasks = mock(NotificationTaskMapper.class);
        when(requests.clearSensitivePayloads(any(), any(), anyInt())).thenReturn(3);
        when(tasks.clearSensitivePayloads(any(), any(), anyInt())).thenReturn(2);
        var service = new NotificationCleanupService(requests, tasks,
                new NotificationCleanupProperties(true, 1, 25, 60));

        var result = service.cleanupSensitivePayloads();

        assertEquals(new NotificationCleanupService.NotificationCleanupResult(3, 2), result);
        verify(requests).clearSensitivePayloads(any(), any(), org.mockito.ArgumentMatchers.eq(25));
        verify(tasks).clearSensitivePayloads(any(), any(), org.mockito.ArgumentMatchers.eq(25));
    }
}
