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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * NameLookupRegistry 的显式类型解析和启动校验测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
class NameLookupRegistryTest {

    @Test
    void registryMustResolveLookupByItsImplementationType() {
        var lookup = new FixtureLookup();
        var registry = new NameLookupRegistry(List.of(lookup));

        assertThat(registry.require(FixtureLookup.class)).isSameAs(lookup);
    }

    @Test
    void registryMustRejectUnknownLookupType() {
        var registry = new NameLookupRegistry(List.of(new FixtureLookup()));

        assertThatThrownBy(() -> registry.require(UnknownLookup.class))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining(UnknownLookup.class.getName());
    }

    @Test
    void registryMustRejectDuplicateLookupImplementationTypes() {
        assertThatThrownBy(() -> new NameLookupRegistry(List.of(new FixtureLookup(), new FixtureLookup())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining(FixtureLookup.class.getName());
    }

    static final class FixtureLookup implements NameLookup<UUID> {

        @Override
        public Map<UUID, String> getNameMap(Set<UUID> ids) {
            return Map.of();
        }
    }

    static final class UnknownLookup implements NameLookup<UUID> {

        @Override
        public Map<UUID, String> getNameMap(Set<UUID> ids) {
            return Map.of();
        }
    }
}
