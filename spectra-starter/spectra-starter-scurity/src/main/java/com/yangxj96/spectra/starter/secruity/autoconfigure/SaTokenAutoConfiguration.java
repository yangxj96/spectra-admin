/*
 *  Copyright 2025 yangxj96.com
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
 *
 */

package com.yangxj96.spectra.starter.secruity.autoconfigure;

import cn.dev33.satoken.log.SaLog;
import com.yangxj96.spectra.starter.secruity.configure.SaLogForSlf4j;
import com.yangxj96.spectra.starter.secruity.properties.UserProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * SaToken相关配置
 *
 * @author Jack Young
 * @version 1.0
 * @since 2025-6-14
 */
@Slf4j
@EnableConfigurationProperties(UserProperties.class)
public class SaTokenAutoConfiguration {

    private static final String PREFIX = "[SaToken]:";

    /**
     * 日志转接到Spring
     *
     * @return {@link SaLog}
     */
    @Bean
    public SaLog saLog() {
        log.atDebug().log(PREFIX + "载入SaToken日志转接到Spring");
        return new SaLogForSlf4j();
    }

    /**
     * 密码加解密
     *
     * @return {@link BCryptPasswordEncoder}
     */
    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        log.atDebug().log(PREFIX + "加载密码加解密工具");
        return new BCryptPasswordEncoder();
    }


}
