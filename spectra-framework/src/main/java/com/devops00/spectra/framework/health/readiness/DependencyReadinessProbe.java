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

import com.devops00.spectra.common.constant.LogPrefix;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NullMarked;
import org.quartz.Scheduler;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.SmartLifecycle;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * 必需依赖（数据库 + 安全 Redis）运行期探针。
 *
 * <p>周期性探测依赖可用性：任一不可用时把 {@link DependencyReadinessState} 标记为 UNREADY 并暂停后台调度；
 * 恢复时先执行 {@link ReadinessVerifier} 一致性校验，通过后标记 READY 并恢复调度。探测失败使用有界退避，
 * 避免故障期间高频重试。</p>
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/16
 */
@Slf4j
@Component
@NullMarked
@EnableConfigurationProperties(ReadinessProperties.class)
public class DependencyReadinessProbe implements SmartLifecycle {

    private final ObjectProvider<DataSource> dataSourceProvider;
    private final RedisTemplate<String, Object> securityRedisTemplate;
    private final ObjectProvider<Scheduler> schedulerProvider;
    private final DependencyReadinessState state;
    private final long baseIntervalMs;
    private final long maxIntervalMs;

    private List<ReadinessVerifier> readinessVerifiers = List.of();

    private volatile ScheduledExecutorService executor;
    private volatile boolean running;
    private final AtomicLong currentIntervalMs = new AtomicLong();

    public DependencyReadinessProbe(ObjectProvider<DataSource> dataSourceProvider,
                                    @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> securityRedisTemplate,
                                    ObjectProvider<Scheduler> schedulerProvider,
                                    DependencyReadinessState state,
                                    ReadinessProperties properties) {
        this.dataSourceProvider = dataSourceProvider;
        this.securityRedisTemplate = securityRedisTemplate;
        this.schedulerProvider = schedulerProvider;
        this.state = state;
        this.baseIntervalMs = properties.probeIntervalMs();
        this.maxIntervalMs = properties.maxIntervalMs();
        this.currentIntervalMs.set(baseIntervalMs);
    }

    /**
     * 注入恢复后一致性校验器；无实现时保持空列表。
     *
     * @param readinessVerifiers 一致性校验器列表
     */
    @Autowired(required = false)
    void setReadinessVerifiers(List<ReadinessVerifier> readinessVerifiers) {
        this.readinessVerifiers = readinessVerifiers;
    }

    @Override
    public void start() {
        running = true;
        currentIntervalMs.set(baseIntervalMs);
        executor = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "spectra-readiness-probe");
            thread.setDaemon(true);
            return thread;
        });
        scheduleNext();
    }

    @Override
    public void stop() {
        running = false;
        ScheduledExecutorService current = executor;
        if (current != null) {
            current.shutdownNow();
        }
    }

    @Override
    public boolean isRunning() {
        return running;
    }

    private void scheduleNext() {
        ScheduledExecutorService current = executor;
        if (running && current != null) {
            current.schedule(this::probe, currentIntervalMs.get(), TimeUnit.MILLISECONDS);
        }
    }

    void probe() {
        boolean wasUnready = !state.isReady();
        try {
            pingDatabase();
            pingRedis();
            if (wasUnready) {
                verifyConsistency();
            }
            state.markReady();
            currentIntervalMs.set(baseIntervalMs);
            if (wasUnready) {
                log.warn(LogPrefix.WEB.f("必需依赖恢复，运行态重新就绪"));
                resumeBackgroundWork();
            }
        } catch (Exception exception) {
            if (!wasUnready) {
                log.error(LogPrefix.WEB.f("必需依赖不可用，运行态转为未就绪: {}"),
                        exception.getClass().getSimpleName());
                pauseBackgroundWork();
            }
            state.markUnready(exception.getClass().getSimpleName());
            currentIntervalMs.set(Math.min(maxIntervalMs, currentIntervalMs.get() * 2));
        } finally {
            scheduleNext();
        }
    }

    private void verifyConsistency() throws Exception {
        for (ReadinessVerifier verifier : readinessVerifiers) {
            verifier.verifyRecovery();
        }
    }

    private void pingDatabase() throws Exception {
        DataSource dataSource = dataSourceProvider.getIfAvailable();
        if (dataSource == null) {
            throw new IllegalStateException("数据库连接未装配");
        }
        try (Connection connection = dataSource.getConnection()) {
            if (connection == null || !connection.isValid(2)) {
                throw new IllegalStateException("数据库连接无效");
            }
        }
    }

    private void pingRedis() {
        var factory = securityRedisTemplate.getConnectionFactory();
        if (factory == null) {
            throw new IllegalStateException("安全 Redis 连接工厂未装配");
        }
        try (var connection = factory.getConnection()) {
            if (connection == null) {
                throw new IllegalStateException("安全 Redis 连接不可用");
            }
            connection.ping();
        }
    }

    private void pauseBackgroundWork() {
        Scheduler scheduler = schedulerProvider.getIfAvailable();
        if (scheduler == null) {
            return;
        }
        try {
            scheduler.pauseAll();
        } catch (Exception exception) {
            log.warn(LogPrefix.WEB.f("暂停后台调度失败: {}"), exception.getClass().getSimpleName());
        }
    }

    private void resumeBackgroundWork() {
        Scheduler scheduler = schedulerProvider.getIfAvailable();
        if (scheduler == null) {
            return;
        }
        try {
            scheduler.resumeAll();
        } catch (Exception exception) {
            log.warn(LogPrefix.WEB.f("恢复后台调度失败: {}"), exception.getClass().getSimpleName());
        }
    }
}
