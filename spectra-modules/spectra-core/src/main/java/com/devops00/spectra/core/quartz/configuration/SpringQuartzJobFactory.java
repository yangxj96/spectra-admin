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

import lombok.RequiredArgsConstructor;
import org.quartz.Job;
import org.quartz.SchedulerException;
import org.quartz.spi.JobFactory;
import org.quartz.spi.TriggerFiredBundle;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.stereotype.Component;

/**
 * 使用 Spring BeanFactory 创建和初始化 Quartz Job。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@Component
@RequiredArgsConstructor
public class SpringQuartzJobFactory implements JobFactory {

    private final AutowireCapableBeanFactory beanFactory;

    /**
     * 按 Quartz JobDetail 中的受信任代码类型创建实例并注入 Spring 协作者。
     *
     * @param bundle    Quartz 触发上下文，只使用其中的 JobDetail 类型
     * @param scheduler 当前 Quartz Scheduler
     * @return 已完成 Spring 自动装配和初始化的 Job 实例
     * @throws SchedulerException Job 类型不合法或实例化失败
     */
    @Override
    public Job newJob(TriggerFiredBundle bundle, org.quartz.Scheduler scheduler) throws SchedulerException {
        if (bundle == null || bundle.getJobDetail() == null || bundle.getJobDetail().getJobClass() == null) {
            throw new SchedulerException("Quartz Job 定义不完整");
        }
        var jobClass = bundle.getJobDetail().getJobClass();
        if (!Job.class.isAssignableFrom(jobClass)) {
            throw new SchedulerException("Quartz Job 类型不合法");
        }
        try {
            var job = (Job) jobClass.getDeclaredConstructor().newInstance();
            beanFactory.autowireBean(job);
            return (Job) beanFactory.initializeBean(job, jobClass.getName());
        } catch (ReflectiveOperationException | RuntimeException exception) {
            throw new SchedulerException("Quartz Job 实例初始化失败", exception);
        }
    }
}
