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

import com.baomidou.mybatisplus.core.conditions.Wrapper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.security.authorization.constant.SecurityAuthorizationState;
import com.devops00.spectra.core.security.authorization.javabean.vo.AuthorizationProfileVO;
import com.devops00.spectra.core.security.authorization.service.AuthorizationProfileService;
import com.devops00.spectra.core.security.authentication.service.AuthenticationIdentityService;
import com.devops00.spectra.core.system.javabean.entity.Department;
import com.devops00.spectra.core.system.javabean.vo.DictItemVO;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.system.service.DictService;
import com.devops00.spectra.core.user.javabean.entity.UserImportRow;
import com.devops00.spectra.core.user.javabean.entity.UserImportTask;
import com.devops00.spectra.core.user.javabean.from.UserImportPreviewFrom;
import com.devops00.spectra.core.user.javabean.from.UserImportRowFrom;
import com.devops00.spectra.core.user.javabean.vo.UserImportTaskVO;
import com.devops00.spectra.core.user.mapper.UserImportRowMapper;
import com.devops00.spectra.core.user.mapper.UserImportTaskMapper;
import com.devops00.spectra.core.user.mapper.UserMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * 验证导入 Preview 对关联部门编码的解析、校验和请求摘要绑定。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/27
 */
class UserImportDepartmentValidationTest {

    private static final UUID OPERATOR_ID = UUID.randomUUID();

    private UserImportTaskMapper taskMapper;
    private UserImportRowMapper rowMapper;
    private DepartmentService departmentService;
    private UserImportPreviewService previewService;

    @BeforeEach
    void setUp() {
        taskMapper = mock(UserImportTaskMapper.class);
        rowMapper = mock(UserImportRowMapper.class);
        var userMapper = mock(UserMapper.class);
        var identityService = mock(AuthenticationIdentityService.class);
        departmentService = mock(DepartmentService.class);
        var dictService = mock(DictService.class);
        var profileService = mock(AuthorizationProfileService.class);
        var securityContextAccessor = mock(SecurityContextAccessor.class);
        var resultService = mock(UserImportResultService.class);

        previewService = new UserImportPreviewService(taskMapper, rowMapper, userMapper, identityService,
                departmentService, dictService, profileService, securityContextAccessor, resultService);
        when(securityContextAccessor.currentUserId()).thenReturn(OPERATOR_ID);
        when(taskMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(taskMapper.insert(any(UserImportTask.class))).thenAnswer(invocation -> {
            ((UserImportTask) invocation.getArgument(0)).setId(UUID.randomUUID());
            return 1;
        });
        when(taskMapper.updateById(any(UserImportTask.class))).thenReturn(1);
        when(rowMapper.insert(any(UserImportRow.class))).thenReturn(1);
        when(userMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        when(identityService.findIdentity(anyString(), anyString())).thenReturn(null);
        when(departmentService.list()).thenReturn(List.of(department("MAIN"), department("ASSOC"), department("OTHER")));
        when(dictService.listDictDataByGroupCode("sys_language")).thenReturn(List.of(dictItem("en")));
        when(dictService.listDictDataByGroupCode("sys_timezone")).thenReturn(List.of(dictItem("UTC")));
        when(profileService.all()).thenReturn(List.of(profile()));
        when(resultService.toVO(any(UserImportTask.class), anyString())).thenReturn(new UserImportTaskVO());
    }

    @Test
    void parsesMultipleAssociatedDepartmentCodes() {
        previewService.preview(request("ASSOC; OTHER"));

        var row = capturedRow();
        assertEquals("ASSOC;OTHER", row.getNormalizedData().get("associated_department_codes"));
        assertEquals("VALID", row.getState());
    }

    @Test
    void rejectsEmptySegmentsAndDuplicateCodes() {
        previewService.preview(request("ASSOC;;OTHER"));
        assertTrue(validationErrors(capturedRow()).contains("关联部门编码不能包含空项"));

        when(taskMapper.selectOne(any(Wrapper.class))).thenReturn(null);
        previewService.preview(request("ASSOC;ASSOC"));
        assertTrue(validationErrors(capturedRow()).contains("关联部门编码不能重复"));
    }

    @Test
    void rejectsUnknownDepartmentCodes() {
        previewService.preview(request("MISSING"));

        assertTrue(validationErrors(capturedRow()).contains("关联部门编码不存在"));
    }

    @Test
    void rejectsPrimaryRepeatedAsAssociated() {
        previewService.preview(request("MAIN"));

        assertTrue(validationErrors(capturedRow()).contains("主部门不能重复作为关联部门"));
    }

    @Test
    void bindsAssociatedCodesIntoPreviewRequestHash() {
        var first = request("ASSOC");
        previewService.preview(first);
        var createdTask = org.mockito.ArgumentCaptor.forClass(UserImportTask.class);
        org.mockito.Mockito.verify(taskMapper).insert(createdTask.capture());
        var persistedTask = createdTask.getValue();
        when(taskMapper.selectOne(any(Wrapper.class))).thenReturn(persistedTask);

        var changed = request("OTHER");
        changed.setIdempotencyKey(first.getIdempotencyKey());
        assertThrows(DataException.class, () -> previewService.preview(changed));
    }

    private UserImportRow capturedRow() {
        var row = org.mockito.ArgumentCaptor.forClass(UserImportRow.class);
        org.mockito.Mockito.verify(rowMapper, org.mockito.Mockito.atLeastOnce()).insert(row.capture());
        return row.getValue();
    }

    @SuppressWarnings("unchecked")
    private static List<String> validationErrors(UserImportRow row) {
        return (List<String>) row.getErrors().get("validation");
    }

    private static UserImportPreviewFrom request(String associatedDepartmentCodes) {
        var row = new UserImportRowFrom();
        row.setRealName("测试用户");
        row.setUsername("user@example.test");
        row.setPhone("13800138000");
        row.setEmail("user@example.test");
        row.setDepartmentCode("MAIN");
        row.setAssociatedDepartmentCodes(associatedDepartmentCodes);
        row.setLanguage("en");
        row.setTimezone("UTC");
        row.setAuthorizationProfileCode("BASIC");

        var request = new UserImportPreviewFrom();
        request.setIdempotencyKey(UUID.randomUUID().toString());
        request.setFileName("users.xlsx");
        request.setFileHash("file-hash");
        request.setRows(List.of(row));
        return request;
    }

    private static Department department(String code) {
        var department = new Department();
        department.setId(UUID.nameUUIDFromBytes(code.getBytes()));
        department.setCode(code);
        department.setName(code);
        return department;
    }

    private static DictItemVO dictItem(String value) {
        return new DictItemVO(UUID.randomUUID(), UUID.randomUUID(), value, value, (short) 1, (short) 1, false, null);
    }

    private static AuthorizationProfileVO profile() {
        var profile = new AuthorizationProfileVO();
        profile.setCode("BASIC");
        profile.setState(SecurityAuthorizationState.ACTIVE.name());
        return profile;
    }
}
