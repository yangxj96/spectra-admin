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

package com.devops00.spectra.framework.health.readiness;

import org.junit.jupiter.api.Test;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * {@link DependencyReadinessProbe} 后台调度暂停/恢复与就绪状态切换的行为测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
class DependencyReadinessProbeTest {

    @SuppressWarnings("unchecked")
    private static <T> ObjectProvider<T> provider(T value) {
        ObjectProvider<T> provider = mock(ObjectProvider.class);
        when(provider.getIfAvailable()).thenReturn(value);
        return provider;
    }

    private static DataSource dataSource(boolean valid) throws Exception {
        DataSource dataSource = mock(DataSource.class);
        Connection connection = mock(Connection.class);
        when(dataSource.getConnection()).thenReturn(connection);
        when(connection.isValid(anyInt())).thenReturn(valid);
        return dataSource;
    }

    private static RedisTemplate<String, Object> redisTemplate() {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> template = mock(RedisTemplate.class);
        RedisConnectionFactory factory = mock(RedisConnectionFactory.class);
        RedisConnection connection = mock(RedisConnection.class);
        when(template.getConnectionFactory()).thenReturn(factory);
        when(factory.getConnection()).thenReturn(connection);
        return template;
    }

    @Test
    void unavailableDependencyMarksUnreadyAndPausesSchedule() throws Exception {
        Scheduler scheduler = mock(Scheduler.class);
        DependencyReadinessState state = new DependencyReadinessState();
        var probe = new DependencyReadinessProbe(
                provider(dataSource(false)), redisTemplate(), provider(scheduler),
                state, new ReadinessProperties(5000L, 30000L));

        probe.probe();

        assertFalse(state.isReady());
        assertEquals(DependencyReadinessState.State.UNREADY, state.state());
        verify(scheduler, times(1)).pauseAll();
        verify(scheduler, never()).resumeAll();
    }

    @Test
    void recoveryMarksReadyAndResumesSchedule() throws Exception {
        Scheduler scheduler = mock(Scheduler.class);
        DependencyReadinessState state = new DependencyReadinessState();
        DataSource dataSource = dataSource(true);
        var probe = new DependencyReadinessProbe(
                provider(dataSource), redisTemplate(), provider(scheduler),
                state, new ReadinessProperties(5000L, 30000L));

        probe.probe();
        assertTrue(state.isReady());
        verify(scheduler, never()).pauseAll();

        when(dataSource.getConnection()).thenThrow(new SQLException("db down"));
        probe.probe();
        assertFalse(state.isReady());
        verify(scheduler, times(1)).pauseAll();

        doReturn(dataSource(true).getConnection()).when(dataSource).getConnection();
        probe.probe();
        assertTrue(state.isReady());
        verify(scheduler, times(1)).resumeAll();
        verify(scheduler, times(1)).pauseAll();
    }

    @Test
    void missingRedisConnectionFactoryMarksUnready() throws Exception {
        @SuppressWarnings("unchecked")
        RedisTemplate<String, Object> template = mock(RedisTemplate.class);
        when(template.getConnectionFactory()).thenReturn(null);
        Scheduler scheduler = mock(Scheduler.class);
        DependencyReadinessState state = new DependencyReadinessState();
        var probe = new DependencyReadinessProbe(
                provider(null), template, provider(scheduler),
                state, new ReadinessProperties(5000L, 30000L));

        probe.probe();

        assertFalse(state.isReady());
        verify(scheduler, times(1)).pauseAll();
    }

    @Test
    void pauseFailureFallsBackToStandbyAndResumesOnRecovery() throws Exception {
        Scheduler scheduler = mock(Scheduler.class);
        doThrow(new SchedulerException("db down")).when(scheduler).pauseAll();
        DependencyReadinessState state = new DependencyReadinessState();
        DataSource dataSource = dataSource(false);
        var probe = new DependencyReadinessProbe(
                provider(dataSource), redisTemplate(), provider(scheduler),
                state, new ReadinessProperties(5000L, 30000L));

        probe.probe();

        assertFalse(state.isReady());
        verify(scheduler, times(1)).pauseAll();
        verify(scheduler, times(1)).standby();
        verify(scheduler, never()).resumeAll();

        doReturn(dataSource(true).getConnection()).when(dataSource).getConnection();
        probe.probe();

        assertTrue(state.isReady());
        verify(scheduler, times(1)).start();
        verify(scheduler, times(1)).resumeAll();
    }

    @Test
    void inconsistentRecoveryStaysUnreadyUntilVerified() throws Exception {
        Scheduler scheduler = mock(Scheduler.class);
        DependencyReadinessState state = new DependencyReadinessState();
        DataSource dataSource = dataSource(true);
        AtomicBoolean consistent = new AtomicBoolean(true);
        ReadinessVerifier verifier = () -> {
            if (!consistent.get()) {
                throw new IllegalStateException("密钥未就绪");
            }
        };
        var probe = new DependencyReadinessProbe(
                provider(dataSource), redisTemplate(), provider(scheduler),
                state, new ReadinessProperties(5000L, 30000L));
        probe.setReadinessVerifiers(List.of(verifier));

        probe.probe();
        assertTrue(state.isReady());

        when(dataSource.getConnection()).thenThrow(new SQLException("db down"));
        probe.probe();
        assertFalse(state.isReady());
        verify(scheduler, times(1)).pauseAll();

        doReturn(dataSource(true).getConnection()).when(dataSource).getConnection();
        consistent.set(false);
        probe.probe();
        assertFalse(state.isReady(), "一致性校验失败时不得重新就绪");
        verify(scheduler, never()).resumeAll();

        consistent.set(true);
        probe.probe();
        assertTrue(state.isReady());
        verify(scheduler, times(1)).resumeAll();
    }
}
