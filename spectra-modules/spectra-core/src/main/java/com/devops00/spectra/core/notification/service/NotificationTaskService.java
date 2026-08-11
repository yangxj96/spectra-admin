package com.devops00.spectra.core.notification.service;

import java.util.List;
import java.util.UUID;

import com.devops00.spectra.core.notification.javabean.entity.NotificationTask;

/** 通知请求拆分为收件人/渠道任务的服务。 */
public interface NotificationTaskService {

    /**
     * 为每个收件人和渠道创建一个独立任务。
     *
     * @param requestId 通知请求
     * @param tenantId 租户
     * @param recipientUserIds 收件人用户
     * @param channel 渠道
     * @param address 外部渠道地址，可为空
     * @return 创建的任务
     */
    List<NotificationTask> split(UUID requestId, UUID tenantId, List<UUID> recipientUserIds, String channel, String address,
            String title, String content);
}
