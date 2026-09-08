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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
        var lookup = new FixtureLookup(Map.of(firstId, "部门一", secondId, "部门二"));

        var first = new FixtureView(firstId);
        var second = new FixtureView(secondId);
        var duplicate = new FixtureView(firstId);
        new NameFillExecutor(new NameLookupRegistry(List.of(lookup))).fill(List.of(first, second, duplicate));

        assertThat(first.name).isEqualTo("部门一");
        assertThat(second.name).isEqualTo("部门二");
        assertThat(duplicate.name).isEqualTo("部门一");
        assertThat(lookup.calls).isEqualTo(1);
        assertThat(lookup.queriedIds).containsExactlyInAnyOrder(firstId, secondId);
    }

    @Test
    void executorMustIgnoreNullAndWronglyTypedSourceValues() throws IllegalAccessException {
        var lookup = new FixtureLookup(Map.of());

        var view = new FixtureView(null);
        var wrongTypeView = new FixtureView("not-a-uuid");
        new NameFillExecutor(new NameLookupRegistry(List.of(lookup))).fill(List.of(view, wrongTypeView));

        assertThat(view.name).isNull();
        assertThat(wrongTypeView.name).isNull();
        assertThat(lookup.calls).isZero();
    }

    @Test
    void executorMustReturnWithoutLookupForEmptyInput() throws IllegalAccessException {
        var lookup = new FixtureLookup(Map.of());

        new NameFillExecutor(new NameLookupRegistry(List.of(lookup))).fill(List.of());

        assertThat(lookup.calls).isZero();
    }

    @Test
    void executorMustConvertStringKeysBackToUuidKeysBeforeFilling() throws IllegalAccessException {
        UUID departmentId = UUID.randomUUID();
        var lookup = new StringKeyFixtureLookup(departmentId, "部门一");
        var view = new StringKeyFixtureView(departmentId);

        new NameFillExecutor(new NameLookupRegistry(List.of(lookup))).fill(List.of(view));

        assertThat(view.name).isEqualTo("部门一");
        assertThat(lookup.calls).isEqualTo(1);
    }

    static final class FixtureLookup implements NameLookup<UUID> {

        private final Map<UUID, String> names;
        private Set<UUID> queriedIds = Set.of();
        private int calls;

        FixtureLookup(Map<UUID, String> names) {
            this.names = names;
        }

        @Override
        public Map<UUID, String> getNameMap(Set<UUID> ids) {
            calls++;
            queriedIds = Set.copyOf(ids);
            return names;
        }
    }

    static final class StringKeyFixtureLookup implements NameLookup<UUID> {

        private final UUID departmentId;
        private final String name;
        private int calls;

        StringKeyFixtureLookup(UUID departmentId, String name) {
            this.departmentId = departmentId;
            this.name = name;
        }

        @Override
        @SuppressWarnings("unchecked")
        public Map<UUID, String> getNameMap(Set<UUID> ids) {
            calls++;
            return (Map<UUID, String>) (Map<?, ?>) Map.of(departmentId.toString(), name);
        }
    }

    static final class FixtureView {

        private final Object departmentId;

        @NameFill(lookup = FixtureLookup.class, sourceField = "departmentId")
        private String name;

        FixtureView(Object departmentId) {
            this.departmentId = departmentId;
        }
    }

    static final class StringKeyFixtureView {

        private final UUID departmentId;

        @NameFill(lookup = StringKeyFixtureLookup.class, sourceField = "departmentId")
        private String name;

        StringKeyFixtureView(UUID departmentId) {
            this.departmentId = departmentId;
        }
    }
}
