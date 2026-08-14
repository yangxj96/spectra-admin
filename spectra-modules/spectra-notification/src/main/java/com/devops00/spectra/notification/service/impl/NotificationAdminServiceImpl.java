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

package com.devops00.spectra.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.notification.NotificationChannel;
import com.devops00.spectra.common.notification.NotificationChannelAvailability;
import com.devops00.spectra.common.notification.NotificationGateway;
import com.devops00.spectra.notification.javabean.converter.NotificationAdminConverter;
import com.devops00.spectra.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.javabean.from.NotificationAdminQueryFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationDeliveryAdminVO;
import com.devops00.spectra.notification.javabean.vo.NotificationRequestAdminVO;
import com.devops00.spectra.notification.javabean.vo.NotificationTaskAdminVO;
import com.devops00.spectra.notification.mapper.NotificationDeliveryMapper;
import com.devops00.spectra.notification.mapper.NotificationRequestMapper;
import com.devops00.spectra.notification.mapper.NotificationTaskMapper;
import com.devops00.spectra.notification.service.NotificationAdminService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * 通知管理端 Service 实现；不返回请求参数、敏感载荷或原始地址。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/8/11
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationAdminServiceImpl implements NotificationAdminService {

    /**
     * 允许人工重新排队的终态。
     */
    private static final Set<String> RETRYABLE_STATUSES = Set.of("FAILED", "BLOCKED", "UNKNOWN");
    /**
     * 允许取消的处理中状态。
     */
    private static final Set<String> CANCELLABLE_STATUSES = Set.of("PENDING", "RETRYING", "PROCESSING");

    /**
     * 通知请求 Mapper。
     */
    private final NotificationRequestMapper requestMapper;
    /**
     * 通知任务 Mapper。
     */
    private final NotificationTaskMapper taskMapper;
    /**
     * 投递记录 Mapper。
     */
    private final NotificationDeliveryMapper deliveryMapper;
    /**
     * 管理视图转换器。
     */
    private final NotificationAdminConverter converter;
    /**
     * 统一通知 Gateway。
     */
    private final NotificationGateway notificationGateway;

    /**
     * 查询渠道健康状态。
     */
    @Override
    public NotificationChannelAvailability availability(NotificationChannel channel) {
        return notificationGateway.availability(channel);
    }

    /**
     * 分页查询通知请求。
     */
    @Override
    public IPage<NotificationRequestAdminVO> pageRequests(PageFrom page, NotificationAdminQueryFrom params) {
        var wrapper = new LambdaQueryWrapper<NotificationRequestEntity>().orderByDesc(NotificationRequestEntity::getCreatedAt);
        if (params != null) {
            if (StringUtils.hasText(params.getStatus())) {
                wrapper.eq(NotificationRequestEntity::getStatus, params.getStatus());
            }
            if (StringUtils.hasText(params.getPurpose())) {
                wrapper.eq(NotificationRequestEntity::getPurpose, params.getPurpose());
            }
            if (StringUtils.hasText(params.getSourceModule())) {
                wrapper.eq(NotificationRequestEntity::getSourceModule, params.getSourceModule());
            }
            if (StringUtils.hasText(params.getBusinessType())) {
                wrapper.eq(NotificationRequestEntity::getBusinessType, params.getBusinessType());
            }
            if (StringUtils.hasText(params.getBusinessId())) {
                wrapper.eq(NotificationRequestEntity::getBusinessId, params.getBusinessId());
            }
            if (params.getRequestId() != null) {
                wrapper.eq(NotificationRequestEntity::getId, params.getRequestId());
            }
        }
        return converter.toRequestPage(requestMapper.selectPage(page.toPage(), wrapper));
    }

    /**
     * 分页查询通知任务。
     */
    @Override
    public IPage<NotificationTaskAdminVO> pageTasks(PageFrom page, NotificationAdminQueryFrom params) {
        var wrapper = new LambdaQueryWrapper<NotificationTaskEntity>().orderByDesc(NotificationTaskEntity::getCreatedAt);
        if (params != null) {
            if (params.getRequestId() != null) {
                wrapper.eq(NotificationTaskEntity::getNotificationRequestId, params.getRequestId());
            }
            if (params.getTaskId() != null) {
                wrapper.eq(NotificationTaskEntity::getId, params.getTaskId());
            }
            if (params.getRecipientUserId() != null) {
                wrapper.eq(NotificationTaskEntity::getReceiverUserId, params.getRecipientUserId());
            }
            if (StringUtils.hasText(params.getStatus())) {
                wrapper.eq(NotificationTaskEntity::getStatus, params.getStatus());
            }
            if (StringUtils.hasText(params.getChannel())) {
                wrapper.eq(NotificationTaskEntity::getChannel, params.getChannel());
            }
            if (StringUtils.hasText(params.getPurpose())) {
                wrapper.eq(NotificationTaskEntity::getPurpose, params.getPurpose());
            }
        }
        return converter.toTaskPage(taskMapper.selectPage(page.toPage(), wrapper));
    }

    /**
     * 分页查询渠道投递记录。
     */
    @Override
    public IPage<NotificationDeliveryAdminVO> pageDeliveries(PageFrom page, NotificationAdminQueryFrom params) {
        var wrapper = new LambdaQueryWrapper<NotificationDeliveryEntity>().orderByDesc(NotificationDeliveryEntity::getCreatedAt);
        if (params != null) {
            if (params.getTaskId() != null) {
                wrapper.eq(NotificationDeliveryEntity::getNotificationTaskId, params.getTaskId());
            }
            if (StringUtils.hasText(params.getStatus())) {
                wrapper.eq(NotificationDeliveryEntity::getResultStatus, params.getStatus());
            }
        }
        return converter.toDeliveryPage(deliveryMapper.selectPage(page.toPage(), wrapper));
    }

    /**
     * 将失败或阻断任务重新置为可处理状态。
     */
    @Override
    @Transactional
    public void retry(UUID taskId) {
        var task = getTask(taskId);
        if (!RETRYABLE_STATUSES.contains(task.getStatus())) {
            throw new DataSaveException("当前通知任务不可重试");
        }
        var updated = taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, taskId)
                .in(NotificationTaskEntity::getStatus, RETRYABLE_STATUSES)
                .set(NotificationTaskEntity::getStatus, "RETRYING")
                .set(NotificationTaskEntity::getAttemptCount, 0)
                .set(NotificationTaskEntity::getLastErrorCode, null)
                .set(NotificationTaskEntity::getScheduledAt, Instant.now())
                .set(NotificationTaskEntity::getNextRetryAt, Instant.now())
                .set(NotificationTaskEntity::getLockedBy, null)
                .set(NotificationTaskEntity::getLockedAt, null));
        if (updated != 1) {
            throw new DataSaveException("重试通知任务失败");
        }
        requestMapper.update(null, new LambdaUpdateWrapper<NotificationRequestEntity>()
                .eq(NotificationRequestEntity::getId, task.getNotificationRequestId())
                .notIn(NotificationRequestEntity::getStatus, List.of("CANCELLED", "EXPIRED"))
                .set(NotificationRequestEntity::getStatus, "DISPATCHING")
                .set(NotificationRequestEntity::getUpdatedAt, Instant.now()));
        log.info("已重新排队通知任务: taskId={}", taskId);
    }

    /**
     * 取消尚未完成的任务，并同步请求状态。
     */
    @Override
    @Transactional
    public void cancel(UUID taskId) {
        var task = getTask(taskId);
        if (!CANCELLABLE_STATUSES.contains(task.getStatus())) {
            throw new DataSaveException("当前通知任务不可取消");
        }
        var updated = taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, taskId)
                .in(NotificationTaskEntity::getStatus, CANCELLABLE_STATUSES)
                .set(NotificationTaskEntity::getStatus, "CANCELLED")
                .set(NotificationTaskEntity::getUpdatedAt, Instant.now()));
        if (updated != 1) {
            throw new DataSaveException("取消通知任务失败");
        }
        updateRequestAfterCancel(task.getNotificationRequestId());
        log.info("已取消通知任务: taskId={}", taskId);
    }

    /**
     * 查询指定通知任务。
     */
    private NotificationTaskEntity getTask(UUID taskId) {
        var task = taskMapper.selectOne(new LambdaQueryWrapper<NotificationTaskEntity>().eq(NotificationTaskEntity::getId, taskId));
        if (task == null) {
            throw new DataNotExistException("通知任务不存在");
        }
        return task;
    }

    /**
     * 根据剩余任务状态刷新取消后的请求状态。
     */
    private void updateRequestAfterCancel(UUID requestId) {
        var tasks = taskMapper.selectList(new LambdaQueryWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getNotificationRequestId, requestId));
        var status = tasks.stream().allMatch(task -> "CANCELLED".equals(task.getStatus())) ? "CANCELLED" : "DISPATCHING";
        requestMapper.update(null, new LambdaUpdateWrapper<NotificationRequestEntity>()
                .eq(NotificationRequestEntity::getId, requestId)
                .notIn(NotificationRequestEntity::getStatus, List.of("EXPIRED"))
                .set(NotificationRequestEntity::getStatus, status)
                .set(NotificationRequestEntity::getUpdatedAt, Instant.now()));
    }
}
