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

package com.devops00.spectra.framework.persistence.configuration;

import com.devops00.spectra.framework.persistence.scope.context.DataScopeEntityRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;

import java.lang.reflect.Modifier;

import static org.assertj.core.api.Assertions.assertThat;

/** MyBatis 基础设施构造器装配回归测试。 */
class MyBatisPlusConfigurationTest {

    @Test
    void shouldUseFinalFieldsAndOneConstructorForInfrastructureDependencies() {
        var fields = new String[]{"innerInterceptors", "authorizationSnapshotProvider", "dataScopeEntityRegistry"};

        for (String fieldName : fields) {
            try {
                var field = MyBatisPlusConfiguration.class.getDeclaredField(fieldName);
                assertThat(Modifier.isFinal(field.getModifiers())).as("字段必须通过构造器注入: %s", fieldName).isTrue();
                assertThat(field.getAnnotations()).as("字段不得使用注入注解: %s", fieldName).isEmpty();
            } catch (NoSuchFieldException exception) {
                throw new AssertionError(exception);
            }
        }

        var constructors = MyBatisPlusConfiguration.class.getDeclaredConstructors();
        assertThat(constructors).hasSize(1);
        assertThat(constructors[0].getParameterTypes()).containsExactly(
                ObjectProvider.class, ObjectProvider.class, DataScopeEntityRegistry.class);
    }
}
