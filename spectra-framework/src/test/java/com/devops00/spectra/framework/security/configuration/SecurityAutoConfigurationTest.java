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

package com.devops00.spectra.framework.security.configuration;

import com.devops00.spectra.framework.FrameworkModule;
import com.devops00.spectra.framework.security.redis.store.RedisSecurityInitializationTokenStore;
import com.devops00.spectra.framework.security.redis.store.RedisSecurityVerificationStore;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Import;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Framework 根包扫描下的 Security 组件装配回归测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/03
 */
class SecurityAutoConfigurationTest {

    @Test
    void shouldDiscoverRedisSecurityAdaptersThroughFrameworkRootScan() {
        ComponentScan componentScan = FrameworkModule.class.getAnnotation(ComponentScan.class);
        assertThat(componentScan).isNotNull();
        assertThat(componentScan.basePackages()).isEmpty();
        assertThat(componentScan.basePackageClasses()).containsExactly(FrameworkModule.class);

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

    @Test
    void securityAutoConfigurationMustNotMaintainASecondScanOrImportEntry() {
        assertThat(SecurityAutoConfiguration.class.getAnnotation(ComponentScan.class)).isNull();
        assertThat(SecurityAutoConfiguration.class.getAnnotation(Import.class)).isNull();
    }

    @Test
    void invalidSecurityRedisContractMustFailDuringContextStartup() {
        new ApplicationContextRunner()
                .withUserConfiguration(SecurityAutoConfiguration.class)
                .withPropertyValues("spectra.security.redis.namespace=")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure()
                        .hasRootCauseMessage("安全 Redis namespace 必须固定为 sec:"));
    }

    @Test
    void invalidSecurityTtlAndAttemptsMustFailDuringContextStartup() {
        new ApplicationContextRunner()
                .withUserConfiguration(SecurityAutoConfiguration.class)
                .withPropertyValues(
                        "spectra.security.min-effective-dev-ops-users=4",
                        "spectra.security.max-dev-ops-users=3")
                .run(context -> assertThat(context).hasFailed()
                        .getFailure()
                        .hasRootCauseMessage("DevOps Root 用户数量边界无效"));
    }
}
