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

package com.devops00.spectra.notification.dispatch;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.devops00.spectra.notification.javabean.domain.NotificationRequestStatus;
import com.devops00.spectra.notification.javabean.domain.NotificationTaskStatus;
import com.devops00.spectra.notification.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

/**
 * 根据投递任务的真实状态统一刷新逻辑通知请求状态。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/24
 */
@Component
@RequiredArgsConstructor
public class NotificationRequestStatusUpdater {

    private static final List<String> OPEN_TASK_STATUSES = List.of(
            NotificationTaskStatus.PENDING.name(),
            NotificationTaskStatus.RETRYING.name(),
            NotificationTaskStatus.PROCESSING.name());

    private static final List<String> TERMINAL_TASK_STATUSES = List.of(
            NotificationTaskStatus.SENT.name(),
            NotificationTaskStatus.FAILED.name(),
            NotificationTaskStatus.BLOCKED.name(),
            NotificationTaskStatus.UNKNOWN.name(),
            NotificationTaskStatus.EXPIRED.name(),
            NotificationTaskStatus.CANCELLED.name());

    private static final List<String> IMMUTABLE_REQUEST_STATUSES = List.of(
            NotificationRequestStatus.CANCELLED.name(), NotificationRequestStatus.EXPIRED.name());

    private final NotificationTaskMapper taskMapper;

    private final NotificationRequestMapper requestMapper;

    /**
     * 汇总指定请求的任务状态并更新请求。
     *
     * @param requestId 逻辑通知请求 ID
     */
    public void refresh(UUID requestId) {
        if (requestId == null) {
            return;
        }
        var tasks = taskMapper.selectList(new LambdaQueryWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getNotificationRequestId, requestId));
        if (tasks.isEmpty()) {
            return;
        }
        var hasOpen = tasks.stream().anyMatch(task -> OPEN_TASK_STATUSES.contains(task.getStatus()));
        var sentCount = tasks.stream()
                .filter(task -> NotificationTaskStatus.SENT.name().equals(task.getStatus()))
                .count();
        var terminalCount = tasks.stream().filter(task -> TERMINAL_TASK_STATUSES.contains(task.getStatus())).count();
        var status = hasOpen
                ? NotificationRequestStatus.DISPATCHING.name()
                : sentCount == tasks.size()
                        ? NotificationRequestStatus.SUCCEEDED.name()
                        : sentCount > 0
                                ? NotificationRequestStatus.PARTIAL.name()
                                : tasks.stream()
                                        .allMatch(task -> NotificationTaskStatus.CANCELLED.name()
                                                .equals(task.getStatus()))
                                                        ? NotificationRequestStatus.CANCELLED.name()
                                                        : tasks.stream()
                                                                .allMatch(task -> NotificationTaskStatus.EXPIRED.name()
                                                                        .equals(task.getStatus()))
                                                                                ? NotificationRequestStatus.EXPIRED.name()
                                                                                : terminalCount == tasks.size()
                                                                                        ? NotificationRequestStatus.FAILED.name()
                                                                                        : NotificationRequestStatus.DISPATCHING.name();
        requestMapper.update(null, new LambdaUpdateWrapper<NotificationRequestEntity>()
                .eq(NotificationRequestEntity::getId, requestId)
                .notIn(NotificationRequestEntity::getStatus, IMMUTABLE_REQUEST_STATUSES)
                .set(NotificationRequestEntity::getStatus, status)
                .set(NotificationRequestEntity::getUpdatedAt, Instant.now()));
    }
}
