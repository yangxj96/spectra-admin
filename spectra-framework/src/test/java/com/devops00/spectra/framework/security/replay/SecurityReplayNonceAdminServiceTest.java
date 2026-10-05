package com.devops00.spectra.framework.security.replay;

import org.junit.jupiter.api.Test;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.RedisTemplate;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SecurityReplayNonceAdminServiceTest {

    @Test
    void rejectsMissingRedisSerializersBeforeScanning() throws Exception {
        RedisTemplate<String, Object> redis = mock(RedisTemplate.class);
        when(redis.getKeySerializer()).thenReturn(null);
        when(redis.getValueSerializer()).thenReturn(null);
        SecurityReplayNonceAdminService service = new SecurityReplayNonceAdminService(redis, null);
        Method scanAndDelete = SecurityReplayNonceAdminService.class
                .getDeclaredMethod("scanAndDelete", RedisConnection.class, long.class);
        scanAndDelete.setAccessible(true);

        InvocationTargetException thrown = org.junit.jupiter.api.Assertions.assertThrows(
                InvocationTargetException.class,
                () -> scanAndDelete.invoke(service, mock(RedisConnection.class), 10L));

        assertInstanceOf(com.devops00.spectra.common.exception.SecurityRedisUnavailableException.class,
                thrown.getCause());
    }
}
