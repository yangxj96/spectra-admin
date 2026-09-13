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

package com.devops00.spectra.common.security.authorization;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 承载范围查询相关的不可变数据。
 *
 * @param subjectId         数据记录的唯一标识
 * @param ownerId           所有者标识
 * @param departmentId      部门标识
 * @param departmentLineage 数据范围使用的部门层级标识路径
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
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
