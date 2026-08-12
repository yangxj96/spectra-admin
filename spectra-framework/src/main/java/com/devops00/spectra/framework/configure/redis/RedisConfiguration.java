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

package com.devops00.spectra.framework.configure.redis;

import com.devops00.spectra.common.constant.LogPrefix;
import io.lettuce.core.ClientOptions;
import io.lettuce.core.RedisClient;
import io.lettuce.core.SocketOptions;
import io.lettuce.core.resource.DefaultClientResources;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.JacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

/**
 * Redis配置类
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/7/28 00:00
 */
@Slf4j
@Configuration
@RequiredArgsConstructor
public class RedisConfiguration {

    private final ObjectMapper om;

    private final DataRedisProperties redisProperties;

    /**
     * 配置原生 RedisClient Bean
     * 用于满足 LettuceClientAdapter.of() 的需求
     */
    @Bean
    public RedisClient redisClient() {
        // 连接 URL 可能包含密码，日志中禁止输出完整地址。
        log.debug(LogPrefix.REDIS.f("正在构建原生 RedisClient"));

        // 1. 构建 ClientResources (可选配置，如线程池、事件循环等)
        // 如果不需要特殊定制，可以传 null 使用默认配置
        DefaultClientResources resources = DefaultClientResources.create();

        // 2. 使用配置文件中的 URL 创建 RedisClient
        // 格式: redis://password@host:port/db
        RedisClient client = RedisClient.create(resources, redisProperties.getUrl());

        // 3. 配置超时时间 (对应配置文件中的 timeout 和 connect-timeout)
        // 这里设置的是命令超时的默认值
        Duration timeout = redisProperties.getTimeout();
        Duration connectTimeout = redisProperties.getConnectTimeout();

        // 构建 SocketOptions
        SocketOptions.Builder socketOptionsBuilder = SocketOptions.builder().keepAlive(true); // 建议开启 TCP KeepAlive

        // 设置 TCP 连接超时时间 (对应 connect-timeout)
        if (connectTimeout != null) {
            socketOptionsBuilder.connectTimeout(connectTimeout);
        }

        // 构建 ClientOptions
        ClientOptions.Builder clientOptionsBuilder = ClientOptions.builder().socketOptions(socketOptionsBuilder.build()).autoReconnect(true); // 建议开启自动重连

        // 设置命令执行超时时间 (对应 timeout)
        if (timeout != null) {
            clientOptionsBuilder.timeoutOptions(io.lettuce.core.TimeoutOptions.builder().fixedTimeout(timeout).build());
        }

        // 将配置应用到 RedisClient
        client.setOptions(clientOptionsBuilder.build());

        return client;
    }

    /**
     * 自定义redisTemplate
     *
     * @param factory redis连接工程
     * @return RedisTemplate<String, Object>
     */
    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        log.debug(LogPrefix.REDIS.f("开始配置Redis"));
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(factory);

        StringRedisSerializer keySerializer = new StringRedisSerializer();
        template.setKeySerializer(keySerializer);
        template.setHashKeySerializer(keySerializer);

        JacksonJsonRedisSerializer<Object> valueSerializer = new JacksonJsonRedisSerializer<>(om, Object.class);
        template.setValueSerializer(valueSerializer);
        template.setHashValueSerializer(valueSerializer);

        template.afterPropertiesSet();
        return template;
    }
}
