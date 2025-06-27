package com.yangxj96.spectra.starter.common.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.yangxj96.spectra.starter.common.aspectj.ULogAspect;
import com.yangxj96.spectra.starter.common.configure.GlobalExceptionConfiguration;
import com.yangxj96.spectra.starter.common.configure.RequestGetParamsFilter;
import com.yangxj96.spectra.starter.common.configure.ResponseBodyModifyConfiguration;
import com.yangxj96.spectra.starter.common.service.ULogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
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

    @Bean
    @ConditionalOnClass({ULogService.class, ObjectMapper.class})
    public ULogAspect uLogAspect(ULogService service, ObjectMapper om) {
        log.atDebug().log(PREFIX + "载入ULogAspect");
        return new ULogAspect(service, om);
    }


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
