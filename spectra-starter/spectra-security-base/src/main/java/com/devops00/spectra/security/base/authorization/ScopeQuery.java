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

package com.devops00.spectra.security.base.authorization;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 资源访问判断所需的最小上下文。departmentLineage 应包含资源部门自身及其祖先节点。
 */
public record ScopeQuery(UUID subjectId, UUID ownerId, UUID departmentId, Set<UUID> departmentLineage) {

    public ScopeQuery {
        departmentLineage = departmentLineage == null
                ? Set.of()
                : Collections.unmodifiableSet(new LinkedHashSet<>(departmentLineage));
    }

    @Override
    public Set<UUID> departmentLineage() {
        return Collections.unmodifiableSet(new LinkedHashSet<>(departmentLineage));
    }
}
