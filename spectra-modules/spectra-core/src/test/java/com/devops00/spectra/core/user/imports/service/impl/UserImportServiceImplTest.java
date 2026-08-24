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

package com.devops00.spectra.core.user.imports.service.impl;

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.common.mybatis.handler.UUIDTypeHandler;
import com.devops00.spectra.core.security.authorization.service.AuthorizationProfileService;
import com.devops00.spectra.core.system.service.DepartmentService;
import com.devops00.spectra.core.system.service.DictService;
import com.devops00.spectra.core.user.imports.entity.UserImportRow;
import com.devops00.spectra.core.user.imports.entity.UserImportTask;
import com.devops00.spectra.core.user.imports.javabean.from.UserImportApplyFrom;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportTaskVO;
import com.devops00.spectra.core.user.imports.mapper.UserImportRowMapper;
import com.devops00.spectra.core.user.imports.mapper.UserImportTaskMapper;
import com.devops00.spectra.core.user.mapper.UserMapper;
import com.devops00.spectra.framework.configure.mapstruct.TimeMapper;
import com.devops00.spectra.security.base.holder.SecurityContextAccessor;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import com.devops00.spectra.common.utils.SHA256Utils;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserImportServiceImplTest {

    private static final UUID OPERATOR_ID = UUID.randomUUID();

    @Mock
    private UserImportTaskMapper taskMapper;

    @Mock
    private UserImportRowMapper rowMapper;

    @Mock
    private UserImportRowProcessor rowProcessor;

    @Mock
    private UserMapper userMapper;

    @Mock
    private DepartmentService departmentService;

    @Mock
    private DictService dictService;

    @Mock
    private AuthorizationProfileService profileService;

    @Mock
    private SecurityContextAccessor securityContextAccessor;

    @Mock
    private TimeMapper timeMapper;

    @Mock
    private TaskExecutor taskExecutor;

    @InjectMocks
    private UserImportServiceImpl service;

    private final AtomicReference<Runnable> submittedTask = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "user-import-test");
        TableInfoHelper.initTableInfo(assistant, UserImportTask.class);
        TableInfoHelper.initTableInfo(assistant, UserImportRow.class);
        when(securityContextAccessor.currentUserId()).thenReturn(OPERATOR_ID);
        when(timeMapper.toLocalDateTime(any(Instant.class))).thenReturn(LocalDateTime.of(2026, 8, 22, 22, 55, 16));
        when(departmentService.list()).thenReturn(List.of());
        when(dictService.listDictDataByGroupCode(any())).thenReturn(List.of());
        when(profileService.all()).thenReturn(List.of());
        when(taskMapper.update(any(), any())).thenReturn(1);
        when(taskMapper.updateById(any(UserImportTask.class))).thenReturn(1);
        doAnswer(invocation -> {
            submittedTask.set(invocation.getArgument(0));
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));
    }

    @Test
    void applyShouldReturnApplyingAndRejectRepeatedDispatch() {
        var task = task("file-hash", false, List.of());
        when(taskMapper.selectOne(any())).thenReturn(task);
        when(rowMapper.selectList(any())).thenReturn(List.of());

        UserImportTaskVO result = service.apply(task.getId(), applyRequest());

        assertThat(result.getStatus()).isEqualTo("APPLYING");
        assertThat(submittedTask.get()).isNotNull();
        submittedTask.get().run();
        assertThat(task.getStatus()).isEqualTo("SUCCEEDED");

        UserImportTaskVO repeated = service.apply(task.getId(), applyRequest());

        assertThat(repeated.getStatus()).isEqualTo("SUCCEEDED");
        verify(taskExecutor).execute(any(Runnable.class));
    }

    @Test
    void applyShouldCountPreviewErrorsAsCompletedRows() {
        var row = errorRow();
        var task = task("file-hash", false, List.of(row));
        task.setTotalRows(1);
        task.setErrorRows(1);
        task.setProfileVersionHash(SHA256Utils.hash("profile|MISSING"));
        task.setRequestHash(SHA256Utils.hash("file-hash\u001ffalse\u001e\u001fEMP-001\u001f13800138000"
                + "\u001fzhangsan@example.com\u001fdept\u001fzh-CN\u001fAsia/Shanghai\u001fprofile"));
        when(taskMapper.selectOne(any())).thenReturn(task);
        when(rowMapper.selectList(any())).thenReturn(List.of(row));

        service.apply(task.getId(), applyRequest());
        submittedTask.get().run();

        assertThat(task.getCompletedRows()).isEqualTo(1);
        assertThat(task.getErrorRows()).isEqualTo(1);
        assertThat(task.getStatus()).isEqualTo("FAILED");
    }

    private UserImportTask task(String fileHash, boolean skipExisting, List<UserImportRow> rows) {
        var task = new UserImportTask();
        task.setId(UUID.randomUUID());
        task.setOperatorId(OPERATOR_ID);
        task.setFileHash(fileHash);
        task.setSkipExisting(skipExisting);
        task.setStatus("PREVIEWED");
        task.setExpiresAt(Instant.now().plusSeconds(60));
        task.setPreviewExpiresAt(Instant.now().plusSeconds(60));
        task.setPreviewTokenHash(SHA256Utils.hash("preview-token"));
        task.setTotalRows(rows.size());
        task.setValidRows(rows.size());
        task.setErrorRows(0);
        task.setProfileVersionHash(SHA256Utils.hash(""));
        task.setRequestHash(SHA256Utils.hash(fileHash + "\u001f" + skipExisting));
        return task;
    }

    private UserImportRow errorRow() {
        var normalized = new LinkedHashMap<String, Object>();
        normalized.put("employee_no", "EMP-001");
        normalized.put("real_name", "张三");
        normalized.put("phone", "13800138000");
        normalized.put("email", "zhangsan@example.com");
        normalized.put("department_code", "dept");
        normalized.put("language", "zh-CN");
        normalized.put("timezone", "Asia/Shanghai");
        normalized.put("authorization_profile_code", "profile");
        var row = new UserImportRow();
        row.setState("ERROR");
        row.setNormalizedData(normalized);
        return row;
    }

    private UserImportApplyFrom applyRequest() {
        var request = new UserImportApplyFrom();
        request.setPreviewToken("preview-token");
        return request;
    }

}
