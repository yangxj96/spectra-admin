package com.devops00.spectra.core.notification.service;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.devops00.spectra.common.base.BaseService;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.core.notification.javabean.dto.NotificationBatchSendDTO;
import com.devops00.spectra.core.notification.javabean.dto.NotificationSendDTO;
import com.devops00.spectra.core.notification.javabean.entity.Notification;
import com.devops00.spectra.core.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationVO;

import java.util.List;
import java.util.UUID;

/**
 * 消息Service
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/7/19
 */
public interface NotificationService extends BaseService<Notification> {

    /**
     * 获取用户消息列表
     */
    IPage<NotificationVO> page(PageFrom page, NotificationQueryFrom params, UUID userId);

    /**
     * 获取用户未读数
     */
    long getUnreadCount(UUID userId);

    /**
     * 标记单条已读
     */
    void markAsRead(UUID id, UUID userId);

    /**
     * 全部标记已读
     */
    void markAllAsRead(UUID userId);

    /**
     * 删除消息
     */
    void deleteById(UUID id, UUID userId);

    /**
     * 批量删除
     */
    void batchDelete(List<UUID> ids, UUID userId);

    /**
     * 发送单条消息
     */
    void send(NotificationSendDTO dto);

    /**
     * 批量发送消息
     */
    void batchSend(NotificationBatchSendDTO dto);
}
