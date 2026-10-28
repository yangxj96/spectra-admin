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

package com.devops00.spectra.core.system.service.impl;

import com.devops00.spectra.common.exception.DataException;
import com.devops00.spectra.core.system.javabean.entity.ServiceMonitorDiagnosticTask;
import com.devops00.spectra.core.system.javabean.from.ServiceMonitorDiagnosticFrom;
import com.devops00.spectra.core.system.mapper.ServiceMonitorDiagnosticTaskMapper;
import com.devops00.spectra.framework.serialization.mapper.TimeMapper;
import io.micrometer.core.instrument.MeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.core.task.TaskExecutor;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.mock.env.MockEnvironment;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * 验证诊断任务绑定生成实例，非归属实例不能执行清理或下载。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/28
 */
class ServiceMonitorDiagnosticInstanceOwnershipTest {

    private ServiceMonitorDiagnosticTaskMapper taskMapper;
    private TaskExecutor taskExecutor;
    private ServiceMonitorDiagnosticServiceImpl service;
    private Path directory;

    @BeforeEach
    void setUp(@TempDir Path tempDirectory) {
        directory = tempDirectory;
        var environment = new MockEnvironment()
                .withProperty("spectra.monitor.diagnostics.enabled", "true")
                .withProperty("spectra.monitor.diagnostics.directory", tempDirectory.toString())
                .withProperty("spectra.monitor.diagnostics.instance-id", "node-a");
        taskMapper = mock(ServiceMonitorDiagnosticTaskMapper.class);
        taskExecutor = mock(TaskExecutor.class);
        service = new ServiceMonitorDiagnosticServiceImpl(mock(MeterRegistry.class), mock(DataSource.class),
                mock(RedisConnectionFactory.class), environment, mock(TimeMapper.class), taskMapper, taskExecutor);
    }

    @Test
    void createTaskBindsCurrentInstance() {
        var inserted = new AtomicReference<ServiceMonitorDiagnosticTask>();
        when(taskMapper.acquireAdmissionLock()).thenReturn(1);
        when(taskMapper.selectCount(any())).thenReturn(0L);
        when(taskMapper.insert(any(ServiceMonitorDiagnosticTask.class))).thenAnswer(invocation -> {
            inserted.set(invocation.getArgument(0));
            return 1;
        });
        var from = new ServiceMonitorDiagnosticFrom();
        from.setTaskType("THREAD_DUMP");

        service.createTask(from);

        assertEquals("node-a", inserted.get().getInstanceId());
    }

    @Test
    void downloadRejectsTaskOwnedByAnotherInstance() {
        var task = task("node-b", "SUCCEEDED", "other.txt", Instant.now().plusSeconds(3600));
        when(taskMapper.selectOne(any())).thenReturn(task);

        var exception = assertThrows(DataException.class, () -> service.openDownload(task.getId()));

        assertTrue(exception.getMessage().contains("生成实例"));
    }

    @Test
    void cleanupSkipsTaskOwnedByAnotherInstance() throws IOException {
        var file = Files.writeString(directory.resolve("other.txt"), "dump");
        var task = task("node-b", "SUCCEEDED", "other.txt", Instant.now().minusSeconds(1));
        when(taskMapper.selectList(any())).thenReturn(List.of(task));

        service.cleanupExpiredTasks();

        assertTrue(Files.exists(file));
        verify(taskMapper, never()).deleteById(task.getId());
    }

    @Test
    void cleanupDeletesOwnedFileAndRecord() throws IOException {
        var file = Files.writeString(directory.resolve("own.txt"), "dump");
        var task = task("node-a", "SUCCEEDED", "own.txt", Instant.now().minusSeconds(1));
        when(taskMapper.selectList(any())).thenReturn(List.of(task));
        when(taskMapper.deleteById(task.getId())).thenReturn(1);

        service.cleanupExpiredTasks();

        assertFalse(Files.exists(file));
        verify(taskMapper).deleteById(task.getId());
    }

    @Test
    void legacySucceededTaskWithoutLocalFileKeepsRecord() {
        var task = task("unknown", "SUCCEEDED", "legacy.txt", Instant.now().minusSeconds(1));
        when(taskMapper.selectList(any())).thenReturn(List.of(task));

        service.cleanupExpiredTasks();

        verify(taskMapper, never()).deleteById(task.getId());
    }

    private ServiceMonitorDiagnosticTask task(String owner, String status, String fileName, Instant expiresAt) {
        var task = new ServiceMonitorDiagnosticTask();
        task.setId(UUID.randomUUID());
        task.setInstanceId(owner);
        task.setStatus(status);
        task.setFileName(fileName);
        task.setExpiresAt(expiresAt);
        return task;
    }
}
