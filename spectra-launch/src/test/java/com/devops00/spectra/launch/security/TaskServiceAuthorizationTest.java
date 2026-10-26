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

package com.devops00.spectra.launch.security;

import com.devops00.spectra.common.port.security.SecurityContextAccessor;
import com.devops00.spectra.common.port.security.SecurityPrincipal;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshot;
import com.devops00.spectra.common.security.authorization.AuthorizationSnapshotProvider;
import com.devops00.spectra.common.security.authorization.AuthorizationMutationFence;
import com.devops00.spectra.framework.persistence.pagination.PageFrom;
import com.devops00.spectra.workflow.service.impl.TaskServiceImpl;
import org.flowable.engine.TaskService;
import org.flowable.task.api.TaskQuery;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_SELF;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

/**
 * Workflow 公共任务 Service 必须把传入办理人与当前身份和有效权限绑定。
 *
 * @author yangxj96
 * @version 1.0
 * @since 2026/10/19
 */
class TaskServiceAuthorizationTest {

    private final TaskService flowableTasks = mock(TaskService.class);

    private final SecurityContextAccessor security = mock(SecurityContextAccessor.class);

    private final AuthorizationSnapshotProvider snapshots = mock(AuthorizationSnapshotProvider.class);

    private final AuthorizationMutationFence mutationFence = mock(AuthorizationMutationFence.class);

    private final UUID userId = UUID.randomUUID();

    private TaskServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TaskServiceImpl(flowableTasks, null, null, null, null, null, null, null, null, security, snapshots,
                mutationFence);
    }

    @Test
    void anonymousAndForgedOperatorAreRejectedBeforeFlowableOrAuthorizationLookup() {
        assertAllWritesDenied("alice");
        authenticate("bob");
        assertAllWritesDenied("alice");
        verifyNoInteractions(flowableTasks, snapshots, mutationFence);
    }

    @Test
    void missingOrUnknownPermissionRejectsAllWritesBeforeFlowable() {
        authenticate("alice");
        assertAllWritesDenied("alice");
        var authorization = mock(AuthorizationSnapshot.class);
        when(snapshots.load(userId)).thenReturn(authorization);
        assertAllWritesDenied("alice");
        verifyNoInteractions(flowableTasks);
    }

    @Test
    void authorizationReadFailurePropagatesBeforeTaskMutation() {
        authenticate("alice");
        var failure = new IllegalStateException("synthetic authorization lookup failure");
        when(snapshots.load(userId)).thenThrow(failure);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> service.complete("task", "comment", "alice")));
        verifyNoInteractions(flowableTasks);
    }

    @Test
    void authorizationFenceFailureRejectsBeforeSnapshotOrTaskMutation() {
        authenticate("alice");
        var failure = new IllegalStateException("synthetic authorization lock failure");
        doThrow(failure).when(mutationFence).lockUser(userId);
        assertSame(failure, assertThrows(IllegalStateException.class,
                () -> service.complete("task", "comment", "alice")));
        verifyNoInteractions(snapshots, flowableTasks);
    }

    @Test
    void permittedOwnerCanTransferOwnTask() {
        authenticate("alice");
        var authorization = mock(AuthorizationSnapshot.class);
        when(authorization.hasPermission("workflow:task:update")).thenReturn(true);
        when(snapshots.load(userId)).thenReturn(authorization);
        var query = mock(TaskQuery.class, RETURNS_SELF);
        var task = mock(org.flowable.task.api.Task.class);
        when(flowableTasks.createTaskQuery()).thenReturn(query);
        when(query.singleResult()).thenReturn(task);
        service.transfer("task", "bob", "alice");
        verify(query).taskId("task");
        verify(query).taskAssignee("alice");
        verify(flowableTasks).setAssignee("task", "bob");
    }

    @Test
    void readQueriesCannotUseAnotherUsersAssignee() {
        authenticate("alice");
        var page = new PageFrom();
        assertThrows(AccessDeniedException.class, () -> service.todo(page, "bob", null));
        assertThrows(AccessDeniedException.class, () -> service.done(page, "bob", null));
        assertFalse(service.canAccessProcess("instance", "bob"));
        verifyNoInteractions(flowableTasks);
    }

    @Test
    void ownTodoStillReturnsAnEmptyPage() {
        authenticate("alice");
        var query = mock(TaskQuery.class, RETURNS_SELF);
        when(flowableTasks.createTaskQuery()).thenReturn(query);
        when(query.count()).thenReturn(0L);
        when(query.listPage(0, 10)).thenReturn(List.of());
        var page = new PageFrom(10L, 1L, null);
        assertEquals(0L, service.todo(page, "alice", null).getTotal());
    }

    private void authenticate(String username) {
        var principal = mock(SecurityPrincipal.class);
        when(principal.getId()).thenReturn(userId);
        when(principal.getUsername()).thenReturn(username);
        when(security.currentUser()).thenReturn(principal);
    }

    private void assertAllWritesDenied(String operator) {
        assertThrows(AccessDeniedException.class, () -> service.complete("task", "comment", operator));
        assertThrows(AccessDeniedException.class, () -> service.reject("task", "comment", operator));
        assertThrows(AccessDeniedException.class, () -> service.transfer("task", "bob", operator));
        assertThrows(AccessDeniedException.class, () -> service.delegate("task", "bob", operator));
    }
}
