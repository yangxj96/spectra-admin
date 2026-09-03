/*
 * Copyright 2018-2026 yangxj96
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.devops00.spectra.framework.configure.security.autoconfiguration;

import com.devops00.spectra.framework.configure.security.redis.RedisSecurityInitializationTokenStore;
import com.devops00.spectra.framework.configure.security.redis.RedisSecurityVerificationStore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Security 自动配置组件扫描回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
class SecurityAutoConfigurationTest {

    @Test
    void shouldDiscoverRedisSecurityAdaptersThroughAutoConfigurationScan() {
        ComponentScan componentScan = SecurityAutoConfiguration.class.getAnnotation(ComponentScan.class);
        var scanner = new ClassPathScanningCandidateComponentProvider(false);
        scanner.addIncludeFilter(new AnnotationTypeFilter(org.springframework.stereotype.Component.class));

        Set<String> beanClassNames = Arrays.stream(componentScan.basePackageClasses())
                .flatMap(basePackageClass -> scanner
                        .findCandidateComponents(basePackageClass.getPackageName())
                        .stream())
                .map(BeanDefinition::getBeanClassName)
                .collect(Collectors.toSet());

        assertThat(beanClassNames)
                .contains(RedisSecurityVerificationStore.class.getName(),
                        RedisSecurityInitializationTokenStore.class.getName());
    }
}
