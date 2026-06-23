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

package com.devops00.spectra.log.starter;

import com.devops00.spectra.common.constant.LogPrefix;
import com.devops00.spectra.log.base.aspect.ULogAspect;
import com.devops00.spectra.log.base.publisher.ULogEventPublisher;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;

/// 操作日志相关配置
///
/// @author yangxj96
/// @version 1.0
/// @since 2025/6/27
@Slf4j
@EnableAsync
@AutoConfiguration
public class ULogConfiguration {

    /// 日志消息订阅发布者
    ///
    /// @param publisher 发布者
    /// @return {@link ULogEventPublisher}
    @Bean
    public ULogEventPublisher uLogEventPublisher(ApplicationEventPublisher publisher) {
        log.debug(LogPrefix.LOG.f("载入日志消息订阅发布者"));
        return new ULogEventPublisher(publisher);
    }

    /// 日志切面
    @Bean
    public ULogAspect uLogAspect() {
        log.debug(LogPrefix.LOG.f("载入ULogAspect"));
        return new ULogAspect();
    }

}
