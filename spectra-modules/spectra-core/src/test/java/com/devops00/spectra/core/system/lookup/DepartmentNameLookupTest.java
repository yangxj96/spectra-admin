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

package com.devops00.spectra.core.system.lookup;

import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DepartmentNameLookupTest {

    @Test
    void resolvesNameForSoftDeletedDepartment() {
        var mapper = mock(DepartmentMapper.class);
        var id = UUID.randomUUID();
        var department = new Department();
        department.setId(id);
        department.setDeleted(java.time.Instant.now());
        department.setPath("旧部门路径");
        when(mapper.selectByIdsIncludingDeleted(List.of(id))).thenReturn(List.of(department));

        var names = new DepartmentNameLookup(mapper).getNameMap(Set.of(id));

        assertEquals("旧部门路径", names.get(id));
        verify(mapper).selectByIdsIncludingDeleted(List.of(id));
    }
}
