package com.yangxj96.spectra.framework.configure;

import com.yangxj96.spectra.framework.aspect.ULogAspect;
import com.yangxj96.spectra.framework.publisher.ULogEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 通用的一些自动配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/27
 */
@Slf4j
@Configuration
public class CommonConfiguration {

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
     * @return {@link ULogAspect}
     */
    @Bean
    public ULogAspect uLogAspect() {
        log.atDebug().log(PREFIX + "载入ULogAspect");
        return new ULogAspect();
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
        return new TaskExecutorAdapter(Executors.newThreadPerTaskExecutor(
                Thread
                        .ofVirtual()
                        .name("ULog-Async-", 0)
                        .factory()
        ));
    }

}
