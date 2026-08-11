package com.devops00.spectra.core.notification.service.impl;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.notification.javabean.entity.NotificationInbox;
import com.devops00.spectra.core.notification.mapper.NotificationInboxMapper;
import com.devops00.spectra.core.notification.service.NotificationInboxService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

/** 站内信写入服务实现。 */
@Service
@RequiredArgsConstructor
public class NotificationInboxServiceImpl implements NotificationInboxService {

    private final NotificationInboxMapper notificationInboxMapper;

    @Override
    public IPage<NotificationInbox> page(PageFrom page, UUID tenantId, UUID recipientUserId, Boolean unreadOnly) {
        var query = new LambdaQueryWrapper<NotificationInbox>().eq(NotificationInbox::getTenantId, tenantId)
                .eq(NotificationInbox::getRecipientUserId, recipientUserId).isNull(NotificationInbox::getArchivedAt)
                .orderByDesc(NotificationInbox::getCreatedAt);
        if (Boolean.TRUE.equals(unreadOnly)) {
            query.isNull(NotificationInbox::getReadAt);
        }
        return notificationInboxMapper.selectPage(new Page<>(page.getPageNum(), page.getPageSize()), query);
    }

    @Override
    public long unreadCount(UUID tenantId, UUID recipientUserId) {
        var query = new LambdaQueryWrapper<NotificationInbox>().eq(NotificationInbox::getTenantId, tenantId)
                .eq(NotificationInbox::getRecipientUserId, recipientUserId).isNull(NotificationInbox::getReadAt)
                .isNull(NotificationInbox::getArchivedAt);
        return notificationInboxMapper.selectCount(query);
    }

    @Override
    @Transactional
    public void markAsRead(UUID id, UUID tenantId, UUID recipientUserId) {
        var inbox = owned(id, tenantId, recipientUserId);
        if (inbox.getReadAt() == null) {
            inbox.setReadAt(Instant.now());
            notificationInboxMapper.updateById(inbox);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID tenantId, UUID recipientUserId) {
        var query = new LambdaQueryWrapper<NotificationInbox>().eq(NotificationInbox::getTenantId, tenantId)
                .eq(NotificationInbox::getRecipientUserId, recipientUserId).isNull(NotificationInbox::getReadAt)
                .isNull(NotificationInbox::getArchivedAt);
        var update = new NotificationInbox();
        update.setReadAt(Instant.now());
        notificationInboxMapper.update(update, query);
    }

    @Override
    @Transactional
    public void archive(UUID id, UUID tenantId, UUID recipientUserId) {
        var inbox = owned(id, tenantId, recipientUserId);
        inbox.setArchivedAt(Instant.now());
        notificationInboxMapper.updateById(inbox);
    }

    private NotificationInbox owned(UUID id, UUID tenantId, UUID recipientUserId) {
        var query = new LambdaQueryWrapper<NotificationInbox>().eq(NotificationInbox::getId, id)
                .eq(NotificationInbox::getTenantId, tenantId).eq(NotificationInbox::getRecipientUserId, recipientUserId);
        var inbox = notificationInboxMapper.selectOne(query);
        if (inbox == null) {
            throw new DataNotExistException("站内信不存在");
        }
        return inbox;
    }

    @Override
    @Transactional
    public List<NotificationInbox> publish(UUID requestId, UUID tenantId, List<UUID> recipientUserIds, String title, String content) {
        if (requestId == null || tenantId == null || recipientUserIds == null || recipientUserIds.isEmpty()
                || !StringUtils.hasText(title) || !StringUtils.hasText(content)) {
            throw new DataSaveException("站内信参数不完整");
        }
        var now = Instant.now();
        var result = new ArrayList<NotificationInbox>();
        for (var recipientUserId : recipientUserIds.stream().distinct().toList()) {
            if (recipientUserId == null) {
                continue;
            }
            var inbox = new NotificationInbox();
            inbox.setId(UUID.randomUUID());
            inbox.setTenantId(tenantId);
            inbox.setRecipientUserId(recipientUserId);
            inbox.setRequestId(requestId);
            inbox.setTitle(title);
            inbox.setContent(content);
            inbox.setCreatedAt(now);
            if (notificationInboxMapper.insert(inbox) != 1) {
                throw new DataSaveException("写入站内信失败");
            }
            result.add(inbox);
        }
        if (result.isEmpty()) {
            throw new DataSaveException("站内信没有有效收件人");
        }
        return result;
    }
}
