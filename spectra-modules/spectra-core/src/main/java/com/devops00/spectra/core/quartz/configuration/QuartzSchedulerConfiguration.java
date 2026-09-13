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
import org.springframework.beans.factory.annotation.Qualifier;
import org.quartz.Scheduler;
import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.quartz.autoconfigure.QuartzAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;

import javax.sql.DataSource;
import java.util.Properties;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Quartz JDBC Scheduler、生命周期和 Spring JobFactory 配置。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Configuration(proxyBeanMethods = false)
@AutoConfigureBefore(QuartzAutoConfiguration.class)
@EnableConfigurationProperties(QuartzSchedulerProperties.class)
public class QuartzSchedulerConfiguration {

    /**
     * 创建关闭时不自动销毁的 Quartz FactoryBean，生命周期由包装器统一管理。
     *
     * @param dataSource 应用共享 PostgreSQL 数据源
     * @param jobFactory Spring 感知的 Job 工厂
     * @param properties Quartz 安全配置
     * @return 未自动启动的 Quartz FactoryBean
     */
    @Bean(name = "quartzSchedulerFactory", destroyMethod = "")
    public SchedulerFactoryBean quartzSchedulerFactory(DataSource dataSource,
                                                       SpringQuartzJobFactory jobFactory,
                                                       QuartzSchedulerProperties properties) {
        var factory = new SchedulerFactoryBean();
        factory.setDataSource(dataSource);
        factory.setJobFactory(jobFactory);
        factory.setQuartzProperties(quartzProperties(properties));
        factory.setAutoStartup(false);
        factory.setWaitForJobsToCompleteOnShutdown(true);
        factory.setOverwriteExistingJobs(false);
        return factory;
    }

    /** 创建只负责 Quartz 数据库恢复重试的生命周期执行器。 */
    @Bean(name = "quartzLifecycleRetryExecutor", destroyMethod = "shutdownNow")
    public ScheduledExecutorService quartzLifecycleRetryExecutor() {
        return Executors.newSingleThreadScheduledExecutor(
                Thread.ofVirtual().name("spectra-quartz-lifecycle-", 0).factory());
    }

    /** 创建 Quartz 生命周期包装器，阻止数据库不可用时影响应用启动。 */
    @Bean
    public QuartzSchedulerLifecycle quartzSchedulerLifecycle(
                                                             Scheduler scheduler,
                                                             QuartzSchedulerProperties properties,
                                                             QuartzJobRegistrar registrar,
                                                             @Qualifier("quartzLifecycleRetryExecutor") ScheduledExecutorService retryExecutor,
                                                             QuartzExecutionHistoryJobListener historyListener) {
        return new QuartzSchedulerLifecycle(scheduler, properties, registrar, retryExecutor, historyListener);
    }

    /** 将应用级配置转换为固定的 Quartz 属性集合。 */
    private Properties quartzProperties(QuartzSchedulerProperties properties) {
        var quartz = new Properties();
        quartz.setProperty("org.quartz.scheduler.instanceId", "AUTO");
        quartz.setProperty("org.quartz.scheduler.instanceName", "spectraQuartzScheduler");
        quartz.setProperty("org.quartz.jobStore.isClustered", Boolean.toString(properties.isClustered()));
        quartz.setProperty("org.quartz.jobStore.class", properties.getJobStoreClass());
        quartz.setProperty("org.quartz.jobStore.driverDelegateClass", "org.quartz.impl.jdbcjobstore.PostgreSQLDelegate");
        quartz.setProperty("org.quartz.jobStore.tablePrefix", properties.getTablePrefix());
        quartz.setProperty("org.quartz.jobStore.misfireThreshold", "60000");
        quartz.setProperty("org.quartz.threadPool.threadCount", Integer.toString(properties.getThreadCount()));
        quartz.setProperty("org.quartz.threadPool.threadPriority", "5");
        quartz.setProperty("org.quartz.threadPool.class", "org.quartz.simpl.SimpleThreadPool");
        return quartz;
    }
}
