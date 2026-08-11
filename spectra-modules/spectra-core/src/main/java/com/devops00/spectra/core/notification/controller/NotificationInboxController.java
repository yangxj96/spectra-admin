package com.devops00.spectra.core.notification.controller;

import java.util.UUID;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.notification.javabean.entity.NotificationInbox;
import com.devops00.spectra.core.notification.service.NotificationInboxService;
import com.devops00.spectra.security.base.holder.SecUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** 统一通知中心站内信接口。 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/notification-center/inbox")
public class NotificationInboxController {

    /** 当前系统单租户标识；多租户接入时替换为租户上下文。 */
    private static final UUID SYSTEM_TENANT_ID = new UUID(0L, 0L);

    private final NotificationInboxService notificationInboxService;

    /** 分页查询当前用户站内信。 */
    @GetMapping(value = "/page", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public IPage<NotificationInbox> page(PageFrom page, @RequestParam(required = false) Boolean unreadOnly) {
        return notificationInboxService.page(page, SYSTEM_TENANT_ID, currentUserId(), unreadOnly);
    }

    /** 查询当前用户未读数。 */
    @GetMapping(value = "/unread-count", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public long unreadCount() {
        return notificationInboxService.unreadCount(SYSTEM_TENANT_ID, currentUserId());
    }

    /** 标记单条已读。 */
    @PutMapping(value = "/{id}/read", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(@PathVariable UUID id) {
        notificationInboxService.markAsRead(id, SYSTEM_TENANT_ID, currentUserId());
    }

    /** 标记全部已读。 */
    @PutMapping(value = "/read-all", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead() {
        notificationInboxService.markAllAsRead(SYSTEM_TENANT_ID, currentUserId());
    }

    /** 归档单条站内信。 */
    @DeleteMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public void archive(@PathVariable UUID id) {
        notificationInboxService.archive(id, SYSTEM_TENANT_ID, currentUserId());
    }

    private UUID currentUserId() {
        var userId = SecUtil.getCurrentUserId();
        if (userId == null) {
            throw new IllegalStateException("当前用户未登录");
        }
        return userId;
    }
}
