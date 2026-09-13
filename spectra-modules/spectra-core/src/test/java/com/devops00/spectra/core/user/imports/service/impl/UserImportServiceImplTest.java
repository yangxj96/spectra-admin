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

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.core.user.imports.javabean.entity.UserImportRow;
import com.devops00.spectra.core.user.imports.javabean.entity.UserImportTask;
import com.devops00.spectra.core.user.imports.javabean.enums.UserImportRowState;
import com.devops00.spectra.core.user.imports.javabean.from.UserImportApplyFrom;
import com.devops00.spectra.core.user.imports.javabean.vo.UserImportTaskVO;
import com.devops00.spectra.core.user.imports.mapper.UserImportRowMapper;
import com.devops00.spectra.core.user.imports.mapper.UserImportTaskMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.task.TaskExecutor;

import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.Set;
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
    private UserImportPreviewService previewService;

    @Mock
    private UserImportResultService resultService;

    @Mock
    private SecurityContextAccessor securityContextAccessor;

    @Mock
    private UserImportExecutionWorker executionWorker;

    @Mock
    private TaskExecutor taskExecutor;

    @InjectMocks
    private UserImportExecutionService service;

    private final AtomicReference<Runnable> submittedTask = new AtomicReference<>();

    @BeforeEach
    void setUp() {
        when(securityContextAccessor.currentUserId()).thenReturn(OPERATOR_ID);
        when(taskMapper.update(any(), any())).thenReturn(1);
        when(taskMapper.updateById(any(UserImportTask.class))).thenReturn(1);
        doAnswer(invocation -> {
            submittedTask.set(invocation.getArgument(0));
            return null;
        }).when(taskExecutor).execute(any(Runnable.class));
        when(previewService.loadReferenceData()).thenReturn(
                new UserImportPreviewService.ReferenceData(Map.of(), Set.of(), Set.of(), Map.of()));
        when(resultService.toVO(any(UserImportTask.class))).thenAnswer(invocation -> {
            var task = invocation.getArgument(0, UserImportTask.class);
            var result = new UserImportTaskVO();
            result.setStatus(task.getStatus());
            return result;
        });
    }

    @Test
    void applyShouldReturnApplyingAndRejectRepeatedDispatch() {
        var task = task(List.of());
        when(resultService.requireTask(task.getId())).thenReturn(task);
        when(resultService.findTask(task.getId(), OPERATOR_ID)).thenReturn(task);
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
    void applyShouldRetainPartialFailureAtRowAndTaskLevel() {
        var row = new UserImportRow();
        row.setState(UserImportRowState.VALID.name());
        var task = task(List.of(row));
        when(resultService.requireTask(task.getId())).thenReturn(task);
        when(resultService.findTask(task.getId(), OPERATOR_ID)).thenReturn(task);
        when(rowMapper.selectList(any())).thenReturn(List.of(row));
        when(executionWorker.processChunk(any(), any(), any(), any(Boolean.TYPE), any()))
                .thenReturn(new UserImportExecutionWorker.ChunkResult(1, 0, 0, 1));

        service.apply(task.getId(), applyRequest());
        submittedTask.get().run();

        assertThat(task.getStatus()).isEqualTo("PARTIAL_FAILED");
        assertThat(task.getErrorRows()).isEqualTo(1);
        assertThat(task.getCompletedRows()).isEqualTo(0);
    }

    @Test
    void applyShouldCountPreviewErrorsAsCompletedAndFailed() {
        var row = new UserImportRow();
        row.setState(UserImportRowState.ERROR.name());
        var task = task(List.of(row));
        task.setErrorRows(1);
        task.setCompletedRows(1);
        when(resultService.requireTask(task.getId())).thenReturn(task);
        when(resultService.findTask(task.getId(), OPERATOR_ID)).thenReturn(task);
        when(rowMapper.selectList(any())).thenReturn(List.of(row));

        service.apply(task.getId(), applyRequest());
        submittedTask.get().run();

        assertThat(task.getStatus()).isEqualTo("FAILED");
        assertThat(task.getErrorRows()).isEqualTo(1);
    }

    private UserImportTask task(List<UserImportRow> rows) {
        var task = new UserImportTask();
        task.setId(UUID.randomUUID());
        task.setOperatorId(OPERATOR_ID);
        task.setFileHash("file-hash");
        task.setSkipExisting(false);
        task.setStatus("PREVIEWED");
        task.setExpiresAt(Instant.now().plusSeconds(60));
        task.setPreviewExpiresAt(Instant.now().plusSeconds(60));
        task.setTotalRows(rows.size());
        task.setValidRows(rows.size());
        task.setErrorRows(0);
        return task;
    }

    private UserImportApplyFrom applyRequest() {
        var request = new UserImportApplyFrom();
        request.setPreviewToken("preview-token");
        return request;
    }
}
