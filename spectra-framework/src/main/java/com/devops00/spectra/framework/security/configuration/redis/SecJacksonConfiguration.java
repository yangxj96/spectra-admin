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

package com.devops00.spectra.framework.security.configuration.redis;

import com.devops00.spectra.common.constant.LogPrefix;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

/**
 * Security配置Jackson
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/15 14:42
 */
@Slf4j
@Configuration
public class SecJacksonConfiguration {

    /**
     * 创建只处理字符串、数字和 Map/Collection 基础值的安全 Redis mapper。
     */
    @Bean("securityObjectMapper")
    public ObjectMapper redisObjectMapper() {
        log.debug(LogPrefix.SECURITY.f("开始配置Security使用的ObjectMapper"));
        return JsonMapper.builder()
                .configureForJackson2()
                .enable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                .build();
    }
}
