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
import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.common.mybatis.handler.UUIDTypeHandler;
import com.devops00.spectra.core.user.imports.entity.UserImportRow;
import com.devops00.spectra.core.user.imports.entity.UserImportTask;
import com.devops00.spectra.core.user.imports.javabean.enums.UserImportRowState;
import com.devops00.spectra.core.user.imports.mapper.UserImportRowMapper;
import com.devops00.spectra.core.user.imports.mapper.UserImportTaskMapper;
import org.apache.ibatis.builder.MapperBuilderAssistant;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserImportExecutionWorkerTest {

    @Mock
    private UserImportTaskMapper taskMapper;

    @Mock
    private UserImportRowMapper rowMapper;

    @Mock
    private UserImportRowProcessor rowProcessor;

    @InjectMocks
    private UserImportExecutionWorker worker;

    @BeforeEach
    void setUp() {
        var configuration = new MybatisConfiguration();
        configuration.getTypeHandlerRegistry().register(UUID.class, UUIDTypeHandler.class);
        var assistant = new MapperBuilderAssistant(configuration, "user-import-worker-test");
        TableInfoHelper.initTableInfo(assistant, UserImportTask.class);
        TableInfoHelper.initTableInfo(assistant, UserImportRow.class);
        when(rowMapper.updateById(any(UserImportRow.class))).thenReturn(1);
        when(taskMapper.updateById(any(UserImportTask.class))).thenReturn(1);
    }

    @Test
    void failedRowIsRetainedAndChunkProgressIsCommitted() {
        var taskId = UUID.randomUUID();
        var operatorId = UUID.randomUUID();
        var task = new UserImportTask();
        task.setId(taskId);
        task.setOperatorId(operatorId);
        task.setErrorRows(0);
        var row = new UserImportRow();
        row.setState(UserImportRowState.VALID.name());
        when(taskMapper.selectOne(any())).thenReturn(task);
        when(rowProcessor.processInCurrentTransaction(any(), any(Boolean.TYPE), any(), any()))
                .thenThrow(new DataException("用户已存在"));

        var result = worker.processChunk(taskId, operatorId, List.of(row), false,
                new UserImportPreviewService.ReferenceData(Map.of(), Set.of(), Set.of(), Map.of()));

        assertThat(result.processedRows()).isEqualTo(1);
        assertThat(result.errorRows()).isEqualTo(1);
        assertThat(task.getCompletedRows()).isEqualTo(1);
        assertThat(task.getErrorRows()).isEqualTo(1);
        assertThat(row.getState()).isEqualTo(UserImportRowState.ERROR.name());
        assertThat(row.getErrors()).containsKey("apply");
    }
}
