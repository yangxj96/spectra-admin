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

package io.github.yangxj96.spectra.core.configure.redis;

import io.github.yangxj96.spectra.common.constant.LogPrefix;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.ObjectMapper;

/// Redis配置类
///
/// @author Jack Young
/// @version 1.0
/// @since 2025/7/28
@Slf4j
@Configuration
public class RedisConfiguration {

    @Resource
    private ObjectMapper om;

    /// 自定义redisTemplate
    ///
    /// @param factory redis连接工程
    /// @return RedisTemplate<String, Object>
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        log.debug(LogPrefix.REDIS.f("开始配置Redis"));
        return RedisTemplateFactory.build(factory, om);
    }

}
