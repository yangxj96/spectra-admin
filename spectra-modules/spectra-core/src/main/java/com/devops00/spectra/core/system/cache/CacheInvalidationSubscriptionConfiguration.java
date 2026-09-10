/*
 *  Copyright 2018-2026 yangxj96
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 */

package com.devops00.spectra.core.system.cache;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.ChannelTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

import java.nio.charset.StandardCharsets;

/** 普通缓存失效广播的 Redis Pub/Sub 订阅配置。 */
@Configuration
public class CacheInvalidationSubscriptionConfiguration {

    /**
     * 订阅固定普通缓存频道，并将消息交给幂等协调器；频道中不携带安全 Redis 数据。
     */
    @Bean
    public RedisMessageListenerContainer cacheInvalidationListenerContainer(
            RedisConnectionFactory connectionFactory, CacheInvalidationCoordinator coordinator) {
        var container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.addMessageListener(
                (message, pattern) -> coordinator.applyEvent(new String(message.getBody(), StandardCharsets.UTF_8)),
                new ChannelTopic(CacheInvalidationCoordinator.CHANNEL));
        return container;
    }
}
