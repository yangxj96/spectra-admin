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

package com.devops00.spectra.upload;

import com.devops00.spectra.core.CoreModule;
import com.devops00.spectra.security.base.change.SecuritySessionRevocationPort;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import com.devops00.spectra.security.base.javabean.entity.SecurityUser;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

/**
 * 单元测试使用
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/3/8 01:16
 */
@SpringBootApplication(exclude = CoreModule.class)
@ComponentScan("com.devops00.spectra.upload")
public class UploadTestApplication {

    /**
     * 上传模块测试不加载安全 Starter，提供无状态上下文以满足框架审计字段和数据权限组件的依赖。
     */
    @Bean
    SecurityContextAccessor securityContextAccessor() {
        return new SecurityContextAccessor() {
            @Override
            public SecurityUser currentUser() {
                return null;
            }

            @Override
            public java.util.UUID currentUserId() {
                return null;
            }

            @Override
            public String currentToken() {
                return null;
            }

            @Override
            public String currentUserZoneId() {
                return "";
            }

            @Override
            public String currentUsername() {
                return "";
            }
        };
    }

    @Bean
    SecuritySessionRevocationPort securitySessionRevocationPort() {
        return userId -> {
            // 测试应用不连接安全会话存储。
        };
    }
}
