/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 */

package com.devops00.spectra.core.security.authentication.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.devops00.spectra.common.exception.DataSaveException;
import com.devops00.spectra.common.exception.EntityUpdateException;
import com.devops00.spectra.core.security.authentication.javabean.entity.UserContact;
import com.devops00.spectra.core.security.authentication.mapper.UserContactMapper;
import com.devops00.spectra.core.security.authentication.service.UserContactService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/** 用户认证与通知联系方式服务实现。 */
@Service
@RequiredArgsConstructor
public class UserContactServiceImpl implements UserContactService {

    private final UserContactMapper mapper;

    @Override
    @Transactional
    public UserContact upsertVerified(UUID userId, String contactType, String value) {
        if (userId == null || !StringUtils.hasText(value)) {
            throw new DataSaveException("用户联系方式不能为空");
        }
        String normalizedType = normalizeType(contactType);
        String normalizedValue = normalizeValue(normalizedType, value);
        var contact = mapper.selectOne(new LambdaQueryWrapper<UserContact>()
                .eq(UserContact::getUserId, userId)
                .eq(UserContact::getContactType, normalizedType)
                .eq(UserContact::getState, ACTIVE)
                .isNull(UserContact::getDeleted)
                .last("LIMIT 1"));
        if (contact == null) {
            contact = new UserContact();
            contact.setUserId(userId);
            contact.setContactType(normalizedType);
            contact.setContactValue(normalizedValue);
            contact.setState(ACTIVE);
            contact.setVerifiedAt(Instant.now());
            if (mapper.insert(contact) != 1) {
                throw new DataSaveException("保存用户联系方式失败");
            }
            return contact;
        }
        contact.setContactValue(normalizedValue);
        contact.setState(ACTIVE);
        contact.setVerifiedAt(Instant.now());
        if (mapper.updateById(contact) != 1) {
            throw new EntityUpdateException("更新用户联系方式失败");
        }
        return contact;
    }

    @Override
    public List<UserContact> listActiveByUserId(UUID userId) {
        if (userId == null) {
            return List.of();
        }
        return mapper.selectList(new LambdaQueryWrapper<UserContact>()
                .eq(UserContact::getUserId, userId)
                .eq(UserContact::getState, ACTIVE)
                .isNull(UserContact::getDeleted)
                .orderByAsc(UserContact::getContactType));
    }

    @Override
    public Map<UUID, List<UserContact>> listActiveByUserIds(List<UUID> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }
        var distinctIds = userIds.stream().filter(id -> id != null).distinct().toList();
        if (distinctIds.isEmpty()) {
            return Map.of();
        }
        var result = new HashMap<UUID, List<UserContact>>();
        mapper.selectList(new LambdaQueryWrapper<UserContact>()
                .in(UserContact::getUserId, distinctIds)
                .eq(UserContact::getState, ACTIVE)
                .isNull(UserContact::getDeleted)
                .orderByAsc(UserContact::getContactType))
                .forEach(contact -> result.computeIfAbsent(contact.getUserId(), ignored -> new java.util.ArrayList<>()).add(contact));
        return result;
    }

    @Override
    @Transactional
    public void revokeByUserIdAndType(UUID userId, String contactType) {
        if (userId == null) {
            return;
        }
        String normalizedType = normalizeType(contactType);
        var contacts = mapper.selectList(new LambdaQueryWrapper<UserContact>()
                .eq(UserContact::getUserId, userId)
                .eq(UserContact::getContactType, normalizedType)
                .eq(UserContact::getState, ACTIVE)
                .isNull(UserContact::getDeleted));
        for (var contact : contacts) {
            contact.setState("REVOKED");
            if (mapper.updateById(contact) != 1) {
                throw new EntityUpdateException("撤销用户联系方式失败");
            }
        }
    }

    private String normalizeType(String contactType) {
        String type = contactType == null ? "" : contactType.trim().toUpperCase(java.util.Locale.ROOT);
        if (!PHONE.equals(type) && !EMAIL.equals(type)) {
            throw new DataSaveException("不支持的用户联系方式类型");
        }
        return type;
    }

    private String normalizeValue(String contactType, String value) {
        String normalized = value.trim();
        return EMAIL.equals(contactType) ? normalized.toLowerCase(java.util.Locale.ROOT) : normalized;
    }
}
