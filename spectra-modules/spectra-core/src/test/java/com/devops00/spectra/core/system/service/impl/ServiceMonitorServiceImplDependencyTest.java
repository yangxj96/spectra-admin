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

package com.devops00.spectra.core.system.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.health.actuate.endpoint.HealthEndpoint;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;

/** 服务监控与 Actuator 健康聚合器之间的 Bean 拓扑测试。 */
class ServiceMonitorServiceImplDependencyTest {

    @Test
    void healthEndpointIsResolvedLazilyToAvoidSchedulerHealthCycle() {
        var constructor = Arrays.stream(ServiceMonitorServiceImpl.class.getDeclaredConstructors())
                .filter(item -> item.getParameterCount() == 8)
                .findFirst()
                .orElseThrow();

        assertThat(Arrays.stream(constructor.getParameterTypes()).anyMatch(HealthEndpoint.class::equals))
                .as("ServiceMonitorServiceImpl 不得在构造阶段创建 HealthEndpoint")
                .isFalse();
        assertThat(Arrays.stream(constructor.getGenericParameterTypes())
                .anyMatch(ServiceMonitorServiceImplDependencyTest::isHealthEndpointProvider))
                .as("ServiceMonitorServiceImpl 应通过 ObjectProvider 延迟获取 HealthEndpoint")
                .isTrue();
    }

    private static boolean isHealthEndpointProvider(Type type) {
        if (!(type instanceof ParameterizedType parameterizedType)
                || parameterizedType.getRawType() != ObjectProvider.class) {
            return false;
        }
        var arguments = parameterizedType.getActualTypeArguments();
        return arguments.length == 1 && arguments[0] == HealthEndpoint.class;
    }
}
