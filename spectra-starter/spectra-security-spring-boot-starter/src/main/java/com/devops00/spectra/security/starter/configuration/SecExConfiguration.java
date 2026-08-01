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


import com.devops00.spectra.security.starter.advice.RestAccessDeniedHandler;
import com.devops00.spectra.security.starter.advice.RestAuthenticationEntryPoint;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.access.AccessDeniedHandler;
import tools.jackson.databind.ObjectMapper;

/// Security异常配置
///
/// @author yangxj96
/// @version 1.0
/// @since 2026/3/9 00:35
@Configuration
public class SecExConfiguration {

    /// 身份认证异常
    @Bean
    public AuthenticationEntryPoint restAuthenticationEntryPoint(ObjectMapper om) {
        return new RestAuthenticationEntryPoint(om);
    }

    /// 权限不足异常
    @Bean
    public AccessDeniedHandler restAccessDeniedHandler(ObjectMapper om) {
        return new RestAccessDeniedHandler(om);
    }

}
