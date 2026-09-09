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

package com.devops00.spectra.framework.cache.configuration;

import com.devops00.spectra.framework.health.RedisHealthContributor;
import io.lettuce.core.RedisClient;
import io.lettuce.core.resource.ClientResources;
import org.junit.jupiter.api.Test;
import org.springframework.boot.data.redis.autoconfigure.DataRedisProperties;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import tools.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

/** Lettuce 客户端和线程资源的 Spring 生命周期回归测试。 */
class RedisConfigurationLifecycleTest {

    @Test
    void shouldRegisterRedisHealthContributorWithRedisConnectionFactory() {
        var context = new AnnotationConfigApplicationContext();
        context.register(RedisConfiguration.class, TestDependencies.class);
        context.refresh();

        assertThat(context.getBean(RedisHealthContributor.class)).isNotNull();

        context.close();
    }

    @Test
    void shouldCloseClientAndClientResourcesWhenContextCloses() {
        var context = new AnnotationConfigApplicationContext();
        context.register(RedisConfiguration.class, TestDependencies.class);
        context.refresh();

        var client = context.getBean(RedisClient.class);
        var resources = context.getBean(ClientResources.class);

        assertThat(context.getBeanFactory().getBeanDefinition("redisClient").getDestroyMethodName())
                .isEqualTo("shutdown");
        assertThat(context.getBeanFactory().getBeanDefinition("redisClientResources").getDestroyMethodName())
                .isEqualTo("shutdown");

        context.close();

        assertThat(client.shutdownAsync().isDone()).isTrue();
        assertThat(resources.eventExecutorGroup().isShuttingDown()).isTrue();
    }

    @Configuration(proxyBeanMethods = false)
    static class TestDependencies {

        @Bean
        ObjectMapper objectMapper() {
            return new ObjectMapper();
        }

        @Bean
        DataRedisProperties redisProperties() {
            var properties = new DataRedisProperties();
            properties.setUrl("redis://127.0.0.1:6379");
            return properties;
        }

        @Bean
        RedisConnectionFactory redisConnectionFactory() {
            return mock(RedisConnectionFactory.class);
        }
    }
}
