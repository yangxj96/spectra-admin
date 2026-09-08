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

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * 部门名称 Lookup 的批量查询和空输入测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/08
 */
class DepartmentNameLookupTest {

    @BeforeAll
    static void registerMybatisLambdaMetadata() {
        var assistant = new MapperBuilderAssistant(new MybatisConfiguration(), "department-name-lookup-test");
        TableInfoHelper.initTableInfo(assistant, Department.class);
    }

    @Test
    void lookupMustReturnDepartmentPathsFromOneMapperQuery() {
        UUID departmentId = UUID.randomUUID();
        var department = new Department();
        department.setId(departmentId);
        department.setPath("光谱平台/研发中心");
        var mapper = mock(DepartmentMapper.class);
        when(mapper.selectList(any())).thenReturn(List.of(department));

        var result = new DepartmentNameLookup(mapper).getNameMap(Set.of(departmentId));

        assertThat(result).containsEntry(departmentId, "光谱平台/研发中心");
        verify(mapper).selectList(any());
    }

    @Test
    void lookupMustReturnEmptyMapWithoutQueryForEmptyIds() {
        var mapper = mock(DepartmentMapper.class);

        var result = new DepartmentNameLookup(mapper).getNameMap(Set.of());

        assertThat(result).isEmpty();
        verifyNoInteractions(mapper);
    }
}
