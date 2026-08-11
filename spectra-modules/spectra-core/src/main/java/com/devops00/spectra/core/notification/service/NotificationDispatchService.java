package com.devops00.spectra.core.notification.service;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.devops00.spectra.core.notification.javabean.entity.NotificationRequest;

/** 统一通知发送编排服务。 */
public interface NotificationDispatchService {

    /**
     * 接受通知请求并按渠道拆分任务。
     *
     * @param request 通知请求
     * @param recipientUserIds 收件人
     * @param channel 渠道
     * @param address 外部渠道地址
     * @param titleTemplate 标题模板
     * @param contentTemplate 正文模板
     * @param variables 模板变量
     */
    void dispatch(NotificationRequest request, List<UUID> recipientUserIds, String channel, String address,
            String titleTemplate, String contentTemplate, Map<String, ?> variables);
}
