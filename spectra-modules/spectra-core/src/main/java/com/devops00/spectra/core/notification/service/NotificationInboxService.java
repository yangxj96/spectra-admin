package com.devops00.spectra.core.notification.service;

import java.util.List;
import java.util.UUID;

import com.devops00.spectra.core.notification.javabean.entity.NotificationInbox;

/** 站内信写入服务。 */
public interface NotificationInboxService {

    /** 批量写入站内信收件箱。 */
    List<NotificationInbox> publish(UUID requestId, UUID tenantId, List<UUID> recipientUserIds, String title, String content);
}
