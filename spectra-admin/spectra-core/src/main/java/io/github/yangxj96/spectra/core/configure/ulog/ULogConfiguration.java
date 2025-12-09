/*
 *  Copyright 2018-2025 yangxj96
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

package io.github.yangxj96.spectra.core.configure.ulog;

import io.github.yangxj96.spectra.core.configure.ulog.aspect.ULogAspect;
import io.github.yangxj96.spectra.core.configure.ulog.publisher.ULogEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.support.TaskExecutorAdapter;
import org.springframework.scheduling.annotation.EnableAsync;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * 操作日志相关配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025/6/27
 */
@Slf4j
@EnableAsync
@Configuration
public class ULogConfiguration {

    private static final String PREFIX = "[ULogConfiguration]:";

    /**
     * 日志消息订阅发布者
     *
     * @param publisher 发布者
     * @return {@link ULogEventPublisher}
     */
    @Bean
    public ULogEventPublisher uLogEventPublisher(ApplicationEventPublisher publisher) {
        log.debug(PREFIX + "载入日志消息订阅发布者");
        return new ULogEventPublisher(publisher);
    }

    /**
     * 日志AOP切面
     *
     * @return {@link ULogAspect}
     */
    @Bean
    public ULogAspect uLogAspect() {
        log.debug(PREFIX + "载入 ULogAspect");
        return new ULogAspect();
    }

}
