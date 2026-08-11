package com.devops00.spectra.core.notification.service;

import java.util.List;
import java.util.UUID;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;

import com.devops00.spectra.core.notification.javabean.entity.NotificationInbox;

/** 站内信写入服务。 */
public interface NotificationInboxService {

    /** 批量写入站内信收件箱。 */
    List<NotificationInbox> publish(UUID requestId, UUID tenantId, List<UUID> recipientUserIds, String title, String content);

    /** 查询当前用户收件箱。 */
    IPage<NotificationInbox> page(PageFrom page, UUID tenantId, UUID recipientUserId, Boolean unreadOnly);

    /** 查询当前用户未读数。 */
    long unreadCount(UUID tenantId, UUID recipientUserId);

    /** 标记单条已读。 */
    void markAsRead(UUID id, UUID tenantId, UUID recipientUserId);

    /** 标记全部已读。 */
    void markAllAsRead(UUID tenantId, UUID recipientUserId);

    /** 归档单条站内信。 */
    void archive(UUID id, UUID tenantId, UUID recipientUserId);
}
