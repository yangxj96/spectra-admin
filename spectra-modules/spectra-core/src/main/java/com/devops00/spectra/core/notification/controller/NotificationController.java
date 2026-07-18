package com.devops00.spectra.core.notification.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.notification.javabean.dto.NotificationBatchSendDTO;
import com.devops00.spectra.core.notification.javabean.dto.NotificationSendDTO;
import com.devops00.spectra.core.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationVO;
import com.devops00.spectra.core.notification.service.NotificationService;
import com.devops00.spectra.log.base.annotation.ULog;
import com.devops00.spectra.security.base.holder.SecUtil;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/// 消息控制器
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Slf4j
@RestController
@RequestMapping("/notification")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    /// 获取消息列表
    @ULog("'查询消息列表'")
    @GetMapping(value = "/list", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public IPage<NotificationVO> list(PageFrom page, NotificationQueryFrom params) {
        var userId = SecUtil.getCurrentUserId();
        return notificationService.page(page, params, userId);
    }

    /// 获取未读数量
    @ULog("'获取未读消息数'")
    @GetMapping(value = "/unread-count", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public long unreadCount() {
        var userId = SecUtil.getCurrentUserId();
        return notificationService.getUnreadCount(userId);
    }

    /// 标记单条已读
    @ULog("'标记消息已读'")
    @PutMapping(value = "/{id}/read", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public void markAsRead(@PathVariable java.util.UUID id) {
        var userId = SecUtil.getCurrentUserId();
        notificationService.markAsRead(id, userId);
    }

    /// 全部标记已读
    @ULog("'全部标记已读'")
    @PutMapping(value = "/read-all", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public void markAllAsRead() {
        var userId = SecUtil.getCurrentUserId();
        notificationService.markAllAsRead(userId);
    }

    /// 删除消息
    @ULog("'删除消息'")
    @DeleteMapping(value = "/{id}", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public void deleteById(@PathVariable java.util.UUID id) {
        var userId = SecUtil.getCurrentUserId();
        notificationService.deleteById(id, userId);
    }

    /// 批量删除
    @ULog("'批量删除消息'")
    @PostMapping(value = "/batch-delete", version = "1.0.0+")
    @PreAuthorize("isAuthenticated()")
    public void batchDelete(@RequestBody List<java.util.UUID> ids) {
        var userId = SecUtil.getCurrentUserId();
        notificationService.batchDelete(ids, userId);
    }

    /// 发送消息（内部调用）
    @ULog("'发送消息'")
    @PostMapping(value = "/send", version = "1.0.0+")
    @PreAuthorize("permitAll()")
    public void send(@Valid @RequestBody NotificationSendDTO dto) {
        notificationService.send(dto);
    }

    /// 批量发送消息（内部调用）
    @ULog("'批量发送消息'")
    @PostMapping(value = "/batch-send", version = "1.0.0+")
    @PreAuthorize("permitAll()")
    public void batchSend(@Valid @RequestBody NotificationBatchSendDTO dto) {
        notificationService.batchSend(dto);
    }
}
