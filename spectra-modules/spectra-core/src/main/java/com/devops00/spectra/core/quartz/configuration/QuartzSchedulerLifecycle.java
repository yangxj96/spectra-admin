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

package com.devops00.spectra.core.quartz.configuration;

import com.devops00.spectra.core.quartz.catalog.QuartzJobRegistrar;
import com.devops00.spectra.core.quartz.listener.QuartzExecutionHistoryJobListener;
import lombok.extern.slf4j.Slf4j;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.context.SmartLifecycle;

import java.time.Duration;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

/** 管理 Quartz JDBC Scheduler 的启动重试、就绪和优雅停止。 */
@Slf4j
public class QuartzSchedulerLifecycle implements SmartLifecycle {

    private static final int LIFECYCLE_PHASE = Integer.MAX_VALUE - 120;

    private final Scheduler scheduler;
    private final boolean enabled;
    private final Duration databaseRetryInterval;
    private final QuartzJobRegistrar registrar;
    private final QuartzExecutionHistoryJobListener historyListener;
    private final ScheduledExecutorService retryExecutor;

    private volatile State state = State.STOPPED;
    private volatile String failureCode = "NOT_STARTED";
    private volatile String failureReason;
    private volatile ScheduledFuture<?> retryFuture;
    private volatile boolean ready;

    /** Quartz 生命周期状态。 */
    public enum State {
        /** 正在初始化 JDBC Scheduler。 */
        STARTING,
        /** Scheduler 已启动且允许执行 Job。 */
        READY,
        /** Scheduler 不可用或正在等待数据库恢复。 */
        NOT_READY,
        /** 正在等待运行中的 Job 完成。 */
        STOPPING,
        /** Scheduler 已停止。 */
        STOPPED
    }

    /** 注入 Quartz Scheduler 及生命周期依赖。 */
    public QuartzSchedulerLifecycle(Scheduler scheduler,
                                    QuartzSchedulerProperties properties,
                                    QuartzJobRegistrar registrar,
                                    ScheduledExecutorService retryExecutor) {
        this(scheduler, properties, registrar, retryExecutor, null);
    }

    /** 注入带执行历史监听器的 Quartz 生命周期。 */
    public QuartzSchedulerLifecycle(Scheduler scheduler,
                                    QuartzSchedulerProperties properties,
                                    QuartzJobRegistrar registrar,
                                    ScheduledExecutorService retryExecutor,
                                    QuartzExecutionHistoryJobListener historyListener) {
        this.scheduler = scheduler;
        this.enabled = properties.isEnabled();
        this.databaseRetryInterval = properties.getDatabaseRetryInterval();
        this.registrar = registrar;
        this.retryExecutor = retryExecutor;
        this.historyListener = historyListener;
    }

    /** 尝试启动 JDBC Cluster；数据库失败只进入未就绪状态。 */
    @Override
    public synchronized void start() {
        if (state == State.READY || state == State.STARTING || state == State.STOPPING) {
            return;
        }
        if (!enabled) {
            ready = false;
            state = State.NOT_READY;
            failureCode = "DISABLED";
            failureReason = "Quartz 调度未启用";
            return;
        }
        state = State.STARTING;
        try {
            registerHistoryListener();
            scheduler.start();
            registrar.register(scheduler);
            ready = true;
            state = State.READY;
            failureCode = null;
            failureReason = null;
            cancelRetry();
        } catch (SchedulerException | RuntimeException exception) {
            ready = false;
            state = State.NOT_READY;
            failureCode = "DATABASE_UNAVAILABLE";
            failureReason = "Quartz JDBC Scheduler 初始化失败";
            standbyAfterFailure();
            scheduleRetry();
            log.warn("Quartz JDBC Scheduler 未就绪，将在稍后重试: code={}", failureCode);
        }
    }

    /** 停止接收新的调度并等待运行中的 Job 完成。 */
    @Override
    public synchronized void stop() {
        if (state == State.STOPPED) {
            return;
        }
        state = State.STOPPING;
        ready = false;
        cancelRetry();
        try {
            scheduler.shutdown(true);
        } catch (SchedulerException exception) {
            log.warn("Quartz Scheduler 停止时出现受控异常");
        } finally {
            state = State.STOPPED;
        }
    }

    /** 执行 Spring 的带回调停止流程。 */
    @Override
    public void stop(Runnable callback) {
        try {
            stop();
        } finally {
            callback.run();
        }
    }

    /** 返回当前生命周期状态。 */
    public State getState() {
        return state;
    }

    /** 返回是否允许管理端和 Quartz Job 执行。 */
    public boolean isReady() {
        return ready;
    }

    /** 返回稳定的不可用原因码。 */
    public String getFailureCode() {
        return failureCode;
    }

    /** 返回脱敏后的不可用原因。 */
    public String getFailureReason() {
        return failureReason;
    }

    /** 返回底层 Quartz Scheduler。 */
    public Scheduler scheduler() {
        return scheduler;
    }

    /** 手动触发一次数据库恢复重试。 */
    public synchronized void retryStartIfNeeded() {
        if (state == State.NOT_READY) {
            start();
        }
    }

    /** SmartLifecycle 自动启动。 */
    @Override
    public boolean isAutoStartup() {
        return true;
    }

    /** 将生命周期放在业务 Job 之前启动、在业务 Job 之后停止。 */
    @Override
    public int getPhase() {
        return LIFECYCLE_PHASE;
    }

    /** 返回底层 Scheduler 是否已经关闭。 */
    @Override
    public boolean isRunning() {
        return ready;
    }

    /** 失败后将 Scheduler 置为 standby，阻止未就绪期间派发 Job。 */
    private void standbyAfterFailure() {
        try {
            if (!scheduler.isShutdown()) {
                scheduler.standby();
            }
        } catch (SchedulerException exception) {
            log.debug("Quartz Scheduler 进入 standby 失败");
        }
    }

    /** 在 Scheduler 启动前注册全局历史监听器，避免首个 Job 无开始记录。 */
    private void registerHistoryListener() throws SchedulerException {
        if (historyListener == null) {
            return;
        }
        var listenerManager = scheduler.getListenerManager();
        if (listenerManager.getJobListener(historyListener.getName()) == null) {
            listenerManager.addJobListener(historyListener);
        }
    }

    /** 只创建一个数据库恢复重试任务。 */
    private synchronized void scheduleRetry() {
        if (retryFuture == null || retryFuture.isDone()) {
            retryFuture = retryExecutor.scheduleWithFixedDelay(
                    this::retryStartIfNeeded,
                    databaseRetryInterval.toMillis(),
                    databaseRetryInterval.toMillis(),
                    TimeUnit.MILLISECONDS);
        }
    }

    /** 进入就绪状态后取消数据库恢复重试任务。 */
    private synchronized void cancelRetry() {
        if (retryFuture != null) {
            retryFuture.cancel(false);
            retryFuture = null;
        }
    }
}
