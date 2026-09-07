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

package com.devops00.spectra.framework.persistence.scope.authorization;

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.framework.persistence.scope.context.DataScopeEntityRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/** 数据权限拦截器生产构造路径回归测试。 */
class DataScopeInnerInterceptorTest {

    @Test
    void shouldExposeOnlyTheCompleteProductionConstructor() {
        var constructors = DataScopeInnerInterceptor.class.getDeclaredConstructors();

        assertThat(constructors).hasSize(1);
        assertThat(constructors[0].getParameterTypes()).containsExactly(
                ObjectProvider.class, DataScopeEntityRegistry.class, SecurityContextAccessor.class);
        assertThat(constructors[0].getAnnotation(Autowired.class)).isNull();
    }
}
