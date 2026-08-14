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

package com.devops00.spectra.framework;

import com.devops00.spectra.common.properties.SystemProperties;
import com.devops00.spectra.framework.configure.mvc.MvcConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * CORS 安全配置回归测试。
 */
class CorsConfigurationSecurityTest {

    @Test
    void shouldRegisterOnlyExactOrigins() {
        var properties = new SystemProperties();
        properties.getCors().setOriginPatterns(List.of("https://admin.example.com"));
        var registry = new InspectableCorsRegistry();

        new MvcConfiguration(properties).addCorsMappings(registry);

        CorsConfiguration configuration = registry.configurations().get("/**");
        assertEquals(List.of("https://admin.example.com"), configuration.getAllowedOrigins());
        assertNull(configuration.getAllowedOriginPatterns());
        assertEquals(Boolean.TRUE, configuration.getAllowCredentials());
    }

    @Test
    void shouldRejectWildcardOriginWhenCredentialsAreEnabled() {
        var properties = new SystemProperties();
        properties.getCors().setOriginPatterns(List.of("https://*.example.com"));

        assertThrows(IllegalStateException.class, () -> new MvcConfiguration(properties)
                .addCorsMappings(new InspectableCorsRegistry()));
    }

    private static final class InspectableCorsRegistry extends CorsRegistry {

        private Map<String, CorsConfiguration> configurations() {
            return getCorsConfigurations();
        }
    }
}
