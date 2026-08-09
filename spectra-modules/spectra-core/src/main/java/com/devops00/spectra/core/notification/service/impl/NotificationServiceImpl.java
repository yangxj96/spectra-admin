package com.devops00.spectra.core.notification.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.base.BaseServiceImpl;
import com.devops00.spectra.common.base.javabean.from.PageFrom;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.core.notification.javabean.converter.NotificationConverter;
import com.devops00.spectra.core.notification.javabean.dto.NotificationBatchSendDTO;
import com.devops00.spectra.core.notification.javabean.dto.NotificationSendDTO;
import com.devops00.spectra.core.notification.javabean.entity.Notification;
import com.devops00.spectra.core.notification.javabean.from.NotificationQueryFrom;
import com.devops00.spectra.core.notification.javabean.vo.NotificationVO;
import com.devops00.spectra.core.notification.mapper.NotificationMapper;
import com.devops00.spectra.core.notification.service.NotificationService;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/// 消息Service实现
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/7/19
@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationServiceImpl extends BaseServiceImpl<NotificationMapper, Notification> implements NotificationService {

    private final NotificationConverter notificationConverter;
    private final TimeMapper timeMapper;

    @Override
    public IPage<NotificationVO> page(PageFrom page, NotificationQueryFrom params, UUID userId) {
        Page<Notification> pageParam = new Page<>(page.getPageNum(), page.getPageSize());
        var wrapper = new LambdaQueryWrapper<Notification>();

        wrapper.eq(Notification::getReceiverId, userId);

        if (StringUtils.hasText(params.getType())) {
            wrapper.eq(Notification::getType, params.getType());
        }

        if (params.getIsRead() != null) {
            wrapper.eq(Notification::getIsRead, params.getIsRead());
        }

        if (StringUtils.hasText(params.getKeyword())) {
            wrapper.and(w -> w.like(Notification::getTitle, params.getKeyword()).or().like(Notification::getContent, params.getKeyword()));
        }

        if (StringUtils.hasText(params.getStartTime())) {
            wrapper.ge(Notification::getCreatedAt, parseTime(params.getStartTime()));
        }

        if (StringUtils.hasText(params.getEndTime())) {
            wrapper.le(Notification::getCreatedAt, parseTime(params.getEndTime()));
        }

        wrapper.orderByDesc(Notification::getCreatedAt);

        var result = this.page(pageParam, wrapper);
        return notificationConverter.toVOPage(result);
    }

    private Instant parseTime(String value) {
        try {
            return timeMapper.toInstant(value);
        } catch (RuntimeException exception) {
            throw new DataSaveException("消息时间格式不正确");
        }
    }

    @Override
    public long getUnreadCount(UUID userId) {
        var wrapper = new LambdaQueryWrapper<Notification>();
        wrapper.eq(Notification::getReceiverId, userId).eq(Notification::getIsRead, false);
        return this.count(wrapper);
    }

    @Override
    @Transactional
    public void markAsRead(UUID id, UUID userId) {
        var entity = this.getById(id);
        if (entity == null || !entity.getReceiverId().equals(userId)) {
            throw new DataNotExistException("消息不存在");
        }

        if (!entity.getIsRead()) {
            entity.setIsRead(true);
            entity.setReadAt(Instant.now());
            if (!this.updateById(entity)) {
                throw new DataSaveException("标记已读失败");
            }
            log.info("标记消息已读: id={}", id);
        }
    }

    @Override
    @Transactional
    public void markAllAsRead(UUID userId) {
        var wrapper = new LambdaQueryWrapper<Notification>();
        wrapper.eq(Notification::getReceiverId, userId).eq(Notification::getIsRead, false);

        var entity = new Notification();
        entity.setIsRead(true);
        entity.setReadAt(Instant.now());

        this.update(entity, wrapper);
        log.info("全部标记已读: userId={}", userId);
    }

    @Override
    @Transactional
    public void deleteById(UUID id, UUID userId) {
        var entity = this.getById(id);
        if (entity == null || !entity.getReceiverId().equals(userId)) {
            throw new DataNotExistException("消息不存在");
        }

        if (!this.removeById(id)) {
            throw new DataSaveException("删除消息失败");
        }
        log.info("删除消息: id={}", id);
    }

    @Override
    @Transactional
    public void batchDelete(List<UUID> ids, UUID userId) {
        var wrapper = new LambdaQueryWrapper<Notification>();
        wrapper.in(Notification::getId, ids).eq(Notification::getReceiverId, userId);

        this.remove(wrapper);
        log.info("批量删除消息: count={}", ids.size());
    }

    @Override
    @Transactional
    public void send(NotificationSendDTO dto) {
        var entity = new Notification();
        entity.setTitle(dto.getTitle());
        entity.setContent(dto.getContent());
        entity.setType(dto.getType());
        entity.setSenderId(dto.getSenderId());
        entity.setSenderName(dto.getSenderName());
        entity.setLink(dto.getLink());
        entity.setReceiverId(dto.getReceiverId());
        entity.setIsRead(false);

        if (dto.getExtra() != null && !dto.getExtra().isEmpty()) {
            entity.setExtra(convertExtraToJson(dto.getExtra()));
        }

        if (!this.save(entity)) {
            throw new DataSaveException("发送消息失败");
        }
        log.info("发送消息成功: type={}, receiverId={}", dto.getType(), dto.getReceiverId());
    }

    @Override
    @Transactional
    public void batchSend(NotificationBatchSendDTO dto) {
        for (UUID receiverId : dto.getReceiverIds()) {
            var entity = new Notification();
            entity.setTitle(dto.getTitle());
            entity.setContent(dto.getContent());
            entity.setType(dto.getType());
            entity.setSenderId(dto.getSenderId());
            entity.setSenderName(dto.getSenderName());
            entity.setLink(dto.getLink());
            entity.setReceiverId(receiverId);
            entity.setIsRead(false);

            if (dto.getExtra() != null && !dto.getExtra().isEmpty()) {
                entity.setExtra(convertExtraToJson(dto.getExtra()));
            }

            this.save(entity);
        }
        log.info("批量发送消息成功: type={}, count={}", dto.getType(), dto.getReceiverIds().size());
    }

    /// 将Map转换为JSON字符串
    private String convertExtraToJson(Map<String, Object> extra) {
        try {
            return new com.fasterxml.jackson.databind.ObjectMapper().writeValueAsString(extra);
        } catch (Exception e) {
            log.error("转换extra为JSON失败", e);
            return null;
        }
    }
}
