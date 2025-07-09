package com.yangxj96.spectra.starter.common.autoconfigure;

import com.yangxj96.spectra.starter.common.aspectj.ULogAspect;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.web.embedded.tomcat.TomcatProtocolHandlerCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.core.task.support.TaskExecutorAdapter;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 虚拟线程自动配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/7/9
 */
@Slf4j
public class VirtualThreadAutoConfiguration {

    private static final String PREFIX = "[VirtualThreadAutoConfiguration]:";

    @Bean
    public TomcatProtocolHandlerCustomizer<?> protocolHandlerCustomizer() {
        log.atDebug().log("启用虚拟线程");
        return protocolHandler -> protocolHandler.setExecutor(Executors.newVirtualThreadPerTaskExecutor());
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
        //ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        //executor.setCorePoolSize(5);
        //executor.setMaxPoolSize(10);
        //executor.setQueueCapacity(100);
        //executor.setThreadNamePrefix("ULog-Async-");
        //executor.setWaitForTasksToCompleteOnShutdown(true);
        //executor.initialize();
        //return executor;
        return new TaskExecutorAdapter(Executors.newThreadPerTaskExecutor(
                Thread
                        .ofVirtual()
                        .name("ULog-Async-", 0)
                        .factory()
        ));
    }


}
