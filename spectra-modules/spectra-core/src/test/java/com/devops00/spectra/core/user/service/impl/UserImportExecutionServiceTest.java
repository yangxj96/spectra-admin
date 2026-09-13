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

import com.baomidou.mybatisplus.core.MybatisConfiguration;
import com.baomidou.mybatisplus.core.metadata.TableInfoHelper;
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.framework.persistence.mybatis.handler.UUIDTypeHandler;
import com.devops00.spectra.core.user.javabean.entity.UserImportTask;
import com.devops00.spectra.core.user.javabean.enums.UserImportTaskStatus;
import com.devops00.spectra.core.user.javabean.from.UserImportApplyFrom;
import com.devops00.spectra.core.user.mapper.UserImportRowMapper;
import com.devops00.spectra.core.user.mapper.UserImportTaskMapper;
import com.devops00.spectra.core.user.provider.DefaultUserPasswordProvider;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 用户导入 Apply 对默认密码配置的前置检查测试。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/09/13
 */
@ExtendWith(MockitoExtension.class)
class UserImportExecutionServiceTest {

    private static final UUID TASK_ID = UUID.fromString("018f3f2a-7c44-7d31-8c21-9a48de15f120");

    private static final UUID OPERATOR_ID = UUID.fromString("018f3f2a-7c44-7d31-8c21-9a48de15f121");

    private static final String DEFAULT_PASSWORD_HASH = "{bcrypt}encoded-default-password";

    @Mock
    private UserImportTaskMapper taskMapper;

    @Mock
    private UserImportRowMapper rowMapper;

    @Mock
    private UserImportPreviewService previewService;

    @Mock
    private UserImportResultService resultService;

    @Mock
    private SecurityContextAccessor securityContextAccessor;

    @Mock
    private UserImportExecutionWorker executionWorker;

    @Mock
    private TaskExecutor userImportTaskExecutor;

    @Mock
    private DefaultUserPasswordProvider defaultUserPasswordProvider;

    @InjectMocks
    private UserImportExecutionService service;

    @BeforeEach
    void setUp() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        TableInfoHelper.initTableInfo(new MapperBuilderAssistant(configuration, "user-import-apply-test"), UserImportTask.class);
    }

    @Test
    void shouldRejectApplyBeforeConsumingThePreviewWhenDefaultPasswordIsMissing() {
        var task = new UserImportTask();
        task.setId(TASK_ID);
        task.setStatus(UserImportTaskStatus.PREVIEWED.name());
        task.setOperatorId(OPERATOR_ID);
        when(resultService.requireTask(TASK_ID)).thenReturn(task);
        lenient().when(defaultUserPasswordProvider.requireEncodedPassword())
                .thenThrow(new DataException("系统默认密码未配置，请先在系统设置中配置"));

        var exception = assertThrows(DataException.class,
                () -> service.apply(TASK_ID, new UserImportApplyFrom()));

        assertEquals("系统默认密码未配置，请先在系统设置中配置", exception.getMessage());
        verify(defaultUserPasswordProvider).requireEncodedPassword();
        verifyNoInteractions(taskMapper, userImportTaskExecutor);
    }

    @Test
    void shouldUseOneDefaultPasswordSnapshotForTheWholeApply() {
        var task = new UserImportTask();
        task.setId(TASK_ID);
        task.setStatus(UserImportTaskStatus.PREVIEWED.name());
        task.setOperatorId(OPERATOR_ID);
        task.setErrorRows(0);
        var row = new com.devops00.spectra.core.user.javabean.entity.UserImportRow();
        row.setState(com.devops00.spectra.core.user.javabean.enums.UserImportRowState.VALID.name());
        when(resultService.requireTask(TASK_ID)).thenReturn(task);
        when(defaultUserPasswordProvider.requireEncodedPassword()).thenReturn(DEFAULT_PASSWORD_HASH);
        when(securityContextAccessor.currentUserId()).thenReturn(OPERATOR_ID);
        when(taskMapper.update(isNull(), any())).thenReturn(1);
        when(resultService.findTask(TASK_ID, OPERATOR_ID)).thenReturn(task);
        when(rowMapper.selectList(any())).thenReturn(java.util.List.of(row));
        when(previewService.loadReferenceData()).thenReturn(
                new UserImportPreviewService.ReferenceData(java.util.Map.of(), java.util.Set.of(),
                        java.util.Set.of(), java.util.Map.of()));
        when(executionWorker.processChunk(eq(TASK_ID), eq(OPERATOR_ID), anyList(), eq(false), any(),
                eq(DEFAULT_PASSWORD_HASH)))
                .thenReturn(new UserImportExecutionWorker.ChunkResult(1, 1, 0, 0));
        when(taskMapper.updateById(any(UserImportTask.class))).thenReturn(1);
        doAnswer(invocation -> {
            ((Runnable) invocation.getArgument(0)).run();
            return null;
        }).when(userImportTaskExecutor).execute(any(Runnable.class));

        service.apply(TASK_ID, new UserImportApplyFrom());

        verify(executionWorker).processChunk(eq(TASK_ID), eq(OPERATOR_ID), anyList(), eq(false), any(),
                eq(DEFAULT_PASSWORD_HASH));
    }
}
