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

package com.devops00.spectra.framework.assembler;

import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * NameFill 注解契约和执行器局部状态测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
class NameFillExecutorTest {

    @Test
    void executorMustBatchLookupAndFillOnlyTheAnnotatedDisplayField() throws IllegalAccessException {
        UUID firstId = UUID.randomUUID();
        UUID secondId = UUID.randomUUID();
        var lookup = mock(FixtureLookup.class);
        var context = mock(ApplicationContext.class);
        when(context.getBean(FixtureLookup.class)).thenReturn(lookup);
        when(lookup.idType()).thenReturn(UUID.class);
        when(lookup.getNameMap(Set.of(firstId, secondId)))
                .thenReturn(Map.of(firstId, "部门一", secondId, "部门二"));

        var first = new FixtureView(firstId);
        var second = new FixtureView(secondId);
        new NameFillExecutor(context).fill(List.of(first, second));

        assertThat(first.name).isEqualTo("部门一");
        assertThat(second.name).isEqualTo("部门二");
        verify(lookup).getNameMap(Set.of(firstId, secondId));
    }

    @Test
    void executorMustIgnoreNullAndWronglyTypedSourceValues() throws IllegalAccessException {
        var lookup = mock(FixtureLookup.class);
        var context = mock(ApplicationContext.class);
        when(context.getBean(FixtureLookup.class)).thenReturn(lookup);
        when(lookup.idType()).thenReturn(UUID.class);

        var view = new FixtureView(null);
        new NameFillExecutor(context).fill(List.of(view));

        assertThat(view.name).isNull();
    }

    static final class FixtureLookup implements NameLookup<UUID> {

        @Override
        public Map<UUID, String> getNameMap(Set<UUID> ids) {
            return Map.of();
        }
    }

    private static final class FixtureView {

        private final UUID departmentId;

        @NameFill(lookup = FixtureLookup.class, sourceField = "departmentId")
        private String name;

        private FixtureView(UUID departmentId) {
            this.departmentId = departmentId;
        }
    }
}
