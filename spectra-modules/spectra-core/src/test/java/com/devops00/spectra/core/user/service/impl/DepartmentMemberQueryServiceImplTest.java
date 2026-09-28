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

package com.devops00.spectra.core.user.service.impl;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.devops00.spectra.common.exception.DataNotExistException;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.user.javabean.from.DepartmentMemberPageFrom;
import com.devops00.spectra.core.user.javabean.vo.DepartmentMemberCandidateVO;
import com.devops00.spectra.core.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证部门成员候选查询校验部门状态并委托精确直属查询。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/28
 */
class DepartmentMemberQueryServiceImplTest {

    private static final UUID DEPARTMENT_ID = UUID.randomUUID();

    private DepartmentMapper departmentMapper;
    private UserMapper userMapper;
    private DepartmentMemberQueryServiceImpl service;

    @BeforeEach
    void setUp() {
        departmentMapper = mock(DepartmentMapper.class);
        userMapper = mock(UserMapper.class);
        service = new DepartmentMemberQueryServiceImpl(departmentMapper, userMapper);
    }

    @Test
    void pageReturnsMapperPageForActiveDepartmentWithoutFilteringAccountStatus() {
        var request = new DepartmentMemberPageFrom();
        request.setDepartmentId(DEPARTMENT_ID);
        request.setKeyword("lee");
        request.setPageNum(2L);
        request.setPageSize(25L);
        var expected = new Page<DepartmentMemberCandidateVO>(2L, 25L);
        expected.setTotal(1L);
        when(departmentMapper.selectActiveIdsByIds(List.of(DEPARTMENT_ID))).thenReturn(List.of(DEPARTMENT_ID));
        when(userMapper.selectDepartmentMemberCandidates(any(), eq(DEPARTMENT_ID), eq("lee"))).thenReturn(expected);

        var actual = service.page(request);

        assertSame(expected, actual);
        verify(userMapper).selectDepartmentMemberCandidates(any(), eq(DEPARTMENT_ID), eq("lee"));
    }

    @Test
    void rejectsDeletedDepartmentBeforeQueryingMembers() {
        var request = new DepartmentMemberPageFrom();
        request.setDepartmentId(DEPARTMENT_ID);
        when(departmentMapper.selectActiveIdsByIds(List.of(DEPARTMENT_ID))).thenReturn(List.of());

        assertThrows(DataNotExistException.class, () -> service.page(request));

        verify(userMapper, never()).selectDepartmentMemberCandidates(any(), eq(DEPARTMENT_ID), any());
    }

}
