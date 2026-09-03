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

package com.devops00.test.spectra;

import com.devops00.spectra.core.CoreModule;
import com.devops00.spectra.core.system.service.impl.RegionServiceImpl;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.security.authentication.javabean.entity.SecurityUser;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;

import java.util.UUID;

/** 行政区划导入专用测试上下文，避免加载无关业务和其他集成测试配置。 */
@SpringBootConfiguration
@EnableAutoConfiguration(exclude = CoreModule.class)
@ComponentScan("com.devops00.spectra.core.system.javabean.converter")
@MapperScan("com.devops00.spectra.core.system.mapper")
@Import(RegionServiceImpl.class)
public class RegionImportTestApplication {

    @Bean
    SecurityContextAccessor securityContextAccessor() {
        return new SecurityContextAccessor() {
            @Override
            public SecurityUser currentUser() {
                return null;
            }

            @Override
            public UUID currentUserId() {
                return null;
            }

            @Override
            public String currentToken() {
                return null;
            }

            @Override
            public String currentUserZoneId() {
                return "UTC";
            }

            @Override
            public String currentUsername() {
                return "";
            }
        };
    }
}
