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

import com.devops00.spectra.common.notification.NotificationRecipientDirectory;
import com.devops00.spectra.common.notification.NotificationSendRequest;
import com.devops00.spectra.common.notification.NotificationService;
import com.devops00.spectra.common.notification.NotificationTemplateCode;
import lombok.RequiredArgsConstructor;
import org.flowable.engine.TaskService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * 流程启动和任务推进共用的待办通知端口。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/20
 */
@Service
@RequiredArgsConstructor
public class WorkflowTaskTodoNotifier {

    private final TaskService flowableTaskService;

    private final NotificationRecipientDirectory recipientDirectory;

    private final NotificationService notificationService;

    /**
     * 为当前活动且有明确办理人的任务写入待办通知；目录或命令失败直接向上抛出。
     */
    public void notifyActiveTasks(String processInstanceId) {
        flowableTaskService.createTaskQuery().processInstanceId(processInstanceId).list().forEach(task -> {
            if (!StringUtils.hasText(task.getAssignee())) {
                return;
            }
            var recipients = recipientDirectory.resolveByLoginNames(List.of(task.getAssignee()))
                    .stream()
                    .filter(item -> item.active() && item.userId() != null)
                    .map(item -> item.userId())
                    .distinct()
                    .toList();
            if (recipients.isEmpty()) {
                return;
            }
            notificationService.send(NotificationSendRequest.inApp(
                    NotificationTemplateCode.WORKFLOW_TASK_TODO,
                    recipients)
                    .parameter("task_name", task.getName())
                    .businessReference("WORKFLOW", processInstanceId)
                    .sourceModule("WORKFLOW")
                    .link("/workflow/tasks/todo")
                    .build());
        });
    }
}
