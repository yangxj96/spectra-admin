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

package com.devops00.spectra.framework.configure.security.autoconfiguration;

import com.devops00.spectra.framework.configure.security.properties.SecurityProperties;
import com.devops00.spectra.common.security.authorization.RootAuthorizationPolicy;
import com.devops00.spectra.framework.configure.security.advice.LoginExceptionAdvice;
import com.devops00.spectra.framework.configure.security.configuration.SecurityConfiguration;
import com.devops00.spectra.framework.configure.security.converter.UserOnlineConverter;
import com.devops00.spectra.framework.configure.security.root.DefaultRootAuthorizationPolicy;
import com.devops00.spectra.framework.configure.security.redis.RedisSecurityVerificationStore;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

/**
 * SpringSecurity配置
 *
 * @author yangxj96
 * @version 1.0
 * @since 2025/12/2 17:31
 */
@Slf4j
@AutoConfiguration
@EnableConfigurationProperties(SecurityProperties.class)
@Import({SecurityConfiguration.class})
@ComponentScan(basePackageClasses = {LoginExceptionAdvice.class, UserOnlineConverter.class,
        RedisSecurityVerificationStore.class})
public class SecurityAutoConfiguration {

    /**
     * 统一 Root 判定入口。Root 仍必须经过审计、Session 和 DataScope 等其他安全边界。
     */
    @Bean
    public RootAuthorizationPolicy rootAuthorizationPolicy() {
        return new DefaultRootAuthorizationPolicy();
    }
}
