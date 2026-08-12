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

package com.devops00.spectra.workflow.service.impl;

import com.devops00.spectra.common.notification.*;
import com.devops00.spectra.workflow.javabean.converter.TaskConverter;
import com.devops00.spectra.workflow.service.WorkflowService;
import org.flowable.engine.HistoryService;
import org.flowable.engine.RepositoryService;
import org.flowable.engine.RuntimeService;
import org.flowable.engine.TaskService;
import org.flowable.engine.history.HistoricProcessInstanceQuery;
import org.flowable.task.api.Task;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Workflow 审批结果通知和稳定幂等键回归测试。
 */
class TaskServiceImplNotificationTest {

    @Test
    void shouldEnqueueWorkflowResultAfterCompletingOwnedTask() {
        var flowableTaskService = mock(TaskService.class);
        var historyService = mock(HistoryService.class);
        var repositoryService = mock(RepositoryService.class);
        var runtimeService = mock(RuntimeService.class);
        var workflowService = mock(WorkflowService.class);
        var taskConverter = mock(TaskConverter.class);
        var notificationGateway = mock(NotificationGateway.class);
        var recipientDirectory = mock(NotificationRecipientDirectory.class);
        var taskQuery = mock(TaskQuery.class);
        var historicProcessQuery = mock(HistoricProcessInstanceQuery.class);
        var task = mock(Task.class);
        var userId = UUID.randomUUID();

        when(flowableTaskService.createTaskQuery()).thenReturn(taskQuery);
        when(taskQuery.taskId("task-1")).thenReturn(taskQuery);
        when(taskQuery.taskAssignee("alice")).thenReturn(taskQuery);
        when(taskQuery.singleResult()).thenReturn(task);
        when(taskQuery.processInstanceId("process-1")).thenReturn(taskQuery);
        when(taskQuery.list()).thenReturn(List.of());
        when(task.getId()).thenReturn("task-1");
        when(task.getProcessInstanceId()).thenReturn("process-1");
        when(task.getProcessDefinitionId()).thenReturn("definition-1");
        when(historyService.createHistoricProcessInstanceQuery()).thenReturn(historicProcessQuery);
        when(historicProcessQuery.processInstanceId("process-1")).thenReturn(historicProcessQuery);
        when(historicProcessQuery.singleResult()).thenReturn(null);
        when(recipientDirectory.resolveByLoginNames(List.of("alice")))
                .thenReturn(List.of(new NotificationRecipient(userId, null, null, true, true)));
        when(notificationGateway.enqueue(any(NotificationRequest.class)))
                .thenReturn(new NotificationReceipt(UUID.randomUUID(), "ACCEPTED", 1, false));

        var service = new TaskServiceImpl(flowableTaskService, historyService, repositoryService, runtimeService,
                workflowService, taskConverter, notificationGateway, recipientDirectory);

        service.complete("task-1", "同意", "alice");

        var requestCaptor = org.mockito.ArgumentCaptor.forClass(NotificationRequest.class);
        verify(notificationGateway).enqueue(requestCaptor.capture());
        assertEquals(NotificationPurpose.WORKFLOW_RESULT, requestCaptor.getValue().purpose());
        assertEquals("workflow:result:task-1:true", requestCaptor.getValue().idempotencyKey());
        verify(flowableTaskService).complete(eq("task-1"), anyMap());
    }
}
