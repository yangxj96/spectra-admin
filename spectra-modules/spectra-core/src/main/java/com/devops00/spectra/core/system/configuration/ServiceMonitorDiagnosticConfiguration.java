/* Copyright 2018-2026 yangxj96 */

package com.devops00.spectra.core.system.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/** 服务监控诊断任务执行器。 */
@Configuration(proxyBeanMethods = false)
public class ServiceMonitorDiagnosticConfiguration {

    /**
     * 处理内部业务逻辑（{@code serviceMonitorDiagnosticTaskExecutor}）。
     */
    @Bean(name = "serviceMonitorDiagnosticTaskExecutor")
    public TaskExecutor serviceMonitorDiagnosticTaskExecutor() {
        var executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(1);
        executor.setMaxPoolSize(1);
        executor.setQueueCapacity(2);
        executor.setThreadNamePrefix("spectra-monitor-diagnostic-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.setAwaitTerminationSeconds(30);
        executor.initialize();
        return executor;
    }
}
