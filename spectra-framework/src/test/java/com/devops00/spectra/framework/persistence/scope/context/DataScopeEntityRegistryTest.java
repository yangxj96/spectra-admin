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

package com.devops00.spectra.framework.persistence.scope.context;

import org.junit.jupiter.api.Test;

import java.lang.reflect.InvocationTargetException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/** 数据权限实体扫描失败必须显式阻断启动的回归测试。 */
class DataScopeEntityRegistryTest {

    @Test
    void shouldFailClosedWhenADataScopeEntityCannotBeLoaded() throws Exception {
        var register = DataScopeEntityRegistry.class.getDeclaredMethod("register", String.class);
        register.setAccessible(true);
        var registry = new DataScopeEntityRegistry();

        assertThatThrownBy(() -> register.invoke(registry, "com.devops00.spectra.missing.RequiredEntity"))
                .isInstanceOf(InvocationTargetException.class)
                .satisfies(exception -> assertThat(exception.getCause())
                        .isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("数据权限实体加载失败"));
    }
}
