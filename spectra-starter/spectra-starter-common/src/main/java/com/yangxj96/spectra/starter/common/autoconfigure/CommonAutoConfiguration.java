package com.yangxj96.spectra.starter.common.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangxj96.spectra.starter.common.aspectj.ULogAspect;
import com.yangxj96.spectra.starter.common.configure.GlobalExceptionConfiguration;
import com.yangxj96.spectra.starter.common.configure.RequestGetParamsFilter;
import com.yangxj96.spectra.starter.common.configure.ResponseBodyModifyConfiguration;
import com.yangxj96.spectra.starter.common.configure.ULogEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

/**
 * 通用的一些自动配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/27
 */
@Slf4j
@Import({
        GlobalExceptionConfiguration.class,
        ResponseBodyModifyConfiguration.class,
        RequestGetParamsFilter.class
})
public class CommonAutoConfiguration {

    private static final String PREFIX = "[CommonAutoConfiguration]:";

    /**
     * 日志消息订阅发布者
     *
     * @param publisher 发布者
     * @return {@link ULogEventPublisher}
     */
    @Bean
    public ULogEventPublisher uLogEventPublisher(ApplicationEventPublisher publisher) {
        log.atDebug().log(PREFIX + "载入日志消息订阅发布者");
        return new ULogEventPublisher(publisher);
    }

    /**
     * 日志AOP切面
     *
     * @param publisher 消息发送器
     * @param om        Jackson
     * @return {@link ULogAspect}
     */
    @Bean
    public ULogAspect uLogAspect(ULogEventPublisher publisher, ObjectMapper om) {
        log.atDebug().log(PREFIX + "载入ULogAspect");
        return new ULogAspect(publisher, om);
    }

    /**
     * 配置一个用于日志保存的异步线程池
     *
     * @return {@link Executor}
     */
    @Bean
    @ConditionalOnBean(ULogAspect.class)
    public Executor uLogTaskExecutor() {
        log.atDebug().log(PREFIX + "初始化一个ThreadPoolTaskExecutor供日志切面保存使用");
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
