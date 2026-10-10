/*
 * Copyright 2018-2026 yangxj96
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 */
package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.exception.SecurityRedisUnavailableException;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.StaticListableBeanFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.RedisConnectionFailureException;

import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 安全集合读取的未知结果与正常空集合边界。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/07
 */
class SecuritySessionStoreTest {
    @Test
    @SuppressWarnings("unchecked")
    void unknownMembersFailClosedWhileConfirmedEmptySucceeds() {
        RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
        SetOperations<String, Object> sets = mock(SetOperations.class);
        when(redis.opsForSet()).thenReturn(sets);
        var store = new SecuritySessionStore(redis, new SecurityProperties(),
                new StaticListableBeanFactory().getBeanProvider(SecuritySessionPolicyProvider.class));
        when(sets.members("unknown")).thenReturn(null);
        when(sets.members("empty")).thenReturn(Set.of());
        when(sets.members("present")).thenReturn(Set.of("access-digest"));
        when(sets.members("failed")).thenThrow(new org.springframework.dao.QueryTimeoutException("synthetic timeout"));
        when(sets.members("disconnected")).thenThrow(new RedisConnectionFailureException("synthetic disconnect"));
        assertThrows(SecurityRedisUnavailableException.class, () -> store.members("test", "unknown"));
        assertThrows(SecurityRedisUnavailableException.class, () -> store.members("test", "failed"));
        assertThrows(SecurityRedisUnavailableException.class, () -> store.members("test", "disconnected"));
        assertEquals(Set.of(), store.members("test", "empty"));
        assertEquals(Set.of("access-digest"), store.members("test", "present"));
    }

    @Test
    @SuppressWarnings("unchecked")
    void unknownUserIndexRejectsRevocationBeforeCleanup() {
        RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
        SetOperations<String, Object> sets = mock(SetOperations.class);
        when(redis.opsForSet()).thenReturn(sets);
        var store = new SecuritySessionStore(redis, new SecurityProperties(),
                new StaticListableBeanFactory().getBeanProvider(SecuritySessionPolicyProvider.class));
        UUID userId = UUID.randomUUID();
        String userIndexKey = SecurityRedisKey.USER_TOKENS.format(userId);
        when(sets.members(userIndexKey)).thenReturn(null);

        var revoker = new SecuritySessionRevocationService(store, () -> null);
        assertThrows(SecurityRedisUnavailableException.class, () -> revoker.deleteByUserId(userId));
        verify(sets, never()).size(anyString());
        verify(redis, never()).delete(anyString());
    }

    @Test
    @SuppressWarnings("unchecked")
    void confirmedEmptyUserIndexAllowsRevocationCleanup() {
        RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
        SetOperations<String, Object> sets = mock(SetOperations.class);
        when(redis.opsForSet()).thenReturn(sets);
        var store = new SecuritySessionStore(redis, new SecurityProperties(),
                new StaticListableBeanFactory().getBeanProvider(SecuritySessionPolicyProvider.class));
        UUID userId = UUID.randomUUID();
        String userIndexKey = SecurityRedisKey.USER_TOKENS.format(userId);
        when(sets.members(userIndexKey)).thenReturn(Set.of());
        when(sets.size(userIndexKey)).thenReturn(0L);

        new SecuritySessionRevocationService(store, () -> null).deleteByUserId(userId);
        verify(redis).delete(userIndexKey);
    }
}
