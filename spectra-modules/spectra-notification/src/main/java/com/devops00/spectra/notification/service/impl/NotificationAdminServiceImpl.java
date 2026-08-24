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
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.notification.javabean.converter.NotificationAdminConverter;
import com.devops00.spectra.notification.javabean.domain.NotificationDeliveryStatus;
import com.devops00.spectra.notification.javabean.domain.NotificationRequestStatus;
import com.devops00.spectra.notification.javabean.domain.NotificationTaskStatus;
import com.devops00.spectra.notification.javabean.entity.NotificationDeliveryEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationRequestEntity;
import com.devops00.spectra.notification.javabean.entity.NotificationTaskEntity;
import com.devops00.spectra.notification.javabean.from.NotificationAdminQueryFrom;
import com.devops00.spectra.notification.javabean.from.NotificationOverviewFrom;
import com.devops00.spectra.notification.javabean.vo.NotificationDeliveryAdminVO;
import com.devops00.spectra.notification.javabean.vo.NotificationOverviewVO;
import com.devops00.spectra.notification.javabean.vo.NotificationOverviewTrendVO;
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

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    /** 默认统计窗口。 */
    private static final int DEFAULT_OVERVIEW_HOURS = 24;
    /** 管理概览允许的最大统计窗口。 */
    private static final int MAX_OVERVIEW_HOURS = 7 * 24;
    /** 最近错误最多返回的条数。 */
    private static final int RECENT_ERROR_LIMIT = 10;
    /** 管理分页查询允许的最大时间窗口。 */
    private static final Duration MAX_QUERY_RANGE = Duration.ofDays(31);
    /** 当前排队中的任务状态。 */
    private static final Set<String> QUEUED_STATUSES = Set.of(
            NotificationTaskStatus.PENDING.name(), NotificationTaskStatus.RETRYING.name());
    /** 当前失败任务状态。 */
    private static final Set<String> FAILED_TASK_STATUSES = Set.of(
            NotificationTaskStatus.FAILED.name(), NotificationTaskStatus.BLOCKED.name());
    /** 成功投递结果。 */
    private static final Set<String> SUCCESS_DELIVERY_STATUSES = Set.of(
            NotificationDeliveryStatus.ACCEPTED.name(), NotificationDeliveryStatus.SENT.name());
    /** 失败投递结果。 */
    private static final Set<String> FAILED_DELIVERY_STATUSES = Set.of(
            NotificationDeliveryStatus.FAILED.name(), NotificationDeliveryStatus.BLOCKED.name());

    /**
     * 允许人工重新排队的终态。
     */
    private static final Set<String> RETRYABLE_STATUSES = Set.of(
            NotificationTaskStatus.FAILED.name(),
            NotificationTaskStatus.BLOCKED.name(),
            NotificationTaskStatus.UNKNOWN.name());
    /**
     * 允许取消的处理中状态。
     */
    private static final Set<String> CANCELLABLE_STATUSES = Set.of(
            NotificationTaskStatus.PENDING.name(),
            NotificationTaskStatus.RETRYING.name(),
            NotificationTaskStatus.PROCESSING.name());

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

    /** 用户时区时间转换器。 */
    private final TimeMapper timeMapper;

    /**
     * 查询通知运行概览。
     */
    @Override
    public NotificationOverviewVO overview(NotificationOverviewFrom from) {
        var hours = resolveOverviewHours(from);
        var to = Instant.now();
        var start = to.minus(hours, ChronoUnit.HOURS);
        var pendingTaskCount = countTasks(QUEUED_STATUSES, null);
        var processingTaskCount = countTasks(Set.of(NotificationTaskStatus.PROCESSING.name()), null);
        var oldestPendingTask = taskMapper.selectOne(new LambdaQueryWrapper<NotificationTaskEntity>()
                .in(NotificationTaskEntity::getStatus, QUEUED_STATUSES)
                .orderByAsc(NotificationTaskEntity::getScheduledAt)
                .last("LIMIT 1"));
        var failedTaskCount = countTasks(FAILED_TASK_STATUSES, null);
        var unknownTaskCount = countTasks(Set.of(NotificationTaskStatus.UNKNOWN.name()), null);
        var deliveryCount = countDeliveries(null, start, to);
        var successfulDeliveryCount = countDeliveries(SUCCESS_DELIVERY_STATUSES, start, to);
        var failedDeliveryCount = countDeliveries(FAILED_DELIVERY_STATUSES, start, to);
        var unknownDeliveryCount = countDeliveries(Set.of(NotificationDeliveryStatus.UNKNOWN.name()), start, to);
        var trendRows = deliveryMapper.selectOverviewTrend(start, to);
        var recentErrorRows = deliveryMapper.selectRecentErrors(start, to, RECENT_ERROR_LIMIT);

        var channels = List.of(NotificationChannel.values())
                .stream()
                .map(channel -> NotificationOverviewVO.ChannelSummary.builder()
                        .availability(availability(channel))
                        .pendingTaskCount(countTasks(QUEUED_STATUSES, channel.name()))
                        .failedTaskCount(countTasks(FAILED_TASK_STATUSES, channel.name()))
                        .unknownTaskCount(countTasks(Set.of(NotificationTaskStatus.UNKNOWN.name()), channel.name()))
                        .build())
                .toList();
        var trend = fillTrend(start, to, trendRows);
        var recentErrors = recentErrorRows == null
                ? List.<NotificationOverviewVO.ErrorSummary>of()
                : recentErrorRows.stream()
                        .map(item -> NotificationOverviewVO.ErrorSummary.builder()
                                .occurredAt(item.getOccurredAt())
                                .channel(item.getChannel())
                                .status(item.getStatus())
                                .errorCode(item.getErrorCode())
                                .message(item.getMessage())
                                .build())
                        .toList();
        return NotificationOverviewVO.builder()
                .generatedAt(timeMapper.toLocalDateTime(to))
                .rangeHours(hours)
                .pendingTaskCount(pendingTaskCount)
                .processingTaskCount(processingTaskCount)
                .oldestPendingTaskAt(oldestPendingTask == null
                        ? null
                        : timeMapper.toLocalDateTime(oldestPendingTask.getScheduledAt()))
                .failedTaskCount(failedTaskCount)
                .unknownTaskCount(unknownTaskCount)
                .deliveryCount(deliveryCount)
                .successfulDeliveryCount(successfulDeliveryCount)
                .failedDeliveryCount(failedDeliveryCount)
                .unknownDeliveryCount(unknownDeliveryCount)
                .failureRate(deliveryCount == 0L ? 0D : failedDeliveryCount * 100D / deliveryCount)
                .channels(channels)
                .trend(trend)
                .recentErrors(recentErrors)
                .build();
    }

    /**
     * 查询渠道健康状态。
     */
    @Override
    public NotificationChannelAvailability availability(NotificationChannel channel) {
        return notificationGateway.availability(channel);
    }

    /**
     * 查询逻辑通知请求的脱敏详情摘要。
     */
    @Override
    public NotificationRequestAdminVO getRequest(UUID requestId) {
        var entity = requestMapper.selectById(requestId);
        if (entity == null) {
            throw new DataNotExistException("通知请求不存在");
        }
        return converter.toRequestVO(entity);
    }

    /**
     * 查询通知投递任务的脱敏详情摘要。
     */
    @Override
    public NotificationTaskAdminVO getTask(UUID taskId) {
        return converter.toTaskVO(getTaskEntity(taskId));
    }

    /**
     * 查询通知投递记录的脱敏详情摘要。
     */
    @Override
    public NotificationDeliveryAdminVO getDelivery(UUID deliveryId) {
        var entity = deliveryMapper.selectById(deliveryId);
        if (entity == null) {
            throw new DataNotExistException("通知投递记录不存在");
        }
        var task = taskMapper.selectById(entity.getNotificationTaskId());
        if (task != null) {
            entity.setChannel(task.getChannel());
        }
        return converter.toDeliveryVO(entity);
    }

    /**
     * 统计任务状态和渠道；渠道参数为空时统计全部渠道。
     */
    private long countTasks(Set<String> statuses, String channel) {
        var wrapper = new LambdaQueryWrapper<NotificationTaskEntity>().in(NotificationTaskEntity::getStatus, statuses);
        if (StringUtils.hasText(channel)) {
            wrapper.eq(NotificationTaskEntity::getChannel, channel);
        }
        return taskMapper.selectCount(wrapper);
    }

    /**
     * 统计时间窗口内的投递结果；状态为空时统计所有结果。
     */
    private long countDeliveries(Set<String> statuses, Instant from, Instant to) {
        var wrapper = new LambdaQueryWrapper<NotificationDeliveryEntity>()
                .ge(NotificationDeliveryEntity::getCreatedAt, from)
                .lt(NotificationDeliveryEntity::getCreatedAt, to);
        if (statuses != null && !statuses.isEmpty()) {
            wrapper.in(NotificationDeliveryEntity::getResultStatus, statuses);
        }
        return deliveryMapper.selectCount(wrapper);
    }

    /**
     * 补齐没有投递记录的小时桶，保证前端趋势图时间轴连续。
     */
    private List<NotificationOverviewVO.TrendPoint> fillTrend(Instant from, Instant to,
                                                              List<NotificationOverviewTrendVO> rows) {
        var byBucket = rows == null
                ? Map.<Instant, NotificationOverviewTrendVO>of()
                : rows.stream()
                        .collect(Collectors.toMap(NotificationOverviewTrendVO::getBucketAt,
                                Function.identity()));
        var result = new ArrayList<NotificationOverviewVO.TrendPoint>();
        for (var bucket = from.truncatedTo(ChronoUnit.HOURS); bucket.isBefore(to); bucket = bucket.plus(1, ChronoUnit.HOURS)) {
            var row = byBucket.get(bucket);
            result.add(NotificationOverviewVO.TrendPoint.builder()
                    .bucketAt(timeMapper.toLocalDateTime(bucket))
                    .totalCount(row == null ? 0L : row.getTotalCount())
                    .successCount(row == null ? 0L : row.getSuccessCount())
                    .failedCount(row == null ? 0L : row.getFailedCount())
                    .unknownCount(row == null ? 0L : row.getUnknownCount())
                    .build());
        }
        return List.copyOf(result);
    }

    /**
     * 解析并限制概览时间窗口。
     */
    private int resolveOverviewHours(NotificationOverviewFrom from) {
        var hours = from == null || from.getHours() == null ? DEFAULT_OVERVIEW_HOURS : from.getHours();
        if (hours < 1 || hours > MAX_OVERVIEW_HOURS) {
            throw new DataSaveException("通知运行概览时间范围必须在 1 到 168 小时之间");
        }
        return hours;
    }

    /**
     * 解析管理分页时间范围；未传条件时默认查询最近 31 天。
     */
    private QueryRange resolveQueryRange(NotificationAdminQueryFrom params) {
        if (params != null
                && (params.getRequestId() != null || params.getTaskId() != null)
                && !StringUtils.hasText(params.getStartTime())
                && !StringUtils.hasText(params.getEndTime())) {
            return new QueryRange(null, null);
        }
        var to = params != null && StringUtils.hasText(params.getEndTime()) ? timeMapper.toInstant(params.getEndTime()) : Instant.now();
        var from = params != null && StringUtils.hasText(params.getStartTime())
                ? timeMapper.toInstant(params.getStartTime())
                : to.minus(MAX_QUERY_RANGE);
        if (!from.isBefore(to) || Duration.between(from, to).compareTo(MAX_QUERY_RANGE) > 0) {
            throw new DataSaveException("通知管理查询时间范围必须在 31 天以内");
        }
        return new QueryRange(from, to);
    }

    /** 管理分页查询时间范围。 */
    private record QueryRange(Instant from, Instant to) {
    }

    /**
     * 分页查询通知请求。
     */
    @Override
    public IPage<NotificationRequestAdminVO> pageRequests(PageFrom page, NotificationAdminQueryFrom params) {
        var range = resolveQueryRange(params);
        var wrapper = new LambdaQueryWrapper<NotificationRequestEntity>().orderByDesc(NotificationRequestEntity::getCreatedAt);
        if (range.from() != null) {
            wrapper.ge(NotificationRequestEntity::getCreatedAt, range.from())
                    .lt(NotificationRequestEntity::getCreatedAt, range.to());
        }
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
        var range = resolveQueryRange(params);
        var wrapper = new LambdaQueryWrapper<NotificationTaskEntity>().orderByDesc(NotificationTaskEntity::getCreatedAt);
        if (range.from() != null) {
            wrapper.ge(NotificationTaskEntity::getCreatedAt, range.from())
                    .lt(NotificationTaskEntity::getCreatedAt, range.to());
        }
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
        var range = resolveQueryRange(params);
        return converter.toDeliveryPage(deliveryMapper.selectAdminPage(page.toPage(), range.from(), range.to(),
                params == null ? null : params.getRequestId(), params == null ? null : params.getTaskId(),
                params == null ? null : params.getRecipientUserId(), params == null ? null : params.getStatus(),
                params == null ? null : params.getChannel()));
    }

    /**
     * 将失败或阻断任务重新置为可处理状态。
     */
    @Override
    @Transactional
    public void retry(UUID taskId) {
        var task = getTaskEntity(taskId);
        if (!RETRYABLE_STATUSES.contains(task.getStatus())) {
            throw new DataSaveException("当前通知任务不可重试");
        }
        var updated = taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, taskId)
                .in(NotificationTaskEntity::getStatus, RETRYABLE_STATUSES)
                .set(NotificationTaskEntity::getStatus, NotificationTaskStatus.RETRYING.name())
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
                .notIn(NotificationRequestEntity::getStatus,
                        List.of(NotificationRequestStatus.CANCELLED.name(), NotificationRequestStatus.EXPIRED.name()))
                .set(NotificationRequestEntity::getStatus, NotificationRequestStatus.DISPATCHING.name())
                .set(NotificationRequestEntity::getUpdatedAt, Instant.now()));
        log.info("已重新排队通知任务: taskId={}", taskId);
    }

    /**
     * 取消尚未完成的任务，并同步请求状态。
     */
    @Override
    @Transactional
    public void cancel(UUID taskId) {
        var task = getTaskEntity(taskId);
        if (!CANCELLABLE_STATUSES.contains(task.getStatus())) {
            throw new DataSaveException("当前通知任务不可取消");
        }
        var updated = taskMapper.update(null, new LambdaUpdateWrapper<NotificationTaskEntity>()
                .eq(NotificationTaskEntity::getId, taskId)
                .in(NotificationTaskEntity::getStatus, CANCELLABLE_STATUSES)
                .set(NotificationTaskEntity::getStatus, NotificationTaskStatus.CANCELLED.name())
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
    private NotificationTaskEntity getTaskEntity(UUID taskId) {
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
        var status = tasks.stream().allMatch(task -> NotificationTaskStatus.CANCELLED.name().equals(task.getStatus()))
                ? NotificationRequestStatus.CANCELLED.name()
                : NotificationRequestStatus.DISPATCHING.name();
        requestMapper.update(null, new LambdaUpdateWrapper<NotificationRequestEntity>()
                .eq(NotificationRequestEntity::getId, requestId)
                .notIn(NotificationRequestEntity::getStatus, List.of(NotificationRequestStatus.EXPIRED.name()))
                .set(NotificationRequestEntity::getStatus, status)
                .set(NotificationRequestEntity::getUpdatedAt, Instant.now()));
    }
}
