package com.devops00.spectra.core.notification.service.impl;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.notification.constant.NotificationChannel;
import com.devops00.spectra.core.notification.javabean.entity.NotificationRequest;
import com.devops00.spectra.core.notification.service.NotificationDispatchService;
import com.devops00.spectra.core.notification.service.NotificationInboxService;
import com.devops00.spectra.core.notification.service.NotificationTaskService;
import com.devops00.spectra.core.notification.service.NotificationTemplateRenderer;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 统一通知发送编排服务实现。 */
@Service
@RequiredArgsConstructor
public class NotificationDispatchServiceImpl implements NotificationDispatchService {

    private final NotificationTaskService notificationTaskService;
    private final NotificationInboxService notificationInboxService;
    private final NotificationTemplateRenderer notificationTemplateRenderer;

    @Override
    @Transactional
    public void dispatch(NotificationRequest request, List<UUID> recipientUserIds, String channel, String address,
            String titleTemplate, String contentTemplate, Map<String, ?> variables) {
        if (request == null || !StringUtils.hasText(channel) || !StringUtils.hasText(titleTemplate)
                || !StringUtils.hasText(contentTemplate)) {
            throw new DataSaveException("通知发送参数不完整");
        }
        final NotificationChannel notificationChannel;
        try {
            notificationChannel = NotificationChannel.valueOf(channel.toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new DataSaveException("不支持的通知渠道");
        }
        var title = notificationTemplateRenderer.render(titleTemplate, variables);
        var content = notificationTemplateRenderer.render(contentTemplate, variables);
        notificationTaskService.split(request.getId(), request.getTenantId(), recipientUserIds,
                notificationChannel.name(), address);
        if (notificationChannel == NotificationChannel.INBOX) {
            notificationInboxService.publish(request.getId(), request.getTenantId(), recipientUserIds, title, content);
        }
    }
}
