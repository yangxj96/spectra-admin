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

package com.devops00.spectra.security.starter.configuration;

import com.devops00.spectra.security.base.holder.SecHolderStrategy;
import com.devops00.spectra.security.base.holder.SecurityUserLoader;
import com.devops00.spectra.security.base.properties.SecurityProperties;
import com.devops00.spectra.security.starter.holder.SecStrategyBridge;
import com.devops00.spectra.security.starter.strategy.RedisSecHolderStrategy;
import com.devops00.spectra.security.starter.web.javabean.converter.UserOnlineConverter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.RedisTemplate;
import tools.jackson.databind.ObjectMapper;

/**
 * SecuUtil工具相关配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/2/19 22:37
 */
@Slf4j
@Configuration
public class SecUtilConfiguration {

    /**
     * SecUtil 工具桥接
     *
     * @param strategy 具体策略
     */
    @Bean
    public SecStrategyBridge secStrategyBridge(SecHolderStrategy strategy) {
        return new SecStrategyBridge(strategy);
    }

    /**
     * 使用Redis操作SecUtil具体业务的策略实现
     *
     * @param om                  Security使用的ObjectMapper
     * @param redis               Security使用的RedisTemplate
     * @param properties          安全配置
     * @param userOnlineConverter 在线用户转换器
     */
    @Bean(name = "sec")
    @ConditionalOnProperty(prefix = "spectra.security", name = "sec-mode", havingValue = "REDIS", matchIfMissing = true)
    public RedisSecHolderStrategy redisSecHolderStrategy(
                                                         @Qualifier("securityObjectMapper") ObjectMapper om,
                                                         @Qualifier("securityRedisTemplate") RedisTemplate<String, Object> redis,
                                                         SecurityProperties properties,
                                                         UserOnlineConverter userOnlineConverter,
                                                         ObjectProvider<SecurityUserLoader> securityUserLoaderProvider) {
        return new RedisSecHolderStrategy(om, redis, properties, userOnlineConverter,
                securityUserLoaderProvider.getIfAvailable());
    }
}
