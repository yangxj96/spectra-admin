package com.yangxj96.spectra.starter.common.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangxj96.spectra.starter.common.aspectj.ULogAspect;
import com.yangxj96.spectra.starter.common.service.ULogService;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 通用的一些自动配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/27
 */
public class CommonAutoConfiguration {

    @Bean
    @ConditionalOnClass({ULogService.class, ObjectMapper.class})
    public ULogAspect uLogAspect(ULogService service, ObjectMapper om) {
        return new ULogAspect(service, om);
    }


    @Bean
    @ConditionalOnBean(ULogAspect.class)
    public Executor uLogTaskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("ULog-Async-");
        executor.setWaitForTasksToCompleteOnShutdown(true);
        executor.initialize();
        return executor;
    }

}
