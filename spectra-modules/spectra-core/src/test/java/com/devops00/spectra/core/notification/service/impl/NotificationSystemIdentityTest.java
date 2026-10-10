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

package com.devops00.spectra.core.notification.service.impl;

import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationChannelAvailability;
import com.devops00.spectra.common.notification.NotificationDirectAddress;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.common.notification.NotificationPurpose;
import com.devops00.spectra.common.notification.NotificationReceipt;
import com.devops00.spectra.common.notification.NotificationRequest;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationSystemActor;
import com.devops00.spectra.common.notification.NotificationTemplateCode;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 后台通知身份只允许已登记的用途与明确收件人。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/09
 */
class NotificationSystemIdentityTest {

    @Test
    void workflowTaskIdentityRoutesAnExplicitRecipientThroughSystemGateway() {
        var gateway = mock(NotificationGateway.class);
        var service = new NotificationServiceImpl(gateway);
        var recipientId = UUID.randomUUID();
        var request = request(NotificationPurpose.WORKFLOW_TODO, NotificationTemplateCode.WORKFLOW_TASK_TODO,
                "WORKFLOW", recipientId);
        var receipt = new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false);
        when(gateway.availability(NotificationChannel.IN_APP))
                .thenReturn(new NotificationChannelAvailability(NotificationChannel.IN_APP, true, "AVAILABLE"));
        when(gateway.enqueueAsSystem(any(NotificationRequest.class), eq(NotificationSystemActor.WORKFLOW_TASK)))
                .thenReturn(receipt);

        assertEquals(receipt, service.sendAsSystem(request, NotificationSystemActor.WORKFLOW_TASK));
        verify(gateway).enqueueAsSystem(argThat(value -> value.recipientUserIds().equals(List.of(recipientId))
                && value.purpose() == NotificationPurpose.WORKFLOW_TODO), eq(NotificationSystemActor.WORKFLOW_TASK));
    }

    @Test
    void mismatchedSystemIdentityFailsBeforeAvailabilityOrRecipientAccess() {
        var gateway = mock(NotificationGateway.class);
        var service = new NotificationServiceImpl(gateway);
        var recipientId = UUID.randomUUID();

        assertThrows(AccessDeniedException.class, () -> service.sendAsSystem(
                request(NotificationPurpose.WORKFLOW_TODO, NotificationTemplateCode.WORKFLOW_TASK_TODO,
                        "WORKFLOW", recipientId),
                null));
        assertThrows(AccessDeniedException.class, () -> service.sendAsSystem(
                request(NotificationPurpose.WORKFLOW_TODO, NotificationTemplateCode.WORKFLOW_TASK_TODO,
                        "OA", recipientId),
                NotificationSystemActor.WORKFLOW_TASK));
        assertThrows(AccessDeniedException.class, () -> service.sendAsSystem(
                request(NotificationPurpose.WORKFLOW_RESULT, NotificationTemplateCode.WORKFLOW_TASK_TODO,
                        "WORKFLOW", recipientId),
                NotificationSystemActor.WORKFLOW_TASK));
        assertThrows(AccessDeniedException.class, () -> service.sendAsSystem(
                request(NotificationPurpose.WORKFLOW_TODO, NotificationTemplateCode.WORKFLOW_TASK_TODO,
                        "WORKFLOW", null),
                NotificationSystemActor.WORKFLOW_TASK));
        var directAddress = NotificationSendRequest.direct("synthetic-task", NotificationPurpose.WORKFLOW_TODO,
                List.of(new NotificationDirectAddress(NotificationChannel.EMAIL, "synthetic@example.invalid")),
                NotificationTemplateCode.WORKFLOW_TASK_TODO)
                .sourceModule("WORKFLOW")
                .build();
        assertThrows(AccessDeniedException.class,
                () -> service.sendAsSystem(directAddress, NotificationSystemActor.WORKFLOW_TASK));
        verifyNoInteractions(gateway);
    }

    @Test
    void eachRegisteredBackgroundCallerHasOnlyItsOwnPurposeAndTemplate() {
        var gateway = mock(NotificationGateway.class);
        var service = new NotificationServiceImpl(gateway);
        var recipientId = UUID.randomUUID();
        var receipt = new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false);
        when(gateway.availability(NotificationChannel.IN_APP))
                .thenReturn(new NotificationChannelAvailability(NotificationChannel.IN_APP, true, "AVAILABLE"));
        when(gateway.enqueueAsSystem(any(NotificationRequest.class), any(NotificationSystemActor.class)))
                .thenReturn(receipt);

        assertEquals(receipt, service.sendAsSystem(request(NotificationPurpose.WORKFLOW_RESULT,
                NotificationTemplateCode.WORKFLOW_TASK_RESULT, "WORKFLOW", recipientId),
                NotificationSystemActor.WORKFLOW_TASK));
        assertEquals(receipt, service.sendAsSystem(request(NotificationPurpose.OA_NOTICE,
                NotificationTemplateCode.OA_APPLICATION_STATUS, "OA", recipientId),
                NotificationSystemActor.OA_WORKFLOW_RESULT));
        assertEquals(receipt, service.sendAsSystem(request(NotificationPurpose.OA_REMINDER,
                NotificationTemplateCode.OA_CONTRACT_MILESTONE_REMINDER, "OA", recipientId),
                NotificationSystemActor.OA_CONTRACT_REMINDER));
        assertEquals(receipt, service.sendAsSystem(request(NotificationPurpose.SYSTEM_NOTICE,
                NotificationTemplateCode.SYSTEM_SERVICE_MONITOR_ALERT, "spectra-core", recipientId),
                NotificationSystemActor.SERVICE_MONITOR_ALERT));
    }

    private static NotificationSendRequest request(NotificationPurpose purpose, String templateCode,
                                                   String sourceModule, UUID recipientId) {
        return NotificationSendRequest.inApp("synthetic-task", purpose,
                recipientId == null ? List.of() : List.of(recipientId), templateCode)
                .sourceModule(sourceModule)
                .build();
    }
}
