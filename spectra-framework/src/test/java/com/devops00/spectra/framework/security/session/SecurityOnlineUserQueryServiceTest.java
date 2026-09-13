/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.devops00.spectra.framework.security.session;

import com.devops00.spectra.common.port.security.UserOnlineVO;
import com.devops00.spectra.common.security.policy.SecuritySessionPolicyProvider;
import com.devops00.spectra.framework.security.session.converter.UserOnlineConverter;
import com.devops00.spectra.framework.security.properties.SecurityProperties;
import com.devops00.spectra.framework.security.redis.key.SecurityRedisKey;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.ValueOperations;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 在线用户查询批量读取契约测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
class SecurityOnlineUserQueryServiceTest {

    @Test
    void shouldReadAllSessionSummariesWithOneMultiGet() {
        RedisTemplate<String, Object> redis = mock();
        SetOperations<String, Object> sets = mock();
        ValueOperations<String, Object> values = mock();
        HashOperations<String, Object, Object> hashes = mock();
        when(redis.opsForSet()).thenReturn(sets);
        when(redis.opsForValue()).thenReturn(values);
        when(redis.opsForHash()).thenReturn(hashes);
        when(sets.members(SecurityRedisKey.ONLINE_SESSIONS.getPattern()))
                .thenReturn(Set.of("digest-a", "digest-b"));
        when(values.multiGet(anyList())).thenReturn(List.of(summary("user-a"), summary("user-b")));
        UserOnlineConverter converter = mock();
        when(converter.toVO(org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyString(), org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.anyLong())).thenReturn(mock(UserOnlineVO.class));

        ObjectProvider<SecuritySessionPolicyProvider> policies = mock();
        var service = new SecurityOnlineUserQueryService(
                new SecuritySessionStore(redis, new SecurityProperties(), policies), converter);

        assertThat(service.listOnlineUsers()).hasSize(2);
        verify(values).multiGet(anyList());
        verify(hashes, never()).entries(org.mockito.ArgumentMatchers.anyString());
    }

    /**
     * 处理安全用户查询相关数据。
     */
    private static Map<String, Object> summary(String userId) {
        return Map.of("userId", userId, "username", userId, "clientType", "web", "ip", "127.0.0.1",
                "loginTime", 1_700_000_000_000L);
    }
}
