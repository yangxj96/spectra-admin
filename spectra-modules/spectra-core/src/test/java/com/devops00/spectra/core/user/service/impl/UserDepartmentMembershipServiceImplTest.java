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

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.mapper.DepartmentMapper;
import com.devops00.spectra.core.user.mapper.UserDepartmentMembershipMapper;
import com.devops00.spectra.core.user.service.UserDepartmentMembershipService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证用户关联部门的校验、替换和事务边界。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/27
 */
class UserDepartmentMembershipServiceImplTest {

    private static final UUID USER_ID = UUID.randomUUID();
    private static final UUID OPERATOR_ID = UUID.randomUUID();
    private static final UUID PRIMARY_ID = UUID.randomUUID();
    private static final UUID ASSOCIATED_ID = UUID.randomUUID();

    private DepartmentMapper departmentMapper;
    private UserDepartmentMembershipMapper membershipMapper;
    private UserDepartmentMembershipService membershipService;

    @BeforeEach
    void setUp() {
        departmentMapper = mock(DepartmentMapper.class);
        membershipMapper = mock(UserDepartmentMembershipMapper.class);
        membershipService = new UserDepartmentMembershipServiceImpl(departmentMapper, membershipMapper);
    }

    @Test
    void rejectsUnknownOrDeletedDepartment() {
        when(departmentMapper.selectActiveIdsByIds(List.of(PRIMARY_ID, ASSOCIATED_ID)))
                .thenReturn(List.of(PRIMARY_ID));

        assertThrows(DataException.class,
                () -> membershipService.replace(USER_ID, PRIMARY_ID, List.of(ASSOCIATED_ID), OPERATOR_ID));

        verify(membershipMapper, never()).softDeleteActiveByUserId(USER_ID, OPERATOR_ID);
    }

    @Test
    void rejectsDuplicateIds() {
        assertThrows(DataException.class,
                () -> membershipService.replace(USER_ID, PRIMARY_ID,
                        List.of(ASSOCIATED_ID, ASSOCIATED_ID), OPERATOR_ID));

        verify(departmentMapper, never()).selectActiveIdsByIds(anyList());
        verify(membershipMapper, never()).softDeleteActiveByUserId(USER_ID, OPERATOR_ID);
    }

    @Test
    void rejectsPrimaryRepeatedAsAssociated() {
        assertThrows(DataException.class,
                () -> membershipService.replace(USER_ID, PRIMARY_ID, List.of(PRIMARY_ID), OPERATOR_ID));

        verify(departmentMapper, never()).selectActiveIdsByIds(anyList());
        verify(membershipMapper, never()).softDeleteActiveByUserId(USER_ID, OPERATOR_ID);
    }

    @Test
    void emptyListRemovesAllAssociatedDepartments() {
        when(departmentMapper.selectActiveIdsByIds(List.of(PRIMARY_ID))).thenReturn(List.of(PRIMARY_ID));

        membershipService.replace(USER_ID, PRIMARY_ID, List.of(), OPERATOR_ID);

        verify(membershipMapper).softDeleteActiveByUserId(USER_ID, OPERATOR_ID);
        verify(membershipMapper, never()).batchInsertAssociated(eq(USER_ID), anyList(), eq(OPERATOR_ID));
    }

    @Test
    void writeFailureRollsBackReplacement() throws NoSuchMethodException {
        when(departmentMapper.selectActiveIdsByIds(List.of(PRIMARY_ID, ASSOCIATED_ID)))
                .thenReturn(List.of(PRIMARY_ID, ASSOCIATED_ID));
        when(membershipMapper.batchInsertAssociated(USER_ID, List.of(ASSOCIATED_ID), OPERATOR_ID))
                .thenThrow(new DataException("关联部门写入失败"));

        assertThrows(DataException.class,
                () -> membershipService.replace(USER_ID, PRIMARY_ID, List.of(ASSOCIATED_ID), OPERATOR_ID));

        verify(membershipMapper).softDeleteActiveByUserId(USER_ID, OPERATOR_ID);
        assertTrue(UserDepartmentMembershipServiceImpl.class
                .getMethod("replace", UUID.class, UUID.class, List.class, UUID.class)
                .isAnnotationPresent(Transactional.class));
    }

}
